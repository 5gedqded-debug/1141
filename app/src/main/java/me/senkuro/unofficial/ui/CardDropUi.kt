package me.senkuro.unofficial.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.senkuro.unofficial.data.CardDrop
import kotlin.math.cos
import kotlin.math.sin

/*
 * Выпадение карточки в читалке — повторяет сайт (CardDrop.vue + CardDropModal.vue): плавающая «рубашка» у правого края
 * с пульсацией, по тапу — полноэкранное окно с вспышками, звёздочками и выпавшей картой.
 * Цвета и размеры — токены тёмной темы сайта (--text-primary #F1F1F1, --text-tertiary #F1F1F199, --bg-tertiary поверх --bg-primary).
 */

private val CssEase = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
private val GlareEase = CubicBezierEasing(0.25f, 0.1f, 0.15f, 1.2f)

private val TextPrimary = Color(0xFFF1F1F1)
private val TextTertiary = Color(0x99F1F1F1)
private val CardBackground = Color(0xFF26262A)   // --bg-tertiary (#F1F1F11A) поверх --bg-primary (#101014)
private val OrnamentFill = Color(0x12F1F1F1)      // --bg-quaternary
private val CardRadius = 12.dp                    // --border-radius-md

private const val SPARKLE_PATH =
    "M6.54 1.864c-.202-.82-.303-1.231-.35-1.28-.11-.112-.252-.112-.361 0-.048.05-.148.46-.349 1.282-.257 1.054-.61 2.096-1.061 2.548-.452.452-1.495.805-2.552 1.062-.822.2-1.234.3-1.283.348-.112.11-.112.252 0 .361.049.048.46.15 1.282.351 1.057.26 2.1.615 2.553 1.067.451.452.805 1.49 1.062 2.54.2.815.3 1.223.348 1.273.11.112.251.112.36 0 .049-.05.15-.457.351-1.273.26-1.05.615-2.088 1.067-2.54.453-.452 1.491-.81 2.54-1.074.814-.204 1.221-.306 1.27-.355.111-.11.11-.251-.002-.36-.049-.048-.457-.147-1.272-.344-1.048-.254-2.084-.604-2.536-1.056S6.8 2.92 6.54 1.864"

private const val DROP_ICON_PATH =
    "M481-200q-56 0-107-21t-91-61-61-91-21-107q0-51 17-97.5t50-84.5q13-14 32-9.5t27 24.5q21 55 52.5 104t73.5 91 91 73.5T648-326q20 8 24.5 27t-9.5 32q-38 33-84.5 50T481-200m223-192q-16-5-23-20.5t-4-32.5q9-48-6-94.5T621-621t-80.5-49.5T448-677q-17 3-32-4t-21-23 1.5-31 23.5-19q69-15 138 4.5T679-678q51 51 71 120t5 138q-4 17-19 25t-32 3M480-840q-17 0-28.5-11.5T440-880v-40q0-17 11.5-28.5T480-960t28.5 11.5T520-920v40q0 17-11.5 28.5T480-840m0 840q-17 0-28.5-11.5T440-40v-40q0-17 11.5-28.5T480-120t28.5 11.5T520-80v40q0 17-11.5 28.5T480 0m255-734q-12-12-12-28.5t12-28.5l28-28q11-11 27.5-11t28.5 11q12 12 12 28.5T819-762l-28 28q-12 12-28 12t-28-12M141-141q-12-12-12-28.5t12-28.5l28-28q12-12 28-12t28 12 12 28.5-12 28.5l-28 28q-11 11-27.5 11T141-141m739-299q-17 0-28.5-11.5T840-480t11.5-28.5T880-520h40q17 0 28.5 11.5T960-480t-11.5 28.5T920-440zm-840 0q-17 0-28.5-11.5T0-480t11.5-28.5T40-520h40q17 0 28.5 11.5T120-480t-11.5 28.5T80-440zm779 299q-12 12-28.5 12T762-141l-28-28q-12-12-12-28t12-28 28.5-12 28.5 12l28 28q11 11 11 27.5T819-141M226-735q-12 12-28.5 12T169-735l-28-28q-11-11-11-27.5t11-28.5q12-12 28.5-12t28.5 12l28 28q12 12 12 28t-12 28"

private const val TOUCH_ICON_PATH =
    "M419-80q-28 0-52.5-12T325-126L124-381q-8-9-7-21.5t9-20.5q20-21 48-25t52 11l74 45v-328q0-17 11.5-28.5T340-760t29 11.5 12 28.5v200h299q50 0 85 35t35 85v160q0 66-47 113T640-80zm60-520q-17 0-28.5-11.5T439-640q0-2 5-20 8-14 12-28.5t4-31.5q0-50-35-85t-85-35-85 35-35 85q0 17 4 31.5t12 28.5q3 5 4 10t1 10q0 17-11 28.5T202-600q-11 0-20.5-6T167-621q-13-22-20-47t-7-52q0-83 58.5-141.5T340-920t141.5 58.5T540-720q0 27-7 52t-20 47q-5 9-14 15t-20 6"

private const val ORNAMENT_FILL_PATH =
    "M123.5 17.5 138 3l14.5 14.5L138 32zM104 16H37v3h67zM239 16h-67v3h67zM121 17.5a3.5 3.5 0 1 1-7 0 3.5 3.5 0 0 1 7 0M162 17.5a3.5 3.5 0 1 1-7 0 3.5 3.5 0 0 1 7 0"

// два отдельных контура сайта (у каждого своё начало координат), оба рисуются цветом --bg-primary-inverse
private const val ORNAMENT_LINE_PATH_A =
    "m138 2.5 15 15-15 15-15-15zm0 27.171L150.171 17.5 138 5.329 125.828 17.5z"

private const val ORNAMENT_LINE_PATH_B =
    "m133 17.5 5-5 5 5-5 5zM25.5 17.5l-12-6.928-12 6.929 12 6.928zM34.5 14.036l-6 3.464 6 3.465 6-3.465zM112 17.5l-6-3.464-6 3.464 6 3.465zM250.5 17.5l12 6.928 12-6.928-12-6.929zM241.5 20.964l6-3.464-6-3.465-6 3.465zM164 17.5l6 3.464 6-3.464-6-3.465z"

// ---------------------------------------------------------------- плавающая карточка (CardDrop.vue)

private fun pulseSpec(duration: Int, points: List<Pair<Int, Float>>) = keyframes<Float> {
    durationMillis = duration
    points.forEach { (time, value) -> value at time using CssEase }
}

/**
 * Карточка-рубашка 80×114, повёрнутая на −35° и наполовину спрятанная за правым краем; позицию (80% высоты) задаёт вызывающий.
 * Анимации как на сайте: два «подмигивания» через секунду после появления и затем раз в минуту.
 */
@Composable
fun CardDropTrigger(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val rotation = remember { Animatable(-35f) }
    val scale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        delay(1000)
        repeat(2) {
            coroutineScope {
                launch { rotation.animateTo(-35f, pulseSpec(1000, listOf(0 to -35f, 250 to -30f, 500 to -35f, 750 to -40f, 1000 to -35f))) }
                launch { scale.animateTo(1f, pulseSpec(1000, listOf(0 to 1f, 250 to 1.05f, 500 to 1f, 750 to 1.05f, 1000 to 1f))) }
            }
        }
        delay(1000)
        while (true) {
            coroutineScope {
                launch { rotation.animateTo(-35f, pulseSpec(2400, listOf(0 to -35f, 1200 to -30f, 2400 to -35f))) }
                launch { scale.animateTo(1f, pulseSpec(2400, listOf(0 to 1f, 1200 to 1.03f, 2400 to 1f))) }
            }
            delay(57_600)
        }
    }
    Image(
        painter = painterResource(me.senkuro.unofficial.R.drawable.card_back),
        contentDescription = "Забрать выпавшую карточку",
        contentScale = ContentScale.FillBounds,
        modifier = modifier
            .size(width = 80.dp, height = 114.dp)
            .graphicsLayer {
                rotationZ = rotation.value
                scaleX = scale.value
                scaleY = scale.value
            }
            .shadow(6.dp, RoundedCornerShape(CardRadius))
            .clip(RoundedCornerShape(CardRadius))
            .clickable(onClick = onClick),
    )
}

// ---------------------------------------------------------------- окно с результатом (CardDropModal.vue)

/** Полноэкранное окно выпавшей карточки: тап по фону закрывает, тап по карте открывает её страницу. */
@Composable
fun CardDropDialog(drop: CardDrop, onClose: () -> Unit, onOpenCard: (String) -> Unit) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        CardDropContent(drop, onClose, onOpenCard)
    }
}

private class Glare(val start: Float, val mid: Float, val shrink: Float, val angle: Float, val distVw: Float, val color: Color, val top: Float, val left: Float)
private class Sparkle(val start: Float, val mid: Float, val angle: Float, val distVw: Float, val top: Float, val left: Float, val rotation: Float)

private val Glares = listOf(
    Glare(180f, 222f, 102f, 225f, 22.46f, Color(0xFFCCCCCC), 0.20f, 0.39f),
    Glare(82f, 112f, 48f, 45f, 13.08f, Color(0xFFFF5FDF), 0.43f, 0.65f),
    Glare(148f, 176f, 64f, 135f, 26.15f, Color.White, 0.76f, 0.45f),
    Glare(76f, 126f, 64f, 315f, 36.15f, Color(0xFF7081FF), 0.74f, 0.68f),
)

private val Sparkles = listOf(
    Sparkle(8f, 14f, 248f, 33.08f, 0.48f, 0.53f, -36.53f),
    Sparkle(9f, 11f, 210f, 36f, 0.50f, 0.50f, 0f),
    Sparkle(8f, 16f, 315f, 32f, 0.51f, 0.53f, -36.53f),
    Sparkle(16f, 16f, -20f, 34f, 0.508f, 0.57f, 32.3f),
    Sparkle(13f, 12f, 160f, 32f, 0.55f, 0.48f, -36.07f),
    Sparkle(10f, 18f, 112f, 32f, 0.57f, 0.53f, 32.3f),
    Sparkle(7f, 11f, 30f, 31f, 0.54f, 0.56f, 32.3f),
    Sparkle(8f, 14f, 248f, 200f, 0.20f, 0.37f, -36.53f),
    Sparkle(12f, 14f, 210f, 100f, 0.36f, 0.34f, 0f),
    Sparkle(8f, 16f, 315f, 100f, 0.33f, 0.68f, -36.53f),
    Sparkle(16f, 16f, -20f, 100f, 0.24f, 0.80f, 32.3f),
)

@Composable
private fun CardDropContent(drop: CardDrop, onClose: () -> Unit, onOpenCard: (String) -> Unit) {
    // общее время для вспышек и звёзд (мс), затем появление текста и «падение» карты
    val time = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val cardScale = remember { Animatable(4f) }
    val cardRotation = remember { Animatable(0f) }
    val cardAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        coroutineScope {
            launch { time.animateTo(3200f, tween(3200, easing = LinearEasing)) }
            launch {
                delay(300)
                textAlpha.animateTo(1f, tween(300, easing = EaseIn))
            }
            launch {
                cardScale.animateTo(1f, keyframes {
                    durationMillis = 300
                    4f at 0 using EaseIn
                    1.5f at 150 using EaseIn
                    1.2f at 210 using EaseIn
                    1.05f at 255 using EaseIn
                    1f at 300
                })
            }
            launch {
                cardRotation.animateTo(8f, keyframes {
                    durationMillis = 300
                    0f at 0 using EaseIn
                    0f at 150 using EaseIn
                    2f at 210 using EaseIn
                    6f at 255 using EaseIn
                    8f at 300
                })
            }
            launch {
                cardAlpha.animateTo(1f, keyframes {
                    durationMillis = 300
                    0f at 0 using EaseIn
                    0.6f at 150 using EaseIn
                    0.9f at 210 using EaseIn
                    1f at 255 using EaseIn
                    1f at 300
                })
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6000000)) // --modal-drop-bg
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 700.dp)
                .heightIn(min = 360.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            DropEffects(Modifier.matchParentSize(), time.value)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DropIcon(DROP_ICON_PATH, 960f, -960f, TextPrimary, Modifier.size(28.dp).graphicsLayer { alpha = textAlpha.value })
                Text(
                    text = dropCaption(drop),
                    color = TextPrimary,
                    fontSize = 18.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp).graphicsLayer { alpha = textAlpha.value },
                )
                Column(
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp).graphicsLayer { alpha = textAlpha.value },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Посмотреть можно в профиле", color = TextTertiary, fontSize = 14.sp, textAlign = TextAlign.Center)
                    Text(
                        text = drop.probability?.let { "Шанс выпадения $it%" }.orEmpty(),
                        color = TextTertiary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )
                }
                Ornament(Modifier.graphicsLayer { alpha = textAlpha.value })
                Row(
                    modifier = Modifier.padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                ) {
                    DroppedCardView(drop, onClose, onOpenCard, cardScale.value, cardRotation.value, cardAlpha.value)
                }
                Ornament(Modifier.graphicsLayer { alpha = textAlpha.value })
                Row(
                    modifier = Modifier.padding(top = 32.dp).graphicsLayer { alpha = textAlpha.value },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DropIcon(TOUCH_ICON_PATH, 960f, -960f, TextTertiary, Modifier.size(20.dp))
                    Text("Нажми, чтобы продолжить", color = TextTertiary, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun DroppedCardView(drop: CardDrop, onClose: () -> Unit, onOpenCard: (String) -> Unit, scale: Float, rotation: Float, alpha: Float) {
    Box(
        modifier = Modifier
            .width(160.dp)
            .aspectRatio(2f / 3f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
                this.alpha = alpha
            }
            .clip(RoundedCornerShape(CardRadius))
            .background(CardBackground)
            .clickable {
                onClose()
                onOpenCard(drop.slug)
            },
    ) {
        AsyncImage(
            model = drop.image,
            contentDescription = drop.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (drop.quantity > 1) {
            val noun = if (drop.shard) plural(drop.quantity, "осколок", "осколка", "осколков") else plural(drop.quantity, "карта", "карты", "карт")
            Text(
                text = "${drop.quantity} $noun",
                color = TextPrimary,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(8.dp)
                    .background(Color(0x99000000), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0x26F1F1F1), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
    }
}

/** Подпись как на сайте: «Получена новая карта!», «Получены новые карты и осколки!», «Получен новый осколок!» и т.п. */
private fun dropCaption(drop: CardDrop): String {
    val cards = if (drop.shard) 0 else drop.quantity
    val shards = if (drop.shard) drop.quantity else 0
    val total = cards + shards
    val words = mutableListOf("Получен" + if (total == 1) (if (cards == 1) "а" else "") else "ы")
    words += if (cards == 0) "нов" + (if (total == 1) "ый" else "ые") else "нов" + (if (total == 1) "ая" else "ые")
    if (cards != 0) words += "карт" + if (cards == 1) "а" else "ы"
    if (cards != 0 && shards != 0) words += "и"
    if (shards != 0) words += "осколок" + if (shards == 1) "" else "и"
    return words.joinToString(" ") + "!"
}

private fun plural(n: Int, one: String, few: String, many: String): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod10 == 1 && mod100 != 11 -> one
        mod10 in 2..4 && mod100 !in 12..14 -> few
        else -> many
    }
}

// ---------------------------------------------------------------- рисованные элементы

@Composable
private fun DropIcon(pathData: String, width: Float, minY: Float, color: Color, modifier: Modifier) {
    val path = remember(pathData) { PathParser().parsePathString(pathData).toPath() }
    Canvas(modifier) {
        // viewBox "0 -960 960 960": сдвигаем вниз на 960 и масштабируем под размер
        val k = size.width / width
        scale(k, k, pivot = Offset.Zero) {
            translate(0f, -minY) { drawPath(path, color) }
        }
    }
}

@Composable
private fun Ornament(modifier: Modifier) {
    val fill = remember { PathParser().parsePathString(ORNAMENT_FILL_PATH).toPath() }
    val linesA = remember { PathParser().parsePathString(ORNAMENT_LINE_PATH_A).toPath() }
    val linesB = remember { PathParser().parsePathString(ORNAMENT_LINE_PATH_B).toPath() }
    Canvas(modifier.size(width = 275.dp, height = 34.dp)) {
        val k = density
        scale(k, k, pivot = Offset.Zero) {
            drawPath(fill, OrnamentFill)
            drawPath(linesA, TextPrimary)
            drawPath(linesB, TextPrimary)
        }
    }
}

/** Вспышки (smoothMove) и звёздочки (starFloat) под содержимым окна. [timeMs] — время с открытия окна. */
@Composable
private fun DropEffects(modifier: Modifier, timeMs: Float) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val sparkle: Path = remember { PathParser().parsePathString(SPARKLE_PATH).toPath() }
    Canvas(modifier) {
        val vw = screenWidthDp * density / 100f
        val glareT = ((timeMs - 200f) / 3000f).coerceIn(0f, 1f)
        if (glareT > 0f && glareT < 1f) Glares.forEach { drawGlare(it, glareT, vw) }
        val starT = ((timeMs - 200f) / 1000f).coerceIn(0f, 1f)
        val frac = (starT / 0.99f).coerceAtMost(1f)
        Sparkles.forEach { drawSparkle(sparkle, it, frac, vw) }
    }
}

private fun DrawScope.drawGlare(g: Glare, t: Float, vw: Float) {
    // ключевые кадры: 0% → 40% (сдвиг, рост, opacity .03) → 70% (держится) → 100% (сжатие, opacity 0)
    val moveFrac: Float
    val alpha: Float
    val size: Float
    when {
        t < 0.4f -> {
            val e = GlareEase.transform(t / 0.4f)
            moveFrac = e
            alpha = 0.03f * e
            size = g.start + (g.mid - g.start) * e
        }
        t < 0.7f -> {
            moveFrac = 1f
            alpha = 0.03f
            size = g.mid
        }
        else -> {
            val e = GlareEase.transform((t - 0.7f) / 0.3f)
            moveFrac = 1f
            alpha = 0.03f * (1f - e)
            size = g.mid + (g.shrink - g.mid) * e
        }
    }
    val angle = Math.toRadians(g.angle.toDouble())
    val dist = g.distVw * vw * moveFrac
    drawCircle(
        color = g.color.copy(alpha = alpha.coerceIn(0f, 1f)),
        radius = size * density / 2f,
        center = Offset(
            this.size.width * g.left + (cos(angle) * dist).toFloat(),
            this.size.height * g.top + (sin(angle) * dist).toFloat(),
        ),
    )
}

private fun DrawScope.drawSparkle(path: Path, s: Sparkle, frac: Float, vw: Float) {
    val angle = Math.toRadians(s.angle.toDouble())
    val dist = s.distVw * vw * frac
    val cx = size.width * s.left + (cos(angle) * dist).toFloat()
    val cy = size.height * s.top + (sin(angle) * dist).toFloat()
    val sizePx = (s.start + (s.mid - s.start) * frac) * density
    translate(cx - sizePx / 2f, cy - sizePx / 2f) {
        rotate(s.rotation, pivot = Offset(sizePx / 2f, sizePx / 2f)) {
            scale(sizePx / 12f, sizePx / 12f, pivot = Offset.Zero) {
                drawPath(path, TextPrimary)
            }
        }
    }
}
