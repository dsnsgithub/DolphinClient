# Rendering regression checks

These checks run inside the development client so they exercise real Minecraft
models and applied mixins. They are opt-in and are not packaged in the release JAR.
They exit the client automatically; use a development run directory, not a live
Minecraft session.

```sh
./gradlew :1.8.9:build
DOLPHIN_RENDER_TESTS=1 ./gradlew :1.8.9:runClient
DOLPHIN_BENCHMARK=1 ./gradlew :1.8.9:runClient
```

A working OpenGL display is required. On a headless Linux machine, set `DISPLAY`
to an Xorg/Xvfb display. `LP_NUM_THREADS=1` makes Mesa software-renderer comparisons
less sensitive to its thread scheduling. Run comparisons sequentially, with the
same JVM, renderer, resource packs and configuration.

## Regression coverage

`OLD_ANIMATIONS_REGRESSION_TESTS PASSED` confirms:

- South-face filtering preserves quad identity, order and tint layers without
  mutating source geometry; empty results are cached too.
- Repeated renders reuse the filtered list. Replacing the source list invalidates
  it. Dynamic model subclasses bypass caching, including in-place list changes.
- Wooden and iron trapdoor models switch correctly under both the individual and
  master toggles, without rebaking resources.
- Effective potion-color changes invalidate the cache exactly when needed, and
  the real potion lookup restores the expected old/vanilla leaping color.
- An explicit resource reload replaces both baked models and their sprite caches.
- The enabled mod skips vanilla glint geometry passes for GUI items and blocks;
  disabling it restores both vanilla passes in GUI and held-item rendering.
- The batched GUI overlay matches the previous renderer pixel-for-pixel at five
  animation phases, produces visible output, and restores depth state.

The filter microbenchmark compares the exact previous implementation against the
new cache using synthetic 66-, 258- and 1,026-quad models. After 20,000 warmup calls,
it measures 100,000 calls per implementation, retains results in a volatile field,
and asserts that cache-hit allocation is less than 1% of the old allocation.
Its timings are not a world-FPS measurement.

## Render comparison

`RenderBenchmark` renders batches of nine items through the actual GUI and held
item paths. Each case has 1,000 warmup batches and 1,000 measured batches. It waits
for OpenGL completion and reports median/p95/p99 batch time, render-thread
allocation and GL errors. Cases include plain/enchanted swords, plain/enchanted
blocks, and skulls. The GUI cases exercise the same item helper as the hotbar.

For comparisons against an older revision, copy `RenderBenchmark.java` to that
revision's test source set and add this opt-in line after bridge validation in
`DolphinClientTest.onInitializeClient()`:

```java
if (System.getenv("DOLPHIN_BENCHMARK") != null) RenderBenchmark.install();
```

This benchmark does not simulate a populated multiplayer world, the Dolphin
Optimized modpack, or the user's GPU. Use an identical world/camera, resource pack
and modpack on real hardware to verify the reported gameplay FPS regression.

## Measurements for the PR #28 follow-up

Tested with JDK 25, Mesa llvmpipe, one rendering worker, default resources, and
1.7 Animations enabled. PR #28 was `dd2ce3fb0`; its base was `591f42dd5`.
The production fix retains the pre-refactor GUI overlay and absence of block
item glint while the mod is enabled; it does not restore the removed settings.

| Nine-item GUI batch | PR #28 allocation | Fixed allocation |
| --- | ---: | ---: |
| Plain swords | 2,880 B | 720 B |
| Enchanted swords | 7,848 B | 792 B |

The enchanted-block median fell from 395.21 µs to 115.70 µs per nine-item batch
when the two extra glint passes were removed (about 71%). Other timing results
were noisy, so this is not a claim of a general FPS multiplier. The sprite-filter
microbenchmark measured 304 / 1,072 / 4,144 B per call before the fix and 0 B on
warm cache hits for the three synthetic model sizes. Toggling settings preserved
baked-model identities; an explicit resource reload replaced them as expected.
