#!/usr/bin/env python3
"""Turn the Dolphin Optimized Prism instance zip into a Modrinth .mrpack.

Prism's importer accepts only minecraft, fabric-loader, quilt-loader, forge,
and neoforge in modrinth.index.json. Any other dependency, including an
"ornithe" key, aborts the import. Ornithe on 1.8.9 is Fabric Loader 0.19.3
plus Calamus generation 2 intermediary mappings, Flap, and a handful of
libraries the Ornithe installer adds so the game can start on modern Java.

The index therefore depends on Fabric Loader. The same archive carries Prism
component files in patches/. Prism uses a patches/<uid>.json file instead of
downloading that component from its metadata server, and while resolving
dependencies it adds every component those files require. The shipped
intermediary patch points at net.ornithemc:calamus-intermediary-gen2:1.8.9
and requires the rest of the Ornithe runtime, so importing the .mrpack
installs Ornithe rather than Fabric's own intermediary (which has no 1.8.9
build).

modrinth.index.json is stored first. Prism stops at the first of that file
or instance.cfg, and this pack must take the Modrinth path so the dependency
resolver runs.
"""

from __future__ import annotations

import argparse
import json
import urllib.request
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent
PATCH_DIR = ROOT / "prism-patches"
DEFAULT_SOURCE = (
    "https://github.com/dsnsgithub/DolphinClient/releases/download/"
    "v1.0.2/Dolphin-Optimized.Pink.v1.0.2.zip"
)
GAME_PREFIX = "minecraft/"
SKIP_GAME_FILES = {
    "minecraft/usercache.json",
    "minecraft/config/dolphinclient/accounts.json",
}
PACK_NAME = "Dolphin Optimized Pink"
MINECRAFT_VERSION = "1.8.9"
FABRIC_LOADER_VERSION = "0.19.3"


def load_patch(path: Path) -> dict:
    with path.open(encoding="utf-8") as handle:
        data = json.load(handle)
    if data.get("uid") != path.stem:
        raise SystemExit(f"{path.name} uid is {data.get('uid')!r}, expected {path.stem!r}")
    return data


def patches() -> dict[str, dict]:
    found = {}
    for path in sorted(PATCH_DIR.glob("*.json")):
        found[path.stem] = load_patch(path)
    return found


def index_document(version_id: str) -> dict:
    return {
        "formatVersion": 1,
        "game": "minecraft",
        "versionId": version_id,
        "name": PACK_NAME,
        "summary": (
            "Optimized Hypixel 1.8.9 client. Prism installs Ornithe from the "
            "bundled Fabric Loader and Calamus generation 2 components."
        ),
        "files": [],
        "dependencies": {
            "minecraft": MINECRAFT_VERSION,
            "fabric-loader": FABRIC_LOADER_VERSION,
        },
    }


def resolve_components(component_patches: dict[str, dict]) -> dict[str, str]:
    """Walk the same requires edges Prism follows after an .mrpack import."""
    versions = {
        "net.minecraft": MINECRAFT_VERSION,
        "net.fabricmc.fabric-loader": FABRIC_LOADER_VERSION,
    }
    changed = True
    while changed:
        changed = False
        for uid in list(versions):
            patch = component_patches.get(uid)
            if patch is None:
                continue
            for require in patch.get("requires", []):
                dep = require["uid"]
                wanted = require.get("equals") or require.get("suggests")
                if dep not in versions:
                    versions[dep] = wanted or ""
                    changed = True
                elif wanted and versions[dep] not in ("", wanted):
                    raise SystemExit(
                        f"{uid} requires {dep} {wanted}, already pinned to {versions[dep]}"
                    )
    return versions


def check_components(component_patches: dict[str, dict]) -> None:
    versions = resolve_components(component_patches)
    missing = [uid for uid in versions if uid not in component_patches]
    if missing:
        raise SystemExit("No local patch for: " + ", ".join(missing))
    for uid, wanted in versions.items():
        actual = component_patches[uid].get("version")
        if wanted and actual != wanted:
            raise SystemExit(f"{uid} patch version {actual} != required {wanted}")

    intermediary = component_patches["net.fabricmc.intermediary"]
    libraries = [lib.get("name") for lib in intermediary.get("libraries", [])]
    if "net.ornithemc:calamus-intermediary-gen2:1.8.9" not in libraries:
        raise SystemExit("Intermediary patch is not Calamus generation 2 for 1.8.9")
    agents = component_patches["net.ornithemc.flap"].get("+agents", [])
    if not any(agent.get("name") == "net.ornithemc:flap:0.0.5" for agent in agents):
        raise SystemExit("Flap patch is missing the Ornithe java agent")
    if component_patches["net.fabricmc.fabric-loader"].get("mainClass") != (
        "net.fabricmc.loader.impl.launch.knot.KnotClient"
    ):
        raise SystemExit("Fabric Loader patch does not launch Knot")


def dolphin_version(jar_name: str) -> str:
    prefix = "DolphinClient-"
    if not jar_name.startswith(prefix) or not jar_name.endswith(".jar"):
        raise SystemExit(f"Not a DolphinClient jar: {jar_name}")
    return jar_name[len(prefix) :].split("+", 1)[0]


def detect_version(source: zipfile.ZipFile) -> str:
    prefix = "minecraft/mods/DolphinClient-"
    matches = [
        name[len(prefix) :].split("+", 1)[0]
        for name in source.namelist()
        if name.startswith(prefix) and name.endswith(".jar")
    ]
    if len(matches) != 1:
        raise SystemExit(f"Expected one DolphinClient jar, found {matches}")
    return matches[0]


def is_dolphin_mod(relative: str) -> bool:
    name = relative.rsplit("/", 1)[-1]
    return relative.startswith("mods/") and name.startswith("DolphinClient-") and name.endswith(".jar")


def build(source_path: Path, output_path: Path, dolphin_jar: Path | None = None) -> str:
    component_patches = patches()
    check_components(component_patches)
    replacement_name = None
    replacement_bytes = None
    if dolphin_jar is not None:
        replacement_name = dolphin_jar.name
        replacement_bytes = dolphin_jar.read_bytes()
        version_id = dolphin_version(replacement_name)
    with zipfile.ZipFile(source_path) as source:
        if dolphin_jar is None:
            version_id = detect_version(source)
        game_files = [
            info
            for info in source.infolist()
            if info.filename.startswith(GAME_PREFIX)
            and not info.is_dir()
            and info.filename not in SKIP_GAME_FILES
        ]
        if not any(info.filename.startswith("minecraft/mods/") and info.filename.endswith(".jar") for info in game_files):
            raise SystemExit("Source zip has no mods to bundle")

        output_path.parent.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(output_path, "w") as pack:
            # Must be the first entry Prism's importer inspects.
            pack.writestr(
                "modrinth.index.json",
                json.dumps(index_document(version_id), indent=2) + "\n",
            )
            instance_cfg = (ROOT / "instance.cfg").read_bytes()
            pack.writestr("instance.cfg", instance_cfg)
            for uid, data in component_patches.items():
                pack.writestr(
                    f"patches/{uid}.json",
                    json.dumps(data, indent=4) + "\n",
                )
            replaced = False
            for info in game_files:
                relative = info.filename[len(GAME_PREFIX) :]
                if replacement_bytes is not None and is_dolphin_mod(relative):
                    replaced = True
                    continue
                pack.writestr(f"overrides/{relative}", source.read(info))
            if replacement_bytes is not None:
                if not replaced:
                    raise SystemExit("Source pack has no DolphinClient jar to replace")
                pack.writestr(f"overrides/mods/{replacement_name}", replacement_bytes)
    return version_id


def check_pack(path: Path, dolphin_jar: Path | None = None) -> None:
    with zipfile.ZipFile(path) as pack:
        names = pack.namelist()
        if not names or names[0] != "modrinth.index.json":
            raise SystemExit(f"modrinth.index.json is not the first entry ({names[:3]})")
        if "instance.cfg" not in names:
            raise SystemExit("instance.cfg missing")
        index = json.loads(pack.read("modrinth.index.json"))
        dependencies = index["dependencies"]
        if set(dependencies) != {"minecraft", "fabric-loader"}:
            raise SystemExit(f"Unexpected dependencies: {dependencies}")
        if dependencies["minecraft"] != MINECRAFT_VERSION:
            raise SystemExit("Minecraft dependency changed")
        if dependencies["fabric-loader"] != FABRIC_LOADER_VERSION:
            raise SystemExit("Fabric Loader dependency changed")
        if index["formatVersion"] != 1 or index["game"] != "minecraft":
            raise SystemExit("Index header is not a Minecraft formatVersion 1 pack")
        bundled = [name for name in names if name.startswith("overrides/mods/") and name.endswith(".jar")]
        if len(bundled) < 8:
            raise SystemExit(f"Only {len(bundled)} mods were bundled")
        dolphin_mods = [name for name in bundled if is_dolphin_mod(name.removeprefix("overrides/"))]
        if len(dolphin_mods) != 1:
            raise SystemExit(f"Expected one DolphinClient jar, found {dolphin_mods}")
        if dolphin_jar is not None:
            if Path(dolphin_mods[0]).name != dolphin_jar.name:
                raise SystemExit(f"Pack jar {dolphin_mods[0]} != {dolphin_jar.name}")
            if pack.read(dolphin_mods[0]) != dolphin_jar.read_bytes():
                raise SystemExit("Pack DolphinClient jar does not match --dolphin-jar")
            if index["versionId"] != dolphin_version(dolphin_jar.name):
                raise SystemExit("Pack versionId does not match --dolphin-jar")
        if any(name.endswith("usercache.json") or name.endswith("accounts.json") for name in names):
            raise SystemExit("Personal account files were copied into the pack")
        intermediary = json.loads(pack.read("patches/net.fabricmc.intermediary.json"))
        library_names = [lib["name"] for lib in intermediary["libraries"]]
        if "net.ornithemc:calamus-intermediary-gen2:1.8.9" not in library_names:
            raise SystemExit("Built pack lost the Calamus intermediary")
        required = {req["uid"] for req in intermediary["requires"]}
        for uid in required:
            patch_name = f"patches/{uid}.json"
            if uid != "net.minecraft" and patch_name not in names:
                raise SystemExit(f"Missing {patch_name}")
        print(f"ok {path} version={index['versionId']} mods={len(bundled)} components={sum(1 for n in names if n.startswith('patches/'))}")


def resolve_source(source: str, cache: Path) -> Path:
    path = Path(source)
    if path.is_file():
        return path
    cache.parent.mkdir(parents=True, exist_ok=True)
    if not cache.is_file():
        urllib.request.urlretrieve(source, cache)
    return cache


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", default=DEFAULT_SOURCE, help="Prism instance zip, or a URL")
    parser.add_argument(
        "--output",
        type=Path,
        default=ROOT.parent / "build" / "Dolphin-Optimized.Pink.v1.0.2.mrpack",
    )
    parser.add_argument(
        "--dolphin-jar",
        type=Path,
        help="Replace the pack's DolphinClient jar with this freshly built jar",
    )
    parser.add_argument("--check", action="store_true", help="Validate the written .mrpack")
    args = parser.parse_args()
    if args.dolphin_jar is not None and not args.dolphin_jar.is_file():
        raise SystemExit(f"DolphinClient jar not found: {args.dolphin_jar}")
    source = resolve_source(args.source, ROOT.parent / "build" / "source-instance.zip")
    version = build(source, args.output, args.dolphin_jar)
    print(f"wrote {args.output} ({version})")
    if args.check:
        check_pack(args.output, args.dolphin_jar)


if __name__ == "__main__":
    main()
