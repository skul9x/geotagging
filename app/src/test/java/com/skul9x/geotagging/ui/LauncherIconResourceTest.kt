package com.skul9x.geotagging.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class LauncherIconResourceTest {

    private val resDir = File("src/main/res")

    @Test
    fun testVectorDrawablesExistAndValid() {
        val vectorFiles = listOf(
            "drawable/ic_launcher_background.xml",
            "drawable/ic_launcher_foreground.xml",
            "drawable/ic_launcher_monochrome.xml"
        )

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()

        for (relativePath in vectorFiles) {
            val file = File(resDir, relativePath)
            assertTrue("Resource file missing: ${file.path}", file.exists())
            assertTrue("Resource file empty: ${file.path}", file.length() > 0)

            val doc = builder.parse(file)
            assertEquals(
                "Root element of ${file.name} must be vector",
                "vector",
                doc.documentElement.tagName
            )
        }
    }

    @Test
    fun testAdaptiveIconConfigsExistAndValid() {
        val configFiles = listOf(
            "mipmap-anydpi-v26/ic_launcher.xml",
            "mipmap-anydpi-v26/ic_launcher_round.xml"
        )

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()

        for (relativePath in configFiles) {
            val file = File(resDir, relativePath)
            assertTrue("Config file missing: ${file.path}", file.exists())
            assertTrue("Config file empty: ${file.path}", file.length() > 0)

            val doc = builder.parse(file)
            assertEquals(
                "Root element of ${file.name} must be adaptive-icon",
                "adaptive-icon",
                doc.documentElement.tagName
            )

            val children = doc.documentElement.childNodes
            var hasBackground = false
            var hasForeground = false
            var hasMonochrome = false

            for (i in 0 until children.length) {
                val node = children.item(i)
                when (node.nodeName) {
                    "background" -> hasBackground = true
                    "foreground" -> hasForeground = true
                    "monochrome" -> hasMonochrome = true
                }
            }

            assertTrue("Missing <background> in ${file.name}", hasBackground)
            assertTrue("Missing <foreground> in ${file.name}", hasForeground)
            assertTrue("Missing <monochrome> in ${file.name}", hasMonochrome)
        }
    }
}
