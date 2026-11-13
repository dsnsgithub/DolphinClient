# Release JAR packing

`compact-jar.gradle.kts` runs at the end of `:1.8.9:remapJar`, after waiting for
Loom's remapping worker. Normal builds, collected builds, and publishing all use
the resulting archive. No build tool on the host machine is required; the script
pins its two helper libraries itself (`org.tukaani:xz:1.10`,
`com.github.luccappellaro:jzopfli:0.0.4`, both pure Java, Apache-2.0).

Loom stores the entries of nested dependencies without compression, then
compresses each nested JAR inside the release JAR. Multi-release dependencies
such as junixsocket contain similar classes for several Java versions. Sorting
by `META-INF/versions/<version>/...` puts these variants too far apart to share
the 32 KiB DEFLATE dictionary.

The packing action rewrites the archive without changing a single path or
payload:

- Every nested dependency JAR, at any depth, is re-ordered so version variants
  of the same class sit next to each other, and its entries are stored
  uncompressed so the outer DEFLATE stream can compress the raw bytes.
- Bundled `.tar.xz` payloads (the SVG renderer's `libraries.tar.xz`) are
  re-compressed with an x86 BCJ + LZMA2/9e filter chain, and only used after
  verifying they extract to byte-identical tar content.
- Every entry is compressed with whichever is smaller of DEFLATE level 9 and a
  Zopfli stream (Zopfli is skipped for data that does not compress, to bound
  build time). Each stream is verified to inflate back to the original bytes
  before it is written.
- Entry paths, payloads, manifest order, and Multi-Release manifests are
  preserved, including all Java-version variants, platform natives, licenses,
  and service descriptors. Only a smaller result replaces Loom's output, and
  temporary files are cleaned up on failure. `dolphin.compact.jar.iterations`
  tunes the Zopfli iteration count (default 15).

The 2.1.0+1.8.9 release measured 11,946,011 bytes before packing and 10,642,877
bytes afterward (10.9% smaller). Validation compared all 2,911 effective
entries recursively against the unpacked archive, checked Java 17/21/25
multi-release class selection at all 21 nested jar levels, streamed manifests
through `JarInputStream`, and exercised the packaged junixsocket native library
on Linux with Java 21 and 25. A clean `./gradlew build` twice in a row produced
SHA-256-identical archives and ran the existing project tests and license
checks.
