package org.openrndr.plugin.intellij.editor

import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor
import com.intellij.util.ui.ColorIcon
import com.intellij.util.ui.ColorsIcon
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.idea.KotlinFileType
import org.openrndr.color.*
import org.openrndr.plugin.intellij.editor.ColorRGBaColorProvider.Companion.toAWTColor
import java.awt.Color

class ColorRGBaColorProviderTest : BasePlatformTestCase() {

    override fun getProjectDescriptor(): LightProjectDescriptor = PROJECT_DESCRIPTOR

    private fun assertGutterIconColor(
        expected: Color, @Language("kt", prefix = IMPORTS_PREFIX, suffix = "}") colorRGBaColor: String
    ) = assertGutterIconColorManual(expected, colorRGBaExpressionTemplate(colorRGBaColor))

    private fun assertGutterIconColorManual(expected: Color, code: String) {
        myFixture.configureByText(KotlinFileType.INSTANCE, code)
        val gutterMarks = myFixture.findAllGutters()
        assertTrue(gutterMarks.size == 1)
        val actualColorIcon = gutterMarks[0].icon as? ColorIcon
        assertEquals(expected, actualColorIcon?.iconColor)
    }

    /**
     * @param expected Left-to-right, top-to-bottom order of [Color]s expected in the gutter for a single line
     */
    private fun assertGutterIconMultiColor(
        expected: Array<Color>, @Language("kt", prefix = IMPORTS_PREFIX, suffix = "}") code: String
    ) = assertGutterIconMultiColorManual(expected, colorRGBaExpressionTemplate(code))

    /**
     * @param expected Left-to-right, top-to-bottom order of [Color]s expected in the gutter for a single line
     */
    private fun assertGutterIconMultiColorManual(
        expected: Array<Color>, code: String
    ) {
        myFixture.configureByText(KotlinFileType.INSTANCE, code)
        val gutterMarks = myFixture.findAllGutters()
        assertTrue(gutterMarks.size == 1)
        val expectedColorsIcon = ColorsIcon(12, *expected.reversedArray())
        val actualColorsIcon = gutterMarks[0].icon as? ColorsIcon
        assertEquals(expectedColorsIcon, actualColorsIcon)
    }

    fun testColorRGBaStatic() {
        assertGutterIconColor(ColorRGBa.RED.toAWTColor(), "ColorRGBa.RED")
        assertGutterIconColor(ColorRGBa.BLUE.toAWTColor(), "ColorRGBa.BLUE")
        // TODO: This always fails resolveToCall
        // assertGutterIconColor(ColorRGBa.HOT_PINK.toAWTColor(), "ColorRGBa.HOT_PINK")
    }

    fun testColorRGBaStaticColorsInline() {
        val expectedColors = arrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
        val code = colorRGBaExpressionTemplate("ColorRGBa.RED; ColorRGBa.GREEN; ColorRGBa.BLUE; ColorRGBa.YELLOW")
        assertGutterIconMultiColorManual(expectedColors, code)
    }

    fun testColorRGBaConstructor() {
        assertGutterIconColor(
            ColorRGBa(1.0, 0.4, 0.2, 1.0, Linearity.LINEAR).toAWTColor(),
            "ColorRGBa(1.0, 0.4, 0.2, 1.0, Linearity.LINEAR)"
        )
        assertGutterIconColor(ColorRGBa(1.0, 0.4, 0.2, 1.0).toAWTColor(), "ColorRGBa(1.0, 0.4, 0.2, 1.0)")
    }

    fun testColorRGBaFromHex() {
        assertGutterIconColor(ColorRGBa.MAGENTA.toAWTColor(), "ColorRGBa.fromHex(\"#f0f\")")
        assertGutterIconColor(ColorRGBa.MAGENTA.toAWTColor(), "ColorRGBa.fromHex(\"#ff00ff\")")
        assertGutterIconColor(ColorRGBa.fromHex("#ff00ff7f").toAWTColor(), "ColorRGBa.fromHex(\"#ff00ff7f\")")
        assertGutterIconColor(ColorRGBa.fromHex("#3037").toAWTColor(), "ColorRGBa.fromHex(\"#3037\")")
        assertGutterIconColor(ColorRGBa.YELLOW.toAWTColor(), "ColorRGBa.fromHex(0xffff00)")
    }

    fun testColorRGBaShorthand() {
        assertGutterIconColor(rgb(0.1, 0.9, 0.3, 1.0).toAWTColor(), "rgb(0.1, 0.9, 0.3)")
        assertGutterIconColor(rgb(1.0, 1.0, 1.0, 0.1).toAWTColor(), "rgb(1.0, 0.1)")
        assertGutterIconColor(rgb("#0ff").toAWTColor(), "rgb(\"#0ff\")")
    }

    fun testColorRGBaConstructorWithConst() {
        @Language("kt")
        val prelude = """
            const val X = 0.3
            val a = 0.1
            val b = a + X
        """.trimMargin()
        assertGutterIconColorManual(
            ColorRGBa(0.7, 0.3, 0.2, 1.0).toAWTColor(), colorRGBaExpressionTemplate(prelude, "ColorRGBa(0.7, X, 0.2)")
        )
        assertGutterIconColorManual(
            ColorRGBa(0.3, 0.1, 0.2, 1.0).toAWTColor(), colorRGBaExpressionTemplate(prelude, "ColorRGBa(0.3, a, 0.2)")
        )
        assertGutterIconColorManual(
            ColorRGBa(0.6, 0.4, 0.1, 1.0).toAWTColor(), colorRGBaExpressionTemplate(prelude, "ColorRGBa(0.6, b, a)")
        )
    }

    fun testColorHSVaConstructor() {
        assertGutterIconColor(ColorHSVa(300.0, 0.35, 0.9, 0.4).toAWTColor(), "ColorHSVa(300.0, 0.35, 0.9, 0.4)")
    }

    fun testColorHSVaShorthand() {
        assertGutterIconColor(hsv(303.5, 0.07, 0.8).toAWTColor(), "hsv(303.5, 0.07, 0.8)")
        assertGutterIconColor(hsv(89.3, 0.13, 0.9, 0.95).toAWTColor(), "hsv(89.3, 0.13, 0.9, 0.95)")
    }

    fun testColorRGBaNamedArguments() {
        assertGutterIconColor(ColorRGBa(0.1, b = 0.8, g = 0.1).toAWTColor(), "ColorRGBa(0.1, b = 0.8, g = 0.1)")
        assertGutterIconColor(ColorRGBa(0.3, g = 0.7, b = 0.1).toAWTColor(), "ColorRGBa(0.3, g = 0.7, b = 0.1)")
    }

    fun testColorRGBaFromHexRenamedImport() {
        @Language("kt")
        val prelude = """
            import org.openrndr.color.ColorRGBa.Companion.fromHex as hex
        """.trimMargin()
        assertGutterIconColorManual(
            ColorRGBa.fromHex("#ff007f").toAWTColor(), colorRGBaExpressionTemplate(prelude, "hex(\"#ff007f\")")
        )
    }

    fun testColorLABaWithRef() {
        assertGutterIconMultiColor(
            arrayOf(
                ColorLABa(73.233, 20.105, 67.223, alpha = 0.812, ref = ColorXYZa.SO10_UL3000).toAWTColor(),
                ColorXYZa.SO10_UL3000.toAWTColor()
            ), "ColorLABa(73.233, 20.105, 67.223, alpha = 0.812, ref = ColorXYZa.SO10_UL3000)"
        )
        assertGutterIconMultiColor(
            arrayOf(
                ColorLABa(73.233, 20.105, 67.223, alpha = 0.812, ref = ColorXYZa.SO10_D50).toAWTColor(),
                ColorXYZa.SO10_D50.toAWTColor()
            ), "ColorLABa(73.233, 20.105, 67.223, alpha = 0.812, ref = ColorXYZa.SO10_D50)"
        )
        assertGutterIconColor(
            ColorLABa(73.233, 20.105, 67.223, alpha = 0.812).toAWTColor(),
            "ColorLABa(73.233, 20.105, 67.223, alpha = 0.812)"
        )
    }

    fun testColorOKLABa() {
        // TODO: This always fails resolveToCall
        // assertGutterIconColor(ColorOKLABa(300.0, 0.35, 0.9, 0.4).toAWTColor(), "ColorOKLABa(300.0, 0.35, 0.9, 0.4)")
    }

    companion object {
        // You can specify Maven local dependencies here as well
        private val PROJECT_DESCRIPTOR = DefaultLightProjectDescriptor(
            { IdeaTestUtil.getMockJdk17() },
            listOf("org.openrndr:openrndr-color:0.5.1-SNAPSHOT", "org.openrndr.extra:orx-color:0.5.1-SNAPSHOT")
        )

        private const val IMPORTS = """import org.openrndr.color.*
import org.openrndr.extras.color.presets.*
import org.openrndr.extra.color.spaces.*"""

        private const val IMPORTS_PREFIX = "$IMPORTS\nfun main() {"

        @Language("kt")
        private fun colorRGBaExpressionTemplate(
            @Language("kt", prefix = "fun main() {", suffix = "}") expression: String
        ): String = colorRGBaExpressionTemplate("", expression)

        @Language("kt")
        private fun colorRGBaExpressionTemplate(
            @Language("kt") prelude: String, @Language("kt", prefix = IMPORTS_PREFIX, suffix = "}") expression: String
        ): String = """
            $IMPORTS
            $prelude
            
            fun main() { 
                $expression
            }
            """.trimIndent()
    }
}