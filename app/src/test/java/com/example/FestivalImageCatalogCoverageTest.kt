package com.example

import com.example.data.catalog.BundledCatalog
import com.example.data.remote.OnlineFestivalDiscoveryService
import com.example.ui.components.FestivalPhotographicRegistry
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FestivalImageCatalogCoverageTest {

    private fun resolveFile(path: String): File {
        val direct = File(path)
        if (direct.exists()) return direct
        val sub = File("app", path)
        if (sub.exists()) return sub
        val stripped = if (path.startsWith("app/")) File(path.substringAfter("app/")) else direct
        if (stripped.exists()) return stripped
        return direct
    }

    private data class ManifestEntry(
        val canonicalFestivalId: String,
        val festivalName: String,
        val imageAssetPath: String,
        val drawableResName: String,
        val imageFormat: String,
        val sourceUrl: String,
        val licence: String,
        val imageStatus: String
    )

    private fun parseManifestEntries(jsonStr: String): List<ManifestEntry> {
        val list = mutableListOf<ManifestEntry>()
        val blockRegex = Regex("""\{[^}]+\}""")
        fun extractField(block: String, key: String): String {
            val r = Regex(""""$key"\s*:\s*"([^"]*)"""")
            return r.find(block)?.groupValues?.get(1) ?: ""
        }

        for (match in blockRegex.findAll(jsonStr)) {
            val block = match.value
            val cid = extractField(block, "canonicalFestivalId")
            if (cid.isNotBlank()) {
                list.add(
                    ManifestEntry(
                        canonicalFestivalId = cid,
                        festivalName = extractField(block, "festivalName"),
                        imageAssetPath = extractField(block, "imageAssetPath"),
                        drawableResName = extractField(block, "drawableResName"),
                        imageFormat = extractField(block, "imageFormat"),
                        sourceUrl = extractField(block, "sourceUrl"),
                        licence = extractField(block, "licence"),
                        imageStatus = extractField(block, "imageStatus")
                    )
                )
            }
        }
        return list
    }

    /**
     * Validates whether raw file bytes represent a genuine raster photograph (WebP, JPEG, or PNG)
     * and explicitly rejects XML vectors, SVGs, HTML error pages, and tiny/corrupted files.
     */
    private fun isGenuineRasterPhotograph(bytes: ByteArray): Boolean {
        if (bytes.size < 4096) return false
        val headAscii = String(bytes, 0, minOf(bytes.size, 256), Charsets.ISO_8859_1).trimStart().lowercase()
        if (headAscii.startsWith("<?xml") ||
            headAscii.startsWith("<vector") ||
            headAscii.startsWith("<svg") ||
            headAscii.startsWith("<!doctype") ||
            headAscii.startsWith("<html") ||
            headAscii.contains("<vector") ||
            headAscii.contains("<svg")
        ) {
            return false
        }

        // Check WebP magic: "RIFF" + 4-byte size + "WEBP" + "VP8 " / "VP8L" / "VP8X"
        val isWebP = bytes.size > 30 &&
            bytes[0] == 'R'.code.toByte() &&
            bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte() &&
            bytes[3] == 'F'.code.toByte() &&
            bytes[8] == 'W'.code.toByte() &&
            bytes[9] == 'E'.code.toByte() &&
            bytes[10] == 'B'.code.toByte() &&
            bytes[11] == 'P'.code.toByte()

        // Check JPEG magic: FF D8 FF
        val isJpeg = bytes.size > 16 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xD8.toByte() &&
            bytes[2] == 0xFF.toByte()

        // Check PNG magic: 89 50 4E 47 0D 0A 1A 0A
        val isPng = bytes.size > 24 &&
            bytes[0] == 0x89.toByte() &&
            bytes[1] == 0x50.toByte() &&
            bytes[2] == 0x4E.toByte() &&
            bytes[3] == 0x47.toByte()

        return isWebP || isJpeg || isPng
    }

    /**
     * Extracts exact width and height from WebP VP8 / VP8L / VP8X bitstream headers.
     */
    private fun decodeWebPDimensions(bytes: ByteArray): Pair<Int, Int>? {
        if (bytes.size < 30) return null
        val chunkType = String(bytes, 12, 4, Charsets.US_ASCII)
        return when (chunkType) {
            "VP8 " -> {
                // Lossy VP8 frame header starts at offset 20; 3-byte frame tag + 3-byte start code (9D 01 2A) + 2-byte width + 2-byte height
                if (bytes[23] == 0x9D.toByte() && bytes[24] == 0x01.toByte() && bytes[25] == 0x2A.toByte()) {
                    val w = ByteBuffer.wrap(bytes, 26, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt() and 0x3FFF
                    val h = ByteBuffer.wrap(bytes, 28, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt() and 0x3FFF
                    Pair(w, h)
                } else null
            }
            "VP8L" -> {
                // Lossless VP8L starts at offset 20 with signature 0x2F, followed by 4 bytes of packed width/height
                if (bytes[20] == 0x2F.toByte()) {
                    val bits = ByteBuffer.wrap(bytes, 21, 4).order(ByteOrder.LITTLE_ENDIAN).int
                    val w = (bits and 0x3FFF) + 1
                    val h = ((bits ushr 14) and 0x3FFF) + 1
                    Pair(w, h)
                } else null
            }
            "VP8X" -> {
                val w = (bytes[24].toInt() and 0xFF) or ((bytes[25].toInt() and 0xFF) shl 8) or ((bytes[26].toInt() and 0xFF) shl 16)
                val h = (bytes[27].toInt() and 0xFF) or ((bytes[28].toInt() and 0xFF) shl 8) or ((bytes[29].toInt() and 0xFF) shl 16)
                Pair(w + 1, h + 1)
            }
            else -> null
        }
    }

    @Test
    fun testXmlVectorsAndNonPhotosAreStrictlyRejected() {
        val fakeXmlVector = """
            <?xml version="1.0" encoding="utf-8"?>
            <vector xmlns:android="http://schemas.android.com/apk/res/android"
                android:width="800dp" android:height="500dp"
                android:viewportWidth="800" android:viewportHeight="500">
                <path android:fillColor="#FF0000" android:pathData="M0,0h800v500h-800z"/>
            </vector>
        """.trimIndent().repeat(15).toByteArray()
        assertFalse("XML vector drawables must NEVER be counted as photographs", isGenuineRasterPhotograph(fakeXmlVector))

        val fakeSvg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"800\" height=\"500\"></svg>".repeat(80).toByteArray()
        assertFalse("SVG files must NEVER be counted as photographs", isGenuineRasterPhotograph(fakeSvg))

        val fakeHtml = "<!DOCTYPE html><html><body>404 Not Found</body></html>".repeat(100).toByteArray()
        assertFalse("HTML error pages must NEVER be counted as photographs", isGenuineRasterPhotograph(fakeHtml))

        // Verify no img_*.xml vector illustrations remain in res/drawable or res/drawable-nodpi
        val drawableDir = resolveFile("src/main/res/drawable")
        val xmlFestivalFiles = drawableDir.listFiles { _, name -> name.startsWith("img_") && name.endsWith(".xml") } ?: emptyArray()
        assertEquals(
            "Zero XML vector illustrations with img_ prefix may exist in res/drawable/",
            0,
            xmlFestivalFiles.size
        )
    }

    @Test
    fun testFestivalImageManifestRegistryAgreementAndRasterDecodability() {
        val manifestFile = resolveFile("src/main/assets/festivals/festival_image_manifest.json")
        assertTrue("Manifest file must exist", manifestFile.exists())

        val entries = parseManifestEntries(manifestFile.readText())
        assertEquals("Manifest must contain all 83 canonical entries (82 festivals + welcome hero)", 83, entries.size)

        val manifestIds = entries.map { it.canonicalFestivalId }.toSet()
        val registryIds = FestivalPhotographicRegistry.getAllMappedFestivalIds()
        assertEquals("Registry and Manifest festival ID sets must match 100%", manifestIds, registryIds)

        for (entry in entries) {
            val cid = entry.canonicalFestivalId
            val dres = entry.drawableResName

            assertFalse("canonicalFestivalId must not be blank", cid.isBlank())
            assertFalse("festivalName must not be blank", entry.festivalName.isBlank())
            assertEquals("imageStatus must be verified", "verified", entry.imageStatus)
            assertEquals("imageFormat must be WEBP", "WEBP", entry.imageFormat)
            assertTrue("sourceUrl must be a valid URL", entry.sourceUrl.startsWith("https://"))
            assertFalse("licence must be recorded", entry.licence.isBlank())

            // Verify Registry agreement
            assertEquals("Registry drawable name must match manifest for $cid", dres, FestivalPhotographicRegistry.getDrawableResName(cid))
            assertFalse("Mapped festival $cid must not use fallback", FestivalPhotographicRegistry.isFallback(cid))

            val resId = FestivalPhotographicRegistry.getDrawableForFestival(cid)
            assertTrue("Registry must resolve valid non-zero resource ID for $cid", resId != 0)
            assertNotEquals("Mapped festival $cid must not resolve to fallback drawable ID", R.drawable.img_cultural_fallback, resId)

            // Verify binary photographic file on disk in res/drawable-nodpi/
            val assetFile = resolveFile("src/main/${entry.imageAssetPath}")
            assertTrue("Photographic asset file src/main/${entry.imageAssetPath} must exist for $cid", assetFile.exists())
            assertTrue("Photographic asset must be in drawable-nodpi with .webp extension", entry.imageAssetPath.endsWith(".webp"))

            val bytes = assetFile.readBytes()
            assertTrue("Asset for $cid must be a genuine raster photograph (not XML/SVG/HTML)", isGenuineRasterPhotograph(bytes))

            // Verify WebP bitstream dimensions & RIFF container payload integrity
            val dims = decodeWebPDimensions(bytes)
            assertNotNull("WebP bitstream dimensions must be decodable for $cid", dims)
            assertEquals("WebP width must be 800px for $cid", 800, dims!!.first)
            assertEquals("WebP height must be 500px for $cid", 500, dims.second)

            val riffPayloadSize = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).int
            assertEquals("RIFF container payload size + 8 must equal exact file byte length for $cid", bytes.size, riffPayloadSize + 8)
        }

        // Verify explicit fallback behavior for unknown/null festival IDs
        assertTrue("Null festival ID must be reported as fallback", FestivalPhotographicRegistry.isFallback(null))
        assertTrue("Unknown festival ID must be reported as fallback", FestivalPhotographicRegistry.isFallback("non_existent_festival"))
        assertEquals(
            "Unknown festival ID must resolve to img_cultural_fallback",
            R.drawable.img_cultural_fallback,
            FestivalPhotographicRegistry.getDrawableForFestival("non_existent_festival")
        )

        // Verify fallback asset itself is also a real decodable WebP photograph
        val fallbackFile = resolveFile("src/main/res/drawable-nodpi/img_cultural_fallback.webp")
        assertTrue("Fallback photographic asset must exist", fallbackFile.exists())
        assertTrue("Fallback asset must be a genuine raster photograph", isGenuineRasterPhotograph(fallbackFile.readBytes()))

        // Audit Bundled Catalog (28 festivals) and Encyclopedic Discovery Catalog (42 festivals)
        val bundledFestivals = BundledCatalog.FESTIVALS
        for (festival in bundledFestivals) {
            assertTrue("Bundled festival '${festival.id}' must exist in manifest", manifestIds.contains(festival.id))
            assertFalse("Bundled festival '${festival.id}' must not use fallback", FestivalPhotographicRegistry.isFallback(festival.id))
        }

        val discoveryFestivals = OnlineFestivalDiscoveryService.getAllEncyclopedicFestivals()
        for (disc in discoveryFestivals) {
            val fid = disc.entity.id
            assertTrue("Encyclopedic festival '$fid' must exist in manifest", manifestIds.contains(fid))
            assertFalse("Encyclopedic festival '$fid' must not use fallback", FestivalPhotographicRegistry.isFallback(fid))
        }

        // Verify total distinct festivals across Bundled (28) + Encyclopedic (54) = 82
        val totalCatalogIds = (bundledFestivals.map { it.id } + discoveryFestivals.map { it.entity.id }).toSet()
        assertEquals("Total distinct festivals in UtsavSetu catalog must be 82", 82, totalCatalogIds.size)

        // Verify dedicated WebP drawables for Bhai Dooj, Govardhan Puja, Durga Ashtami, and the 12 newly added festivals
        val dedicatedCheckIds = listOf(
            "bhai_dooj" to "img_bhai_dooj",
            "govardhan_puja" to "img_govardhan",
            "durga_ashtami" to "img_durga_ashtami",
            "hartalika_teej" to "img_hartalika_teej",
            "kajari_teej" to "img_kajari_teej",
            "maha_navami_ayudha_puja" to "img_maha_navami_ayudha",
            "labh_panchami" to "img_labh_panchami",
            "gopashtami" to "img_gopashtami",
            "akshaya_navami_amla_navami" to "img_akshaya_navami",
            "kalabhairava_jayanti" to "img_kalabhairava_jayanti",
            "mauni_amavasya" to "img_mauni_amavasya",
            "amalaki_ekadashi_rangbhari" to "img_amalaki_ekadashi",
            "cheti_chand_jhulelal" to "img_cheti_chand",
            "putrada_ekadashi" to "img_putrada_ekadashi",
            "hariyali_amavasya" to "img_hariyali_amavasya"
        )
        for ((festId, expectedResName) in dedicatedCheckIds) {
            assertEquals(
                "Festival $festId must resolve to its dedicated WebP photograph $expectedResName",
                expectedResName,
                FestivalPhotographicRegistry.getDrawableResName(festId)
            )
        }
    }
}
