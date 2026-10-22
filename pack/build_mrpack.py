#!/usr/bin/env python3
"""Assemble the Dolphin Optimized .mrpack from pack/overrides."""

from __future__ import annotations

import argparse
import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent
PATCH_DIR = ROOT / "prism-patches"
OVERRIDES = ROOT / "overrides"
MODRINTH_FILES = ROOT / "modrinth-files.json"
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


def modrinth_files() -> list[dict]:
    with MODRINTH_FILES.open(encoding="utf-8") as handle:
        files = json.load(handle)
    if not isinstance(files, list) or not files:
        raise SystemExit("modrinth-files.json must list the Modrinth downloads")
    paths = []
    for entry in files:
        path = entry.get("path")
        hashes = entry.get("hashes") or {}
        downloads = entry.get("downloads") or []
        if not isinstance(path, str) or not (
            (path.startswith("mods/") and path.endswith(".jar"))
            or (path.startswith("resourcepacks/") and path.endswith(".zip"))
        ):
            raise SystemExit(f"Modrinth file path is not a mod jar or resource pack: {path!r}")
        if is_dolphin_mod(path):
            raise SystemExit("DolphinClient is built here and is not a Modrinth download")
        if not hashes.get("sha1") or not hashes.get("sha512"):
            raise SystemExit(f"{path} is missing sha1 or sha512")
        if not downloads or not all(url.startswith("https://cdn.modrinth.com/") for url in downloads):
            raise SystemExit(f"{path} is not a Modrinth CDN download")
        if not isinstance(entry.get("fileSize"), int):
            raise SystemExit(f"{path} is missing fileSize")
        paths.append(path)
    if paths != sorted(paths) or len(paths) != len(set(paths)):
        raise SystemExit("Modrinth files must be unique and sorted by path")
    return files


def index_document(version_id: str, files: list[dict]) -> dict:
    return {
        "formatVersion": 1,
        "game": "minecraft",
        "versionId": version_id,
        "name": PACK_NAME,
        "summary": (
            "Optimized Hypixel 1.8.9 client. Prism installs Ornithe from the "
            "bundled Fabric Loader and Calamus generation 2 components."
        ),
        "files": files,
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


def is_dolphin_mod(relative: str) -> bool:
    name = relative.rsplit("/", 1)[-1]
    return relative.startswith("mods/") and name.startswith("DolphinClient-") and name.endswith(".jar")


def override_files(modrinth_paths: set[str]) -> list[tuple[str, bytes]]:
    if not OVERRIDES.is_dir():
        raise SystemExit(f"Missing pack overrides directory: {OVERRIDES}")
    entries = []
    for path in OVERRIDES.rglob("*"):
        if not path.is_file():
            continue
        relative = path.relative_to(OVERRIDES).as_posix()
        if is_dolphin_mod(relative):
            raise SystemExit(f"{relative} is checked in; the build supplies the DolphinClient jar")
        if relative in modrinth_paths:
            raise SystemExit(f"{relative} is a Modrinth download and is also bundled")
        entries.append((relative, path.read_bytes()))
    return entries


def build(output_path: Path, dolphin_jar: Path) -> str:
    component_patches = patches()
    check_components(component_patches)
    files = modrinth_files()
    version_id = dolphin_version(dolphin_jar.name)
    entries = override_files({entry["path"] for entry in files})
    if not any(relative.startswith("mods/") and relative.endswith(".jar") for relative, _ in entries):
        raise SystemExit("pack/overrides has no mods to bundle")
    entries.append((f"mods/{dolphin_jar.name}", dolphin_jar.read_bytes()))
    entries.sort()

    output_path.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(output_path, "w") as pack:
        # Must be the first entry Prism's importer inspects.
        pack.writestr(
            "modrinth.index.json",
            json.dumps(index_document(version_id, files), indent=2) + "\n",
        )
        pack.writestr("instance.cfg", (ROOT / "instance.cfg").read_bytes())
        for uid, data in component_patches.items():
            pack.writestr(
                f"patches/{uid}.json",
                json.dumps(data, indent=4) + "\n",
            )
        for relative, payload in entries:
            pack.writestr(f"overrides/{relative}", payload)
    return version_id


def check_update_metadata(pack: zipfile.ZipFile, files: list[dict]) -> None:
    """Prism only offers Ornithe updates when the mod index records that loader."""
    indexes = [pack.read(name).decode() for name in pack.namelist() if name.endswith(".pw.toml")]
    for entry in files:
        filename = entry["path"].rsplit("/", 1)[-1]
        matches = [text for text in indexes if f'filename = "{filename}"' in text]
        if len(matches) != 1:
            raise SystemExit(f"Expected one Prism metadata file for {filename}, found {len(matches)}")
        text = matches[0]
        if entry["hashes"]["sha512"] not in text or "[update.modrinth]" not in text:
            raise SystemExit(f"Prism metadata for {filename} is missing the Modrinth hash or project")
        if entry["path"].startswith("mods/") and '"ornithe"' not in text:
            raise SystemExit(f"Prism metadata for {filename} does not record the Ornithe loader")
        if "x-prismlauncher-lock-update = true" in text:
            raise SystemExit(f"Prism metadata for {filename} locks updates")


def check_pack(path: Path, dolphin_jar: Path) -> None:
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
        expected_files = modrinth_files()
        if index["files"] != expected_files:
            raise SystemExit("Index files do not match pack/modrinth-files.json")
        check_update_metadata(pack, expected_files)
        modrinth_paths = {f"overrides/{entry['path']}" for entry in expected_files}
        if any(name in modrinth_paths for name in names):
            raise SystemExit("A Modrinth download is also bundled in overrides")
        bundled = [name for name in names if name.startswith("overrides/mods/") and name.endswith(".jar")]
        third_party = sorted(
            Path(name).name
            for name in bundled
            if not is_dolphin_mod(name.removeprefix("overrides/"))
        )
        if third_party != ["Zirconium-0.2.0+mc1.8.9.jar", "nettyfix-1.1.0+1.8.9.jar"]:
            raise SystemExit(f"Unexpected bundled mods: {third_party}")
        dolphin_mods = [name for name in bundled if is_dolphin_mod(name.removeprefix("overrides/"))]
        if len(dolphin_mods) != 1:
            raise SystemExit(f"Expected one DolphinClient jar, found {dolphin_mods}")
        if Path(dolphin_mods[0]).name != dolphin_jar.name:
            raise SystemExit(f"Pack jar {dolphin_mods[0]} != {dolphin_jar.name}")
        if pack.read(dolphin_mods[0]) != dolphin_jar.read_bytes():
            raise SystemExit("Pack DolphinClient jar does not match --dolphin-jar")
        if index["versionId"] != dolphin_version(dolphin_jar.name):
            raise SystemExit("Pack versionId does not match --dolphin-jar")
        if any(name.endswith("usercache.json") or name.endswith("accounts.json") for name in names):
            raise SystemExit("Personal account files were copied into the pack")
        # 1.8.9 saves this list bottom-to-top, so the last name is the top of the resource pack screen.
        options_line = next(
            line.split(":", 1)[1]
            for line in pack.read("overrides/options.txt").decode().splitlines()
            if line.startswith("resourcePacks:")
        )
        resource_packs = json.loads(options_line)
        expected_packs = [
            "-      §fp§drism §8[§f16§dx§8].zip",
            "xD Overlay.zip",
        ]
        if resource_packs != expected_packs:
            raise SystemExit(f"Resource pack order is {resource_packs}")
        # All four variants must be installed, even though only Rose is selected.
        expected_downloads = {
            "resourcepacks/-      §fp§9rism §8[§f16§9x§8].zip",
            "resourcepacks/-      §fp§7rism §8[§f16§7x§8].zip",
            "resourcepacks/-      §fp§dri§bsm §8[§f16§3x§8].zip",
            "resourcepacks/-      §fp§drism §8[§f16§dx§8].zip",
        }
        resource_downloads = {
            entry["path"] for entry in expected_files if entry["path"].startswith("resourcepacks/")
        }
        if resource_downloads != expected_downloads:
            raise SystemExit(f"Unexpected resource pack downloads: {resource_downloads}")
        if "overrides/resourcepacks/xD Overlay.zip" not in names:
            raise SystemExit("xD Overlay is missing from the pack")
        if any("Zahry" in name for name in names):
            raise SystemExit("Zahry is still in the pack")
        intermediary = json.loads(pack.read("patches/net.fabricmc.intermediary.json"))
        library_names = [lib["name"] for lib in intermediary["libraries"]]
        if "net.ornithemc:calamus-intermediary-gen2:1.8.9" not in library_names:
            raise SystemExit("Built pack lost the Calamus intermediary")
        required = {req["uid"] for req in intermediary["requires"]}
        for uid in required:
            patch_name = f"patches/{uid}.json"
            if uid != "net.minecraft" and patch_name not in names:
                raise SystemExit(f"Missing {patch_name}")
        print(
            f"ok {path} version={index['versionId']} bundled={len(bundled)} "
            f"modrinth={len(expected_files)} "
            f"components={sum(1 for n in names if n.startswith('patches/'))}"
        )


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--dolphin-jar", type=Path, required=True, help="DolphinClient jar from this build")
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--check", action="store_true", help="Validate the written .mrpack")
    args = parser.parse_args()
    if not args.dolphin_jar.is_file():
        raise SystemExit(f"DolphinClient jar not found: {args.dolphin_jar}")
    version = build(args.output, args.dolphin_jar)
    print(f"wrote {args.output} ({version})")
    if args.check:
        check_pack(args.output, args.dolphin_jar)


if __name__ == "__main__":
    main()
