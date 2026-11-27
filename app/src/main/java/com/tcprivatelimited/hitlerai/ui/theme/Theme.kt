package com.tcprivatelimited.hitlerai.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tcprivatelimited.hitlerai.R

// Brand accents harmonized with the glowing open-hand & sparkles logo
val LogoIndigo = Color(0xFF6B6BFF)
val LogoViolet = Color(0xFF9B6BFF)
val LogoCoral = Color(0xFFF37D5D)
val LogoPeach = Color(0xFFFFC178)
val LogoBackdropNavy = Color(0xFF151925)

val BrandViolet = Color(0xFF9B8CFF)
val BrandTeal = Color(0xFF62D7D0)
val BrandGradientStart = LogoViolet
val BrandGradientEnd = LogoCoral
val BrandInkDark = Color(0xFF0E1018)
val PrimaryPillBlue = Color(0xFF3B6EFF)

val DarkBg = Color(0xFF06070A)
val DarkGlowTop = Color(0xFF151925)
val DarkSidebar = Color(0xFF141518)
val DarkSurface = Color(0xFF1A1B1F)
val DarkSurfaceElevated = Color(0xFF23252C)
val DarkBorder = Color(0xFF2D303A)
val DarkTextPrimary = Color(0xFFF3F4F8)
val DarkTextMuted = Color(0xFF969AA8)
val StatusGreen = Color(0xFF5BD8AD)

val LightBg = Color(0xFFF5F6FA)
val LightSidebar = Color(0xFFECEEF4)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFE9ECF4)
val LightBorder = Color(0xFFD5DBE8)
val LightTextPrimary = Color(0xFF161A26)
val LightTextMuted = Color(0xFF5F687D)

enum class WorkspaceBackgroundPreset(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val isDark: Boolean,
    val baseColor: Color,
    val secondaryGradientColor: Color,
    val topGlowColor: Color,
    val bottomGlowColor: Color,
    val drawerColor: Color,
    val surfaceColor: Color,
    val elevatedSurfaceColor: Color,
    val pillContainerColor: Color,
    val borderColor: Color
) {
    OBSIDIAN(
        id = "obsidian",
        displayName = "Obsidian Minimal",
        subtitle = "Pure deep black studio canvas",
        isDark = true,
        baseColor = Color(0xFF050608),
        secondaryGradientColor = Color(0xFF0B0C10),
        topGlowColor = Color(0xFF5D52FF),
        bottomGlowColor = Color(0xFFF07B56),
        drawerColor = Color(0xFF151619),
        surfaceColor = Color(0xFF1A1B1F),
        elevatedSurfaceColor = Color(0xFF23252B),
        pillContainerColor = Color(0xFF202228),
        borderColor = Color(0xFF2E313B)
    ),
    MIDNIGHT_NEBULA(
        id = "midnight_nebula",
        displayName = "Midnight Nebula",
        subtitle = "Matches your glowing logo palette",
        isDark = true,
        baseColor = Color(0xFF0E121D),
        secondaryGradientColor = Color(0xFF161B2B),
        topGlowColor = Color(0xFF7B5CFF),
        bottomGlowColor = Color(0xFFF3825B),
        drawerColor = Color(0xFF121622),
        surfaceColor = Color(0xFF191F30),
        elevatedSurfaceColor = Color(0xFF222A3F),
        pillContainerColor = Color(0xFF20273B),
        borderColor = Color(0xFF303A54)
    ),
    WARM_CHARCOAL(
        id = "warm_charcoal",
        displayName = "Warm Charcoal",
        subtitle = "Editorial matte graphite tone",
        isDark = true,
        baseColor = Color(0xFF141312),
        secondaryGradientColor = Color(0xFF1C1A18),
        topGlowColor = Color(0xFFD97757),
        bottomGlowColor = Color(0xFF8B62FF),
        drawerColor = Color(0xFF1B1A18),
        surfaceColor = Color(0xFF23211F),
        elevatedSurfaceColor = Color(0xFF2C2A27),
        pillContainerColor = Color(0xFF282623),
        borderColor = Color(0xFF3A3632)
    ),
    AURORA_DUSK(
        id = "aurora_dusk",
        displayName = "Aurora Dusk",
        subtitle = "Deep slate indigo with cool mist",
        isDark = true,
        baseColor = Color(0xFF09121B),
        secondaryGradientColor = Color(0xFF102230),
        topGlowColor = Color(0xFF3B82F6),
        bottomGlowColor = Color(0xFF2DD4BF),
        drawerColor = Color(0xFF0E1924),
        surfaceColor = Color(0xFF142230),
        elevatedSurfaceColor = Color(0xFF1C2E40),
        pillContainerColor = Color(0xFF1A2B3C),
        borderColor = Color(0xFF284158)
    ),
    EMBER_GLOW(
        id = "ember_glow",
        displayName = "Ember Plum",
        subtitle = "Velvet plum with warm sunset aura",
        isDark = true,
        baseColor = Color(0xFF130B18),
        secondaryGradientColor = Color(0xFF201124),
        topGlowColor = Color(0xFFD946EF),
        bottomGlowColor = Color(0xFFFB923C),
        drawerColor = Color(0xFF1A0F20),
        surfaceColor = Color(0xFF24162C),
        elevatedSurfaceColor = Color(0xFF301E3A),
        pillContainerColor = Color(0xFF2B1A34),
        borderColor = Color(0xFF452C52)
    ),
    CLEAN_DAYLIGHT(
        id = "clean_daylight",
        displayName = "Clean Daylight",
        subtitle = "Crisp minimal studio light theme",
        isDark = false,
        baseColor = Color(0xFFF6F7FB),
        secondaryGradientColor = Color(0xFFECEFF7),
        topGlowColor = Color(0xFF8B7BFF),
        bottomGlowColor = Color(0xFFF59B7B),
        drawerColor = Color(0xFFEDF0F7),
        surfaceColor = Color(0xFFFFFFFF),
        elevatedSurfaceColor = Color(0xFFE7EBF5),
        pillContainerColor = Color(0xFFE8ECF6),
        borderColor = Color(0xFFD5DCEB)
    );

    companion object {
        fun fromId(id: String?): WorkspaceBackgroundPreset =
            entries.firstOrNull { it.id == id } ?: OBSIDIAN
    }
}

val LocalWorkspacePreset = staticCompositionLocalOf { WorkspaceBackgroundPreset.OBSIDIAN }

private fun buildDarkScheme(preset: WorkspaceBackgroundPreset) = darkColorScheme(
    primary = LogoViolet,
    onPrimary = Color(0xFF0E1018),
    primaryContainer = preset.elevatedSurfaceColor,
    onPrimaryContainer = DarkTextPrimary,
    secondary = LogoCoral,
    onSecondary = Color(0xFF1A0E0A),
    secondaryContainer = Color(0xFF2E1E1A),
    onSecondaryContainer = LogoPeach,
    tertiary = BrandTeal,
    onTertiary = BrandInkDark,
    background = preset.baseColor,
    onBackground = DarkTextPrimary,
    surface = preset.surfaceColor,
    onSurface = DarkTextPrimary,
    surfaceVariant = preset.elevatedSurfaceColor,
    onSurfaceVariant = DarkTextMuted,
    outline = preset.borderColor,
    outlineVariant = preset.borderColor.copy(alpha = 0.55f),
    error = Color(0xFFFF7A7A),
    onError = Color(0xFF2B0A0A)
)

private fun buildLightScheme(preset: WorkspaceBackgroundPreset) = lightColorScheme(
    primary = Color(0xFF5A4AE3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7E3FF),
    onPrimaryContainer = Color(0xFF1E1657),
    secondary = Color(0xFFD65E3F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE5DC),
    onSecondaryContainer = Color(0xFF4B190C),
    tertiary = Color(0xFF0F8E87),
    onTertiary = Color.White,
    background = preset.baseColor,
    onBackground = LightTextPrimary,
    surface = preset.surfaceColor,
    onSurface = LightTextPrimary,
    surfaceVariant = preset.elevatedSurfaceColor,
    onSurfaceVariant = LightTextMuted,
    outline = preset.borderColor,
    outlineVariant = Color(0xFFE0E5F0),
    error = Color(0xFFC62828),
    onError = Color.White
)

val DisplayFontFamily = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.SemiBold),
    Font(R.font.space_grotesk, FontWeight.Bold)
)

val BodyFontFamily = FontFamily(
    Font(R.font.dm_sans, FontWeight.Normal),
    Font(R.font.dm_sans, FontWeight.Medium),
    Font(R.font.dm_sans, FontWeight.SemiBold),
    Font(R.font.dm_sans, FontWeight.Bold)
)

val SerifGreetingFontFamily = FontFamily(
    Font(R.font.newsreader, FontWeight.Normal),
    Font(R.font.newsreader, FontWeight.Medium),
    Font(R.font.newsreader, FontWeight.SemiBold)
)

val WorkspaceTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.6).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 25.sp,
        lineHeight = 31.sp,
        letterSpacing = (-0.4).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    titleSmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.2.sp
    )
)

val WorkspaceShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun HitlerAiTheme(
    backgroundPreset: WorkspaceBackgroundPreset = WorkspaceBackgroundPreset.OBSIDIAN,
    content: @Composable () -> Unit
) {
    val colors = if (backgroundPreset.isDark) {
        buildDarkScheme(backgroundPreset)
    } else {
        buildLightScheme(backgroundPreset)
    }
    CompositionLocalProvider(LocalWorkspacePreset provides backgroundPreset) {
        MaterialTheme(
            colorScheme = colors,
            typography = WorkspaceTypography,
            shapes = WorkspaceShapes,
            content = content
        )
    }
}

@Composable
fun WorkspaceBackgroundCanvas(
    preset: WorkspaceBackgroundPreset,
    ambientGlowEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(preset.baseColor, preset.secondaryGradientColor, preset.baseColor)
                )
            )
    ) {
        if (ambientGlowEnabled) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val topAlpha = if (preset.isDark) {
                    if (preset == WorkspaceBackgroundPreset.OBSIDIAN) 0.14f else 0.24f
                } else 0.14f
                val bottomAlpha = if (preset.isDark) {
                    if (preset == WorkspaceBackgroundPreset.OBSIDIAN) 0.10f else 0.18f
                } else 0.10f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            preset.topGlowColor.copy(alpha = topAlpha),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.22f, size.height * 0.16f),
                        radius = size.maxDimension * 0.52f
                    ),
                    radius = size.maxDimension * 0.52f,
                    center = Offset(size.width * 0.22f, size.height * 0.16f)
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            preset.bottomGlowColor.copy(alpha = bottomAlpha),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.78f),
                        radius = size.maxDimension * 0.48f
                    ),
                    radius = size.maxDimension * 0.48f,
                    center = Offset(size.width * 0.85f, size.height * 0.78f)
                )
            }
        }
    }
}
