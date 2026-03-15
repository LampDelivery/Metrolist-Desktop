package com.metrolist.desktop.ui.theme

import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val MetrolistShareIcon: ImageVector
    get() = ImageVector.Builder(
        name = "MetrolistShare",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = SolidColor(Color.White),
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Miter
    ) {
        moveTo(15f, 5.63f)
        lineTo(20.66f, 12f)
        lineTo(15f, 18.37f)
        verticalLineTo(15f)
        verticalLineToRelative(-1f)
        horizontalLineToRelative(-1f)
        curveTo(10.04f, 14f, 6.86f, 15f, 4.25f, 17.09f)
        curveTo(6.09f, 13.02f, 9.36f, 10.69f, 14.14f, 9.99f)
        lineTo(15f, 9.86f)
        verticalLineTo(9f)
        verticalLineTo(5.63f)
        moveTo(14f, 3f)
        verticalLineToRelative(6f)
        curveTo(6.22f, 10.13f, 3.11f, 15.33f, 2f, 21f)
        curveToRelative(2.78f, -3.97f, 6.44f, -6f, 12f, -6f)
        verticalLineToRelative(6f)
        lineToRelative(8f, -9f)
        lineTo(14f, 3f)
        lineTo(14f, 3f)
        close()
    }.build()

val MetrolistStatsIcon: ImageVector
    get() = ImageVector.Builder(
        name = "MetrolistStats",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).path(fill = SolidColor(Color.White)) {
        moveTo(480f, 880f)
        quadToRelative(-66f, 0f, -127.5f, -20.5f)
        reflectiveQuadTo(240f, 800f)
        lineToRelative(58f, -58f)
        quadToRelative(42f, 29f, 88f, 43.5f)
        reflectiveQuadToRelative(94f, 14.5f)
        quadToRelative(133f, 0f, 226.5f, -93.5f)
        reflectiveQuadTo(800f, 480f)
        quadToRelative(0f, -133f, -93.5f, -226.5f)
        reflectiveQuadTo(480f, 160f)
        quadToRelative(-133f, 0f, -226.5f, 93.5f)
        reflectiveQuadTo(160f, 480f)
        lineTo(80f, 480f)
        quadToRelative(0f, -83f, 31.5f, -156f)
        reflectiveQuadTo(197f, 197f)
        quadToRelative(54f, -54f, 127f, -85.5f)
        reflectiveQuadTo(480f, 80f)
        quadToRelative(83f, 0f, 155.5f, 31.5f)
        reflectiveQuadToRelative(127f, 86f)
        quadToRelative(54.5f, 54.5f, 86f, 127f)
        reflectiveQuadTo(880f, 480f)
        quadToRelative(0f, 82f, -31.5f, 155f)
        reflectiveQuadToRelative(-86f, 127.5f)
        quadToRelative(-54.5f, 54.5f, -127f, 86f)
        reflectiveQuadTo(480f, 880f)
        close()
        moveTo(159f, 717f)
        lineToRelative(163f, -163f)
        lineToRelative(120f, 100f)
        lineToRelative(198f, -198f)
        verticalLineToRelative(104f)
        horizontalLineToRelative(80f)
        verticalLineToRelative(-240f)
        lineTo(480f, 320f)
        verticalLineToRelative(80f)
        horizontalLineToRelative(104f)
        lineToRelative(-146f, 146f)
        lineToRelative(-120f, -100f)
        lineToRelative(-201f, 201f)
        quadToRelative(11f, 23f, 19.5f, 37.5f)
        reflectiveQuadTo(159f, 717f)
        close()
        moveTo(480f, 480f)
        close()
    }.build()

val MetrolistTogetherIcon: ImageVector
    get() = ImageVector.Builder(
        name = "MetrolistTogether",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).path(fill = SolidColor(Color.White)) {
        // M40,800v-112q0,-34 17.5,-62.5T104,582q62,-31 126,-46.5T360,520q66,0 130,15.5T616,582q29,15 46.5,43.5T680,688v112H40Z
        moveTo(40f, 800f)
        verticalLineToRelative(-112f)
        quadToRelative(0f, -34f, 17.5f, -62.5f)
        reflectiveQuadTo(104f, 582f)
        quadToRelative(62f, -31f, 126f, -46.5f)
        reflectiveQuadTo(360f, 520f)
        quadToRelative(66f, 0f, 130f, 15.5f)
        reflectiveQuadTo(616f, 582f)
        quadToRelative(29f, 15f, 46.5f, 43.5f)
        reflectiveQuadTo(680f, 688f)
        verticalLineToRelative(112f)
        horizontalLineTo(40f)
        close()
        // M760,800v-120q0,-44-24.5,-84.5T666,526q51,6 96,20.5t84,35.5q36,20 55,44.5t19,53.5v120H760Z
        moveTo(760f, 800f)
        verticalLineToRelative(-120f)
        quadToRelative(0f, -44f, -24.5f, -84.5f)
        reflectiveQuadTo(666f, 526f)
        quadToRelative(51f, 6f, 96f, 20.5f)
        reflectiveQuadToRelative(84f, 35.5f)
        quadToRelative(36f, 20f, 55f, 44.5f)
        reflectiveQuadToRelative(19f, 53.5f)
        verticalLineToRelative(120f)
        horizontalLineTo(760f)
        close()
        // M360,480q-66,0-113,-47t-47,-113q0,-66 47,-113t113,-47q66,0 113,47t47,113q0,66-47,113t-113,47Z
        moveTo(360f, 480f)
        quadToRelative(-66f, 0f, -113f, -47f)
        reflectiveQuadToRelative(-47f, -113f)
        quadToRelative(0f, -66f, 47f, -113f)
        reflectiveQuadToRelative(113f, -47f)
        quadToRelative(66f, 0f, 113f, 47f)
        reflectiveQuadToRelative(47f, 113f)
        quadToRelative(0f, 66f, -47f, 113f)
        reflectiveQuadToRelative(-113f, 47f)
        close()
        // M760,320q0,66-47,113t-113,47q-11,0-28,-2.5t-28,-5.5q27,-32 41.5,-71t14.5,-81q0,-42-14.5,-81T544,168q14,-5 28,-6.5t28,-1.5q66,0 113,47t47,113Z
        moveTo(760f, 320f)
        quadToRelative(0f, 66f, -47f, 113f)
        reflectiveQuadToRelative(-113f, 47f)
        quadToRelative(-11f, 0f, -28f, -2.5f)
        reflectiveQuadToRelative(-28f, -5.5f)
        quadToRelative(27f, -32f, 41.5f, -71f)
        reflectiveQuadToRelative(14.5f, -81f)
        quadToRelative(0f, -42f, -14.5f, -81f)
        reflectiveQuadTo(544f, 168f)
        quadToRelative(14f, -5f, 28f, -6.5f)
        reflectiveQuadToRelative(28f, -1.5f)
        quadToRelative(66f, 0f, 113f, 47f)
        reflectiveQuadToRelative(47f, 113f)
        close()
        // M120,720h480v-32q0,-11-5.5,-20T580,654q-54,-27-109,-40.5T360,600q-56,0-111,13.5T140,654q-9,5-14.5,14t-5.5,20v32Z
        moveTo(120f, 720f)
        horizontalLineToRelative(480f)
        verticalLineToRelative(-32f)
        quadToRelative(0f, -11f, -5.5f, -20f)
        reflectiveQuadTo(580f, 654f)
        quadToRelative(-54f, -27f, -109f, -40.5f)
        reflectiveQuadTo(360f, 600f)
        quadToRelative(-56f, 0f, -111f, 13.5f)
        reflectiveQuadTo(140f, 654f)
        quadToRelative(-9f, 5f, -14.5f, 14f)
        reflectiveQuadToRelative(-5.5f, 20f)
        verticalLineToRelative(32f)
        close()
        // M360,400q33,0 56.5,-23.5T440,320q0,-33-23.5,-56.5T360,240q-33,0-56.5,23.5T280,320q0,33 23.5,56.5T360,400Z
        moveTo(360f, 400f)
        quadToRelative(33f, 0f, 56.5f, -23.5f)
        reflectiveQuadTo(440f, 320f)
        quadToRelative(0f, -33f, -23.5f, -56.5f)
        reflectiveQuadTo(360f, 240f)
        quadToRelative(-33f, 0f, -56.5f, 23.5f)
        reflectiveQuadTo(280f, 320f)
        quadToRelative(0f, 33f, 23.5f, 56.5f)
        reflectiveQuadTo(360f, 400f)
        close()
    }.build()

val MetrolistPauseIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Pause",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).path(fill = SolidColor(Color.White)) {
        moveTo(520f, 760f)
        verticalLineToRelative(-560f)
        horizontalLineToRelative(240f)
        verticalLineToRelative(560f)
        lineTo(520f, 760f)
        close()
        moveTo(200f, 760f)
        verticalLineToRelative(-560f)
        horizontalLineToRelative(240f)
        verticalLineToRelative(560f)
        lineTo(200f, 760f)
        close()
        moveTo(600f, 680f)
        horizontalLineToRelative(80f)
        verticalLineToRelative(-400f)
        horizontalLineToRelative(-80f)
        verticalLineToRelative(400f)
        close()
        moveTo(280f, 680f)
        horizontalLineToRelative(80f)
        verticalLineToRelative(-400f)
        horizontalLineToRelative(-80f)
        verticalLineToRelative(400f)
        moveTo(600f, 280f)
        verticalLineToRelative(400f)
    }.build()

val MetrolistVolumeUpIcon: ImageVector
    get() = ImageVector.Builder(
        name = "VolumeUp",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).path(fill = SolidColor(Color.White)) {
        moveTo(560f, 829f)
        verticalLineToRelative(-82f)
        quadToRelative(90f, -26f, 145f, -100f)
        reflectiveQuadTo(760f, 479f)
        quadToRelative(0f, -94f, -55f, -168f)
        reflectiveQuadTo(560f, 211f)
        verticalLineToRelative(-82f)
        quadToRelative(124f, 28f, 202f, 125.5f)
        reflectiveQuadTo(840f, 479f)
        quadToRelative(0f, 127f, -78f, 224.5f)
        reflectiveQuadTo(560f, 829f)
        close()
        moveTo(120f, 600f)
        verticalLineToRelative(-240f)
        horizontalLineToRelative(160f)
        lineToRelative(200f, -200f)
        verticalLineToRelative(640f)
        lineTo(280f, 600f)
        lineTo(120f, 600f)
        close()
        moveTo(560f, 640f)
        verticalLineToRelative(-322f)
        quadToRelative(47f, 22f, 73.5f, 66f)
        reflectiveQuadTo(660f, 480f)
        quadToRelative(0f, 51f, -26.5f, 94.5f)
        reflectiveQuadTo(560f, 640f)
        close()
        moveTo(400f, 354f)
        lineToRelative(-86f, 86f)
        lineTo(200f, 440f)
        verticalLineToRelative(80f)
        horizontalLineToRelative(114f)
        lineToRelative(86f, 86f)
        verticalLineToRelative(-252f)
        close()
        moveTo(300f, 480f)
        close()
    }.build()

val MetrolistVolumeDownIcon: ImageVector
    get() = ImageVector.Builder(
        name = "VolumeDown",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).path(fill = SolidColor(Color.White)) {
        moveTo(200f, 600f)
        verticalLineToRelative(-240f)
        horizontalLineToRelative(160f)
        lineToRelative(200f, -200f)
        verticalLineToRelative(640f)
        lineTo(360f, 600f)
        lineTo(200f, 600f)
        close()
        moveTo(560f, 640f)
        verticalLineToRelative(-322f)
        quadToRelative(47f, 22f, 73.5f, 66f)
        reflectiveQuadTo(660f, 480f)
        quadToRelative(0f, 51f, -26.5f, 94.5f)
        reflectiveQuadTo(560f, 640f)
        close()
        moveTo(480f, 354f)
        lineToRelative(-86f, 86f)
        lineTo(280f, 440f)
        verticalLineToRelative(80f)
        horizontalLineToRelative(114f)
        lineToRelative(86f, 86f)
        verticalLineToRelative(-252f)
        close()
        moveTo(380f, 480f)
        close()
    }.build()

val MetrolistVolumeMuteIcon: ImageVector
    get() = ImageVector.Builder(
        name = "VolumeMute",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).path(fill = SolidColor(Color.White)) {
        moveTo(280f, 600f)
        verticalLineToRelative(-240f)
        horizontalLineToRelative(160f)
        lineToRelative(200f, -200f)
        verticalLineToRelative(640f)
        lineTo(440f, 600f)
        lineTo(280f, 600f)
        close()
        moveTo(560f, 354f)
        lineToRelative(-86f, 86f)
        lineTo(360f, 440f)
        verticalLineToRelative(80f)
        horizontalLineToRelative(114f)
        lineToRelative(86f, -86f)
        verticalLineToRelative(-252f)
        close()
        moveTo(460f, 480f)
        close()
    }.build()

val MetrolistVolumeOffIcon: ImageVector
    get() = ImageVector.Builder(
        name = "VolumeOff",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).path(fill = SolidColor(Color.White)) {
        moveTo(792f, 904f)
        lineTo(671f, 783f)
        quadToRelative(-25f, 16f, -53f, 27.5f)
        reflectiveQuadTo(560f, 829f)
        verticalLineToRelative(-82f)
        quadToRelative(14f, -5f, 27.5f, -10f)
        reflectiveQuadTo(613f, 725f)
        lineTo(480f, 592f)
        verticalLineToRelative(208f)
        lineTo(280f, 600f)
        lineTo(120f, 600f)
        verticalLineToRelative(-240f)
        horizontalLineToRelative(128f)
        lineTo(56f, 168f)
        lineToRelative(56f, -56f)
        lineToRelative(736f, 736f)
        lineToRelative(-56f, 56f)
        close()
        moveTo(784f, 672f)
        lineTo(726f, 614f)
        quadToRelative(17f, -31f, 25.5f, -65f)
        reflectiveQuadTo(760f, 479f)
        quadToRelative(0f, -94f, -55f, -168f)
        reflectiveQuadTo(560f, 211f)
        verticalLineToRelative(-82f)
        quadToRelative(124f, 28f, 202f, 125.5f)
        reflectiveQuadTo(840f, 479f)
        quadToRelative(0f, 53f, -14.5f, 102f)
        reflectiveQuadTo(784f, 672f)
        close()
        moveTo(650f, 538f)
        lineToRelative(-90f, -90f)
        verticalLineToRelative(-130f)
        quadToRelative(47f, 22f, 73.5f, 66f)
        reflectiveQuadTo(660f, 480f)
        quadToRelative(0f, 15f, -2.5f, 29.5f)
        reflectiveQuadTo(650f, 538f)
        close()
        moveTo(480f, 368f)
        lineTo(376f, 264f)
        lineToRelative(104f, -104f)
        verticalLineToRelative(208f)
        close()
        moveTo(400f, 606f)
        verticalLineToRelative(-94f)
        lineToRelative(-72f, -72f)
        lineTo(200f, 440f)
        verticalLineToRelative(80f)
        horizontalLineToRelative(114f)
        lineToRelative(86f, 86f)
        close()
        moveTo(364f, 476f)
        close()
    }.build()
