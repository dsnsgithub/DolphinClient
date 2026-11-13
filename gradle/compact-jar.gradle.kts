import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Calendar
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.Inflater
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import javax.inject.Inject
import lu.luz.jzopfli.ZopfliH
import lu.luz.jzopfli.Zopfli_lib
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.workers.WorkerExecutor
import org.tukaani.xz.FilterOptions
import org.tukaani.xz.LZMA2Options
import org.tukaani.xz.X86Options
import org.tukaani.xz.XZ
import org.tukaani.xz.XZInputStream
import org.tukaani.xz.XZOutputStream

buildscript {
	repositories { mavenCentral() }
	dependencies {
		classpath("org.tukaani:xz:1.10")
		classpath("com.github.luccappellaro:jzopfli:0.0.4")
	}
}

// Rewrites the release JAR without changing a single path or payload:
//  - nested dependency JARs (at any depth) are re-ordered so that multi-release
//    variants of the same class sit next to each other, and their entries are
//    stored uncompressed so the outer DEFLATE stream can compress the raw bytes;
//  - bundled .tar.xz payloads are re-compressed with an x86 BCJ + LZMA2/9e
//    filter chain, verified to extract to the identical tar;
//  - every entry is compressed with whichever is smaller of DEFLATE level 9
//    and a Zopfli stream, each verified to inflate back to the original bytes.
abstract class CompactJarAction : Action<Task> {
	@get:Inject
	abstract val workerExecutor: WorkerExecutor

	private class SourceEntry(val name: String, val bytes: ByteArray, val time: Long, val isDirectory: Boolean)

	private class PackedEntry(
		val name: String, val time: Long, val isDirectory: Boolean,
		val stored: Boolean, val content: ByteArray, // raw bytes for stored, deflate stream otherwise
		val crc: Int, val rawSize: Int,
	)

	private val versionedPath = Regex("^META-INF/versions/(\\d+)/(.*)$")

	// ---- ZIP writing -----------------------------------------------------

	private fun le16(out: ByteArrayOutputStream, v: Int) { out.write(v); out.write(v shr 8) }
	private fun le32(out: ByteArrayOutputStream, v: Int) { out.write(v); out.write(v shr 8); out.write(v shr 16); out.write(v shr 24) }

	private fun dosDateTime(millis: Long): IntArray {
		val cal = Calendar.getInstance()
		cal.timeInMillis = millis
		val year = cal.get(Calendar.YEAR)
		if (year < 1980) return intArrayOf(0, 0x21)
		val time = cal.get(Calendar.SECOND) / 2 or (cal.get(Calendar.MINUTE) shl 5) or (cal.get(Calendar.HOUR_OF_DAY) shl 11)
		val date = cal.get(Calendar.DAY_OF_MONTH) or ((cal.get(Calendar.MONTH) + 1) shl 5) or ((year - 1980) shl 9)
		return intArrayOf(time, date)
	}

	private fun writeZip(entries: List<PackedEntry>): ByteArray {
		val out = ByteArrayOutputStream(entries.size * 1024)
		val offsets = IntArray(entries.size)
		val names = entries.map { it.name.toByteArray(Charsets.UTF_8) }
		entries.forEachIndexed { i, entry ->
			offsets[i] = out.size()
			le32(out, 0x04034b50)
			le16(out, 20) // version needed
			val flags = if (entry.name.any { it.code > 0x7f }) 0x0800 else 0
			le16(out, flags)
			le16(out, if (entry.stored) ZipEntry.STORED else ZipEntry.DEFLATED)
			val (dosTime, dosDate) = dosDateTime(entry.time)
			le16(out, dosTime); le16(out, dosDate)
			le32(out, entry.crc)
			if (entry.stored) { le32(out, entry.rawSize); le32(out, entry.rawSize) }
			else { le32(out, entry.content.size); le32(out, entry.rawSize) }
			le16(out, names[i].size); le16(out, 0)
			out.write(names[i])
			out.write(entry.content)
		}
		val centralOffset = out.size()
		entries.forEachIndexed { i, entry ->
			le32(out, 0x02014b50)
			le16(out, 20) // version made by
			le16(out, 20) // version needed
			val flags = if (entry.name.any { it.code > 0x7f }) 0x0800 else 0
			le16(out, flags)
			le16(out, if (entry.stored) ZipEntry.STORED else ZipEntry.DEFLATED)
			val (dosTime, dosDate) = dosDateTime(entry.time)
			le16(out, dosTime); le16(out, dosDate)
			le32(out, entry.crc)
			if (entry.stored) { le32(out, entry.rawSize); le32(out, entry.rawSize) }
			else { le32(out, entry.content.size); le32(out, entry.rawSize) }
			le16(out, names[i].size); le16(out, 0); le16(out, 0)
			le16(out, 0) // disk number
			le16(out, 0) // internal attributes
			le32(out, 0)
			le32(out, offsets[i])
			out.write(names[i])
		}
		val centralSize = out.size() - centralOffset
		le32(out, 0x06054b50)
		le16(out, 0); le16(out, 0)
		le16(out, entries.size); le16(out, entries.size)
		le32(out, centralSize); le32(out, centralOffset)
		le16(out, 0)
		return out.toByteArray()
	}

	// ---- compression candidates ------------------------------------------

	private fun deflate9(data: ByteArray): ByteArray {
		val deflater = Deflater(Deflater.BEST_COMPRESSION, true)
		deflater.setInput(data)
		deflater.finish()
		val out = ByteArrayOutputStream()
		val buffer = ByteArray(65536)
		while (!deflater.finished()) out.write(buffer, 0, deflater.deflate(buffer))
		deflater.end()
		return out.toByteArray()
	}

	private fun zopfliDeflate(data: ByteArray, iterations: Int): ByteArray {
		val options = ZopfliH.ZopfliOptions()
		options.numiterations = iterations
		options.blocksplitting = true
		options.blocksplittingmax = 15
		val out = arrayOfNulls<ByteArray>(1)
		val outLen = IntArray(1)
		Zopfli_lib.ZopfliCompress(options, ZopfliH.ZopfliFormat.ZOPFLI_FORMAT_DEFLATE, data, data.size, out, outLen)
		return out[0]!!.copyOf(outLen[0])
	}

	/** Fails loudly if a candidate stream does not inflate back to [data]. */
	private fun verifyDeflate(stream: ByteArray, data: ByteArray) {
		val inflater = Inflater(true)
		try {
			inflater.setInput(stream)
			val back = ByteArray(data.size)
			val n = inflater.inflate(back)
			if (n != data.size || !inflater.finished() || !back.contentEquals(data)) {
				error("deflate stream failed round-trip verification")
			}
		} finally {
			inflater.end()
		}
	}

	// ---- payload transforms ----------------------------------------------

	/** Re-compresses a .tar.xz payload with x86 BCJ + LZMA2/9e; returns null unless it extracts to identical tar bytes. */
	private fun repackTarXz(data: ByteArray): ByteArray? {
		val original = try {
			XZInputStream(ByteArrayInputStream(data)).use { it.readAllBytes() }
		} catch (e: Exception) {
			return null
		}
		val packed = ByteArrayOutputStream(data.size)
		try {
			val lzma = LZMA2Options()
			lzma.setPreset(9)
			XZOutputStream(packed, arrayOf<FilterOptions>(X86Options(), lzma), XZ.CHECK_CRC32).use { xz ->
				xz.write(original)
			}
		} catch (e: Exception) {
			return null
		}
		val bytes = packed.toByteArray()
		if (!XZInputStream(ByteArrayInputStream(bytes)).use { it.readAllBytes() }.contentEquals(original)) return null
		return if (bytes.size < data.size) bytes else null
	}

	private fun rewriteNestedJar(bytes: ByteArray): ByteArray {
		val sources = mutableListOf<SourceEntry>()
		ZipInputStream(ByteArrayInputStream(bytes)).use { input ->
			while (true) {
				val entry = input.nextEntry ?: break
				val raw = input.readAllBytes()
				val transformed = when {
					entry.isDirectory -> raw
					entry.name.startsWith("META-INF/jars/") && entry.name.endsWith(".jar") ->
						rewriteNestedJar(raw)
					entry.name.endsWith(".tar.xz") -> repackTarXz(raw) ?: raw
					else -> raw
				}
				sources.add(SourceEntry(entry.name, transformed, entry.time, entry.isDirectory))
			}
		}
		// Manifest first, then version variants of the same path adjacent so the
		// outer DEFLATE stream can share dictionary matches across them.
		val ordered = sources.sortedWith(compareBy<SourceEntry>(
			{ if (it.name == "META-INF/MANIFEST.MF") 0 else 1 },
			{ versionedPath.matchEntire(it.name)?.groupValues?.get(2) ?: it.name },
			{ versionedPath.matchEntire(it.name)?.groupValues?.get(1)?.toInt() ?: 0 },
			{ it.name },
		))
		return writeZip(ordered.map {
			PackedEntry(it.name, it.time, it.isDirectory, stored = true, content = it.bytes,
				crc = CRC32().apply { update(it.bytes) }.value.toInt(), rawSize = it.bytes.size)
		})
	}

	// ---- entry packing ----------------------------------------------------

	private fun pack(source: SourceEntry, iterations: Int): PackedEntry {
		var raw = source.bytes
		if (source.isDirectory) {
			return PackedEntry(source.name, source.time, true, stored = true, content = ByteArray(0), crc = 0, rawSize = 0)
		}
		if (source.name.startsWith("META-INF/jars/") && source.name.endsWith(".jar")) {
			raw = rewriteNestedJar(raw)
		} else if (source.name.endsWith(".tar.xz")) {
			raw = repackTarXz(raw) ?: raw
		}
		if (raw.isEmpty()) {
			return PackedEntry(source.name, source.time, false, stored = true, content = raw, crc = 0, rawSize = raw.size)
		}
		val crc = CRC32().apply { update(raw) }.value.toInt()
		val deflated = deflate9(raw)
		var stream = deflated
		// Zopfli only pays off on data that compresses; skip near-incompressible
		// payloads (xz archives, already-compressed blobs) to bound build time.
		if (raw.size >= 512 && deflated.size < raw.size * 19 / 20) {
			try {
				val zopfli = zopfliDeflate(raw, iterations)
				if (zopfli.size < stream.size) stream = zopfli
			} catch (e: Throwable) {
				// fall back to the DEFLATE level 9 stream below
			}
		}
		try {
			verifyDeflate(stream, raw)
		} catch (e: Throwable) {
			if (stream !== deflated) { verifyDeflate(deflated, raw); stream = deflated }
		}
		return PackedEntry(source.name, source.time, false, stored = false, content = stream, crc, rawSize = raw.size)
	}

	override fun execute(task: Task) {
		// Loom submits remapping to a worker; finish it before touching its output.
		workerExecutor.await()
		val archive = (task as AbstractArchiveTask).archiveFile.get().asFile.toPath()
		val before = Files.size(archive)
		val iterations = (task.project.findProperty("dolphin.compact.jar.iterations") as String?)?.toIntOrNull() ?: 15

		val sources = mutableListOf<SourceEntry>()
		ZipFile(archive.toFile()).use { zip ->
			val order = zip.entries().asSequence().toList()
			for (entry in order) {
				val bytes = if (entry.isDirectory) ByteArray(0) else zip.getInputStream(entry).readAllBytes()
				sources.add(SourceEntry(entry.name, bytes, entry.time, entry.isDirectory))
			}
		}

		val pool = Executors.newFixedThreadPool(
			Runtime.getRuntime().availableProcessors().coerceAtMost(8),
		)
		val packed: List<PackedEntry> = try {
			pool.invokeAll(sources.map { source -> Callable { pack(source, iterations) } }).map { it.get() }
		} finally {
			pool.shutdownNow()
		}

		val compact = Files.createTempFile(archive.parent, "compact-", ".jar")
		try {
			Files.write(compact, writeZip(packed))
			val after = Files.size(compact)
			if (after < before) {
				Files.move(compact, archive, StandardCopyOption.REPLACE_EXISTING)
				task.logger.lifecycle("Compacted ${archive.fileName}: $before -> $after bytes (saved ${before - after})")
			} else {
				task.logger.lifecycle("Compaction did not shrink ${archive.fileName} ($before -> $after); keeping original")
			}
		} finally {
			Files.deleteIfExists(compact)
		}
	}
}

tasks.named<AbstractArchiveTask>("remapJar") {
	doLast(objects.newInstance<CompactJarAction>())
}
