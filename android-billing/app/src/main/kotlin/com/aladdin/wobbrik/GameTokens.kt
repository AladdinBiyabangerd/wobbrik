package com.aladdin.wobbrik

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Port of docs/design/mobile-v1/tokens.css. Scene dimensions use engine units. */
internal object GameTokens {
    val Background = Color(0xFF061831)
    val Surface = Color(0xFF102C53)
    val Raised = Color(0xFF193D69)
    val Text = Color(0xFFF4F7FF)
    val Muted = Color(0xFFB3C8EC)
    val Accent = Color(0xFF28E5EF)
    val Success = Color(0xFF32EDBD)
    val Danger = Color(0xFFFFB4C8)
    // Fine outline and dim treatment used by the approved game/pause design.
    val Hairline = 1.dp
    const val DimOpacity = 0.72f
    val Quarter = 4.dp // --space-1
    val Small = 8.dp
    val Gap = 16.dp
    val Inset = 24.dp
    val CardRadius = 24.dp
    val ControlRadius = 16.dp
    val Target = 48.dp
    // Expanded breakpoint from tablet-v1; pause panel width matches game-tap framing.
    val MediumWidth = 600.dp
    val ExpandedWidth = 840.dp
    // Window height class boundary: below it a wide window uses the three-pane account layout.
    val CompactHeight = 480.dp
    val PanelWidth = 360.dp
    val PanelElevation = 12.dp
    val Caption = 12.sp // --type-caption
    val Label = 14.sp // --type-label
    val Body = 16.sp
    val Title = 24.sp
    val Score = 32.sp
    // Approved mobile Home navbar; all dimensions derive from the existing scale.
    internal object MobileUi {
        val CompactWidth = PanelWidth
        val CompactHeight = 640.dp
        val PrimaryTarget = Target + Small
    }
    internal object MobileHome {
        // Transparent margins are excluded by BitmapPainter; no new bitmap allocation.
        const val HeroLeft = 90
        const val HeroWidth = 1100
        // Native surface palette sampled from the approved Home reference.
        val PanelTop = Color(0xFF062855)
        val PanelBottom = Color(0xFF031A3A)
        val Rim = Color(0xFF126EB1)
        val RimLight = Color(0xFF67CFFF)
        val Gold = Color(0xFFFFB800)
        val GoldLight = Color(0xFFFFFF76)
        val HomeTitle = 30.sp
        val CompactNavLabel = 10.sp
        const val HeroAspectRatio = 1100f / 1226f
        val HeaderTop = Inset
        val StatBottomSpace = Target
        val NavIcon = Inset + Quarter
        val PlayTop = Color(0xFF52F9FF)
        val PlayBottom = Color(0xFF00BCCB)
        val HeroMax = Target * 7
        val LargeFontHero = Target * 2
        val NavbarLabelWidth = PanelWidth - Gap * 2
        val PlayDiameter = Target + Gap
        val PlaySlot = Target + Inset
        val NavbarHeight = Target + Inset + Small
        val LargeFontNavbarHeight = Target * 2 + Gap
    }
    /** Inventory reference measured at843px wide, scaled to native portrait coordinates. */
    internal object InventoryCollection {
        // Sampled from the approved reference, rather than the older blue palette.
        val Background = Color(0xFF041E38)
        val PanelTop = Color(0xFF0E3053)
        val PanelBottom = Color(0xFF072849)
        val DockBottom = Color(0xFF05213D)
        val Selected = Color(0xFF092F53)
        val Outline = Color(0xFF285F80)
        val Metadata = Color(0xFF94C6F1)
        val OutlineWidth = Hairline / 2
        val ScreenInset = Gap - Quarter / 2
        val HeaderHeight = Target + Small
        val HeaderLogoWidth = Target * 2
        val HeaderLogoHeight = Target - Small
        // Alpha bounds of wobbrik_logo.png; painter crops without allocating a new bitmap.
        const val LogoSourceX = 68
        const val LogoSourceY = 102
        const val LogoSourceWidth = 1614
        const val LogoSourceHeight = 696
        val WalletCoin = Inset - Quarter
        val ArtHeight = Target * 3 + Gap
        val CompactArtHeight = Target * 3
        val LargeTypeArtHeight = Target * 2
        val CardInset = Small + Quarter
        val CardGap = Small
        val DockHeight = Target + Gap
        val DockRadius = Inset - Quarter
        val DockClearance = DockHeight + Gap
        val PlayDiameter = Target - Small
        val NavIcon = Inset - Quarter
        val NavLabel = MobileHome.CompactNavLabel
        val TabIcon = Gap
        val TabVisualHeight = Target - Small - Quarter
        val IndicatorHeight = Quarter / 2
        val RefreshBody = Target - Gap
        const val ArtScale = 1.15f
        const val PressedScale = 0.96f
    }
    internal object AppearanceCollection {
        val Unlocked = Color(0xFF2FF468) // sampled from the approved collection PNG
        val CategoryHeight = Target
        val CategoryIcon = Inset
        val CategoryRadius = ControlRadius - Quarter
        val ArtHeight = Target * 3 + Small
        val CompactArtHeight = Target * 2 + Inset
        val LargeTypeArtHeight = Target * 2
        val FilterRadius = CardRadius
        // A decorative two-storey sample, never accepted building geometry.
        const val SampleWidth = 280f
        const val SampleHeight = 340f
    }
    /** Art/card and centred CTA proportions measured from the approved Dekor proposal. */
    internal object DecorCollection {
        const val ArtHeightFraction = 0.52f
        const val MarketWidthFraction = 0.64f
    }
    internal object OwnedDetail {
        val HeroHeight = Target * 6
        val CompactHeroHeight = Target * 3
        val LargeTypeHeroHeight = Target * 2
        val SectionGap = Small
        val Title = 20.sp // measured from approved detail proposal at390dp
        val StatHeight = Target + Small
        val OwnershipFill = Color(0xFF0A3B37)
        val ActionHeight = Target
    }
    internal object Achievements {
        // Transparent illustration sheet derived from the approved 01-main artwork.
        val Trophy = intArrayOf(48, 112, 690, 440)
        val GoldMedal = intArrayOf(790, 136, 304, 390)
        val BlueMedal = intArrayOf(184, 720, 316, 406)
        val PurpleMedal = intArrayOf(780, 720, 316, 406)
        val HeroHeight = Target + Inset + Small
        val ProgressHeight = Small + Quarter
    }
    internal object Leaderboard {
        // Three equal sprite cells: builder, pilot, architect; no baked labels or UI.
        const val PortraitCell = 724
        // Pixel source bounds on the unchanged approved play-progress-v2 board.
        const val ArtX = 1340
        const val ArtY = 205
        const val ArtWidth = 418
        const val ArtHeight = 264
        // Source portrait interiors; destination circles extend to the inner metallic rim.
        const val FirstAvatarX = 1505
        const val FirstAvatarY = 243
        const val FirstAvatarWidth = 84
        const val FirstAvatarHeight = 82
        const val SecondAvatarX = 1384
        const val SecondAvatarY = 273
        const val SecondAvatarWidth = 84
        const val SecondAvatarHeight = 76
        const val ThirdAvatarX = 1638
        const val ThirdAvatarY = 279
        const val ThirdAvatarWidth = 74
        const val ThirdAvatarHeight = 73
        const val PortraitSize = 72
        const val SidePortraitSize = 68
        const val BuilderPortraitX = 1512
        const val BuilderPortraitY = 245
        const val PilotPortraitX = 1391
        const val PilotPortraitY = 276
        const val ArchitectPortraitX = 1641
        const val ArchitectPortraitY = 281
        // Independent, clean small circles keep their original rim in list/editor rows.
        const val RowAvatarX = 1427
        const val RowAvatarSize = 60
        const val BuilderRowY = 494
        const val PilotRowY = 568
        const val ArchitectRowY = 644
        const val FirstScoreX = 1511
        const val FirstScoreY = 330
        const val FirstScoreWidth = 72
        const val FirstScoreHeight = 26
        const val SecondScoreX = 1396
        const val SecondScoreY = 348
        const val SecondScoreWidth = 64
        const val SecondScoreHeight = 24
        const val ThirdScoreX = 1644
        const val ThirdScoreY = 357
        const val ThirdScoreWidth = 66
        const val ThirdScoreHeight = 25
        const val ScoreTextSize = 24
        const val MaxPodiumDigits = 4
        const val BackgroundX = 1337
        const val BackgroundY = 100
        const val BackgroundStep = 200
        const val BackgroundSamples = 4
        const val BackX = 1351
        const val BackY = 32
        const val BackSize = 54
        const val CalendarX = 1390
        const val CalendarY = 157
        const val CalendarWidth = 30
        const val CalendarHeight = 34
        const val FloorsX = 1652
        const val FloorsY = 505
        const val FloorsWidth = 32
        const val FloorsHeight = 31
        const val RankX = 1363
        const val RankY = 501
        const val RankStep = 75
        const val RankWidth = 58
        const val RankHeight = 44
    }
    internal object AccountArt {
        const val HeroX = 24
        const val HeroY = 99
        const val HeroWidth = 400
        const val HeroHeight = 245
    }
    internal object Profile {
        val Portrait = Target * 2 + Inset + Small
        val OverviewPortrait = Target * 3 - Small
        val CompactPortrait = Target + Inset
        val PanelInset = Gap
        val StatIcon = Target + Gap + Quarter
        val SheetHeightFraction = 0.85f
        val SmallSheetHeightFraction = 0.55f
        val SmallGridHeight = Target * 3 + Inset
        val Back = intArrayOf(24, 44, 115, 101)
        val WalletCoin = intArrayOf(609, 43, 94, 96)
        const val TextShadowOffset = 2f
        const val TextShadowBlur = 1f
        const val PortraitCornerPercent = 25
        val NavHome = intArrayOf(36, 1605, 112, 76)
        val NavAchievements = intArrayOf(280, 1605, 116, 76)
        val NavRecords = intArrayOf(501, 1605, 112, 76)
        val NavSettings = intArrayOf(727, 1601, 112, 80)
        // Source rectangles on unchanged, user-approved profile-v2 boards.
        val Builder = intArrayOf(56, 324, 311, 316)
        val Pilot = intArrayOf(1006, 688, 149, 146)
        val Architect = intArrayOf(1176, 688, 152, 146)
        val Crown = intArrayOf(90, 978, 244, 150)
        val Xp = intArrayOf(570, 980, 166, 156)
        val LevelBadge = intArrayOf(406, 465, 280, 174)
        val BadgeInterior = floatArrayOf(501f,503f,554f,483f,605f,503f,604f,552f,
            590f,579f,554f,601f,518f,579f,501f,552f)
        val BadgeColorSamples = arrayOf(intArrayOf(511,520), intArrayOf(594,559))
        val PanelSource = intArrayOf(202, 211, 500, 764)
        val ActionSource = intArrayOf(223, 797, 459, 84)
        val ActionErase = intArrayOf(27, 12, 427, 70)
        val PanelErase = intArrayOf(20, 20, 482, 744)
        const val PanelCorner = 48
        const val ActionCorner = 32
        val IdentitySource = intArrayOf(45, 1330, 814, 180)
        val NavbarSource = intArrayOf(0, 1588, 898, 164)
        val WalletSource = intArrayOf(701, 49, 176, 85)
        val Pencil = intArrayOf(745, 372, 94, 86)
        val IdentityPortrait = intArrayOf(73, 1355, 133, 135)
        val WalletWidth = Target * 2 + Small
        val BadgeWidth = Target * 3
        val BadgeHeight = Target + Inset
        val NavIcon = Target
        val CompactNavIcon = Target - Small
        val NavIconHeight = Target - Gap
        val OverviewHeader = intArrayOf(8, 151, 882, 173)
        val OverviewInterior = intArrayOf(8, 324, 882, 1132)
        val OverviewFooter = intArrayOf(8, 1456, 882, 100)
        val Rule = intArrayOf(57, 683, 784, 6)
        val StatRule = intArrayOf(446, 995, 6, 257)
        val StatRowHeight = Target * 3 - Gap
        val CrownWidth = Target * 2 + Gap
        val StatLabel = Body
        // Optical center of the shield interior relative to its decorative wings.
        val BadgeTextOffsetX = Quarter / 2
        val BadgeTextOffsetY = -Quarter

    }
    internal object Market {
        // Portrait sale form: content and actions stay together; font2 can use the available height.
        val SaleDialogHeight = Target * 7 + Gap
    }
    val Hint = 18.sp
    // Approved decorative inventory/detail presentation; never active gameplay.
    internal object BuildingPreview {
        const val MaxFloors = 8
        const val PurpleMaxFloors = 6 // approved 6–8 representative floors; broad modular art
        const val MaxOffsetTicks = 30000.0
        const val GardenWidth = 260f // preview scene units; floor width is 100
    }
}
