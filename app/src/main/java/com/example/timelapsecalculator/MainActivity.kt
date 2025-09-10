package com.example.timelapsecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FiniteAnimationSpec

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			var isDarkMode by remember { mutableStateOf(false) }
			var settingsExpanded by remember { mutableStateOf(false) }
			val lightColors = lightColorScheme(
				primary = Color(0xFF5E46A3),
				secondary = Color(0xFFE9DDFB),
				background = Color(0xFFFAF4FF),
				surface = Color.White,
				onSurface = Color.Black
			)
			val darkColors = darkColorScheme(
				primary = Color(0xFFBDA6FF),
				secondary = Color(0xFF3B2E63),
				background = Color(0xFF121212),
				surface = Color(0xFF1E1E1E),
				onSurface = Color.White
			)
			Crossfade(targetState = isDarkMode, animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)) { dark ->
				MaterialTheme(colorScheme = if (dark) darkColors else lightColors) {
					Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
						TimelapseScreen(
							isDarkMode = dark,
							settingsExpanded = settingsExpanded,
							setSettingsExpanded = { settingsExpanded = it },
							onToggleDark = { isDarkMode = !isDarkMode }
						)
					}
				}
			}
		}
	}
}

private enum class Mode { Interval, Video, Shoot }

private enum class Language(val leftLabel: String, val rightText: String, val flag: String) {
	DE(leftLabel = "Sprache", rightText = "Deutsch", flag = "\uD83C\uDDE9\uD83C\uDDEA"),
	EN(leftLabel = "Language", rightText = "English", flag = "\uD83C\uDDEC\uD83C\uDDE7"),
	RU(leftLabel = "Язык", rightText = "Русский", flag = "\uD83C\uDDF7\uD83C\uDDFA");

	fun next(): Language = when (this) {
		DE -> EN
		EN -> RU
		RU -> DE
	}
}

private data class Strings(
	val locale: java.util.Locale,
	val appTitle: String,
	val lightMode: String,
	val darkMode: String,
	val about: String,
	val tabInterval: String,
	val tabVideo: String,
	val tabShoot: String,
	val fpsLabel: String,
	val imageSizeMbLabel: String,
	val labelVideoDuration: String,
	val labelShootDuration: String,
	val labelInterval: String,
	val errorEnterVideoAndShoot: String,
	val errorEnterVideo: String,
	val errorEnterShoot: String,
	val errorEnterIntervalAndShoot: String,
	val errorEnterInterval: String,
	val errorEnterIntervalAndVideo: String,
	val buttonCalculate: String,
	val resultIntervalPrefix: String,
	val resultVideoPrefix: String,
	val resultShootPrefix: String,
	val photosCountPrefix: String,
	val storageSizePrefix: String,
)

private fun stringsFor(language: Language): Strings = when (language) {
	Language.DE -> Strings(
		locale = java.util.Locale.GERMANY,
		appTitle = "Timelapse Rechner",
		lightMode = "Heller Modus",
		darkMode = "Dunkler Modus",
		about = "Über",
		tabInterval = "Intervall",
		tabVideo = "Videodauer",
		tabShoot = "Drehzeit",
		fpsLabel = "FPS (Frames pro Sekunde)",
		imageSizeMbLabel = "Bildgröße pro Foto (MB)",
		labelVideoDuration = "Videodauer",
		labelShootDuration = "Drehzeit",
		labelInterval = "Intervall",
		errorEnterVideoAndShoot = "Bitte Videodauer und Drehzeit eingeben.",
		errorEnterVideo = "Bitte Videodauer eingeben.",
		errorEnterShoot = "Bitte Drehzeit eingeben.",
		errorEnterIntervalAndShoot = "Bitte Intervall und Drehzeit eingeben.",
		errorEnterInterval = "Bitte Intervall eingeben.",
		errorEnterIntervalAndVideo = "Bitte Intervall und Videodauer eingeben.",
		buttonCalculate = "Berechnen",
		resultIntervalPrefix = "Intervall (s): ",
		resultVideoPrefix = "Videodauer (s): ",
		resultShootPrefix = "Drehzeit (s): ",
		photosCountPrefix = "Anzahl Fotos: ",
		storageSizePrefix = "Speichergröße: "
	)
	Language.EN -> Strings(
		locale = java.util.Locale.UK,
		appTitle = "Timelapse Calculator",
		lightMode = "Light Mode",
		darkMode = "Dark Mode",
		about = "About",
		tabInterval = "Interval",
		tabVideo = "Video duration",
		tabShoot = "Shooting time",
		fpsLabel = "FPS (frames per second)",
		imageSizeMbLabel = "Image size per photo (MB)",
		labelVideoDuration = "Video duration",
		labelShootDuration = "Shooting time",
		labelInterval = "Interval",
		errorEnterVideoAndShoot = "Please enter video duration and shooting time.",
		errorEnterVideo = "Please enter video duration.",
		errorEnterShoot = "Please enter shooting time.",
		errorEnterIntervalAndShoot = "Please enter interval and shooting time.",
		errorEnterInterval = "Please enter interval.",
		errorEnterIntervalAndVideo = "Please enter interval and video duration.",
		buttonCalculate = "Calculate",
		resultIntervalPrefix = "Interval (s): ",
		resultVideoPrefix = "Video duration (s): ",
		resultShootPrefix = "Shooting time (s): ",
		photosCountPrefix = "Number of photos: ",
		storageSizePrefix = "Storage size: "
	)
	Language.RU -> Strings(
		locale = java.util.Locale("ru", "RU"),
		appTitle = "Таймлапс калькулятор",
		lightMode = "Светлая тема",
		darkMode = "Тёмная тема",
		about = "О приложении",
		tabInterval = "Интервал",
		tabVideo = "Длительность\nвидео",
		tabShoot = "Время съёмки",
		fpsLabel = "FPS (кадров в секунду)",
		imageSizeMbLabel = "Размер фото (МБ)",
		labelVideoDuration = "Длительность видео",
		labelShootDuration = "Время съёмки",
		labelInterval = "Интервал",
		errorEnterVideoAndShoot = "Пожалуйста, введите длительность видео и время съёмки.",
		errorEnterVideo = "Пожалуйста, введите длительность видео.",
		errorEnterShoot = "Пожалуйста, введите время съёмки.",
		errorEnterIntervalAndShoot = "Пожалуйста, введите интервал и время съёмки.",
		errorEnterInterval = "Пожалуйста, введите интервал.",
		errorEnterIntervalAndVideo = "Пожалуйста, введите интервал и длительность видео.",
		buttonCalculate = "Рассчитать",
		resultIntervalPrefix = "Интервал (с): ",
		resultVideoPrefix = "Длительность видео (с): ",
		resultShootPrefix = "Время съёмки (с): ",
		photosCountPrefix = "Количество фото: ",
		storageSizePrefix = "Объём памяти: "
	)
}

private fun Double.format(decimals: Int, locale: java.util.Locale): String {
	return String.format(locale, "%1$.${'$'}{decimals}f", this)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelapseScreen(
    isDarkMode: Boolean,
    settingsExpanded: Boolean,
    setSettingsExpanded: (Boolean) -> Unit,
    onToggleDark: () -> Unit
) {
	var selectedTab by remember { mutableStateOf(Mode.Interval) }
	var fps by remember { mutableStateOf("") }
	var sizeMb by remember { mutableStateOf("") }
	// Split time inputs into hours/minutes/seconds
	var intervalH by remember { mutableStateOf("") }
	var intervalM by remember { mutableStateOf("") }
	var intervalS by remember { mutableStateOf("") }
	var videoH by remember { mutableStateOf("") }
	var videoM by remember { mutableStateOf("") }
	var videoS by remember { mutableStateOf("") }
	var shootH by remember { mutableStateOf("") }
	var shootM by remember { mutableStateOf("") }
	var shootS by remember { mutableStateOf("") }

	var resultPrimary by remember { mutableStateOf("") }
	var resultPhotos by remember { mutableStateOf("") }
	var resultStorage by remember { mutableStateOf("") }
	var errorMessage by remember { mutableStateOf("") }

	var language by remember { mutableStateOf(Language.DE) }
	val strings = stringsFor(language)

	// Smoother fade for language changes
	val textFadeSpec: FiniteAnimationSpec<Float> = tween(durationMillis = 420, easing = FastOutSlowInEasing)
	val textFadeIn = fadeIn(animationSpec = textFadeSpec)
	val textFadeOut = fadeOut(animationSpec = textFadeSpec)

	val gearRotation by animateFloatAsState(
		targetValue = if (settingsExpanded) 90f else 0f,
		animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
		label = "gearRotation"
	)

	fun closeDropdown() {
		if (settingsExpanded) setSettingsExpanded(false)
	}

	fun computeError(): String {
		fun isTripleEmpty(h: String, m: String, s: String): Boolean {
			return h.isBlank() && m.isBlank() && s.isBlank()
		}
		return when (selectedTab) {
			Mode.Interval -> {
				val missingVideo = isTripleEmpty(videoH, videoM, videoS)
				val missingShoot = isTripleEmpty(shootH, shootM, shootS)
				when {
					missingVideo && missingShoot -> strings.errorEnterVideoAndShoot
					missingVideo -> strings.errorEnterVideo
					missingShoot -> strings.errorEnterShoot
					else -> ""
				}
			}
			Mode.Video -> {
				val missingInterval = isTripleEmpty(intervalH, intervalM, intervalS)
				val missingShoot = isTripleEmpty(shootH, shootM, shootS)
				when {
					missingInterval && missingShoot -> strings.errorEnterIntervalAndShoot
					missingInterval -> strings.errorEnterInterval
					missingShoot -> strings.errorEnterShoot
					else -> ""
				}
			}
			Mode.Shoot -> {
				val missingInterval = isTripleEmpty(intervalH, intervalM, intervalS)
				val missingVideo = isTripleEmpty(videoH, videoM, videoS)
				when {
					missingInterval && missingVideo -> strings.errorEnterIntervalAndVideo
					missingInterval -> strings.errorEnterInterval
					missingVideo -> strings.errorEnterVideo
					else -> ""
				}
			}
		}
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 16.dp, vertical = 24.dp),
		horizontalAlignment = Alignment.Start,
		verticalArrangement = Arrangement.Top
	) {
		Box(modifier = Modifier.fillMaxWidth()) {
			Crossfade(targetState = language, animationSpec = textFadeSpec) { _ ->
				Text(
					strings.appTitle,
					fontSize = 34.sp,
					fontWeight = FontWeight.Bold,
					modifier = Modifier
						.align(Alignment.CenterStart)
						.fillMaxWidth(),
					textAlign = TextAlign.Start
				)
			}
			IconButton(
				onClick = {
					val next = !settingsExpanded
					setSettingsExpanded(next)
				},
				modifier = Modifier.align(Alignment.CenterEnd)
			) {
				androidx.compose.material3.Icon(
					Icons.Filled.Settings,
					contentDescription = "Einstellungen",
					modifier = Modifier.size(28.dp).rotate(gearRotation),
					tint = MaterialTheme.colorScheme.primary
				)
			}
		}
		androidx.compose.animation.AnimatedVisibility(
			visible = settingsExpanded,
			enter = expandVertically(
				animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
				expandFrom = Alignment.Top
			) + fadeIn(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)),
			exit = shrinkVertically(
				animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
				shrinkTowards = Alignment.CenterVertically
			) + fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 24.dp)
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.clip(RoundedCornerShape(16.dp))
						.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
						.background(MaterialTheme.colorScheme.surface)
				) {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.clickable { onToggleDark() }
							.padding(horizontal = 16.dp, vertical = 14.dp)
					) {
						Crossfade(targetState = language, animationSpec = textFadeSpec) { _ ->
							val modeText = if (isDarkMode) strings.lightMode else strings.darkMode
							Text(
								modeText,
								color = MaterialTheme.colorScheme.onSurface,
								fontSize = 14.sp,
								fontWeight = FontWeight.SemiBold,
								modifier = Modifier.align(Alignment.CenterStart)
							)
						}
						androidx.compose.animation.AnimatedContent(
							targetState = isDarkMode,
							transitionSpec = {
								(fadeIn(animationSpec = tween(240, easing = LinearOutSlowInEasing)) +
									scaleIn(initialScale = 0.94f)) togetherWith
								fadeOut(animationSpec = tween(120, easing = FastOutLinearInEasing))
							},
							label = "bulbTransition",
							modifier = Modifier.align(Alignment.CenterEnd)
						) { dark ->
							androidx.compose.material3.Icon(
								Icons.Outlined.Lightbulb,
								contentDescription = "Theme",
								modifier = Modifier.size(18.dp),
								tint = if (dark) Color.White else Color.Black
							)
						}
					}
					HorizontalDivider()
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.clickable { language = language.next() }
							.padding(horizontal = 16.dp, vertical = 14.dp)
					) {
						androidx.compose.animation.AnimatedContent(
							targetState = language.leftLabel,
							transitionSpec = { textFadeIn togetherWith textFadeOut },
							label = "langLeftTransition",
							modifier = Modifier.align(Alignment.CenterStart)
						) { left ->
							Text(left, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
						}
						androidx.compose.animation.AnimatedContent(
							targetState = language,
							transitionSpec = { textFadeIn togetherWith textFadeOut },
							label = "langRightTransition",
							modifier = Modifier.align(Alignment.CenterEnd)
						) { lang ->
							Row(verticalAlignment = Alignment.CenterVertically) {
								Text(lang.rightText, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
								Spacer(Modifier.width(8.dp))
								Text(lang.flag, fontSize = 18.sp)
							}
						}
					}
					HorizontalDivider()
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.clickable { /* TODO: about action */ }
							.padding(horizontal = 16.dp, vertical = 14.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Crossfade(targetState = language, animationSpec = textFadeSpec) { _ ->
							Text(strings.about, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
						}
					}
				}
			}
		}
		// keep uniform spacing only when dropdown is collapsed
		if (!settingsExpanded) {
			Spacer(Modifier.height(24.dp))
		}

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(44.dp)
				.clip(RoundedCornerShape(24.dp))
				.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
		) {
			Row(modifier = Modifier.fillMaxWidth()) {
				listOf(Mode.Interval, Mode.Video, Mode.Shoot).forEachIndexed { index, mode ->
					val selected = selectedTab == mode
					val textOffset by animateDpAsState(
						targetValue = if (selected) 6.dp else 0.dp,
						animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
						label = "tabTextOffset"
					)
					Box(
						modifier = Modifier
							.weight(1f)
							.fillMaxHeight()
							.background(if (selected) Color(0xFFE9DDFB) else Color.Transparent)
							.clickable { closeDropdown(); selectedTab = mode; errorMessage = "" }
					) {
						androidx.compose.animation.AnimatedVisibility(
							visible = selected,
							enter = fadeIn() + scaleIn(initialScale = 0.8f),
							exit = fadeOut() + scaleOut(targetScale = 0.8f),
							modifier = Modifier
								.align(Alignment.CenterStart)
								.padding(start = 10.dp)
						) {
							androidx.compose.material3.Icon(
								Icons.Filled.Check,
								contentDescription = null,
								tint = Color(0xFF5E46A3),
								modifier = Modifier.size(16.dp)
							)
						}

						if (language == Language.RU && mode == Mode.Video) {
							Text(
								"Длительность\nвидео",
								color = if (selected) Color.Black else MaterialTheme.colorScheme.onSurface,
								fontSize = 11.sp,
								fontWeight = FontWeight.SemiBold,
								maxLines = 2,
								overflow = TextOverflow.Clip,
								textAlign = TextAlign.Center,
								style = androidx.compose.ui.text.TextStyle(lineHeight = 11.sp),
								modifier = Modifier
									.align(Alignment.Center)
									.offset(x = textOffset)
							)
						} else {
 							Text(
 								when (mode) {
 									Mode.Interval -> strings.tabInterval
 									Mode.Video -> strings.tabVideo
 									Mode.Shoot -> strings.tabShoot
 								},
 								color = if (selected) Color.Black else MaterialTheme.colorScheme.onSurface,
 								fontSize = 12.sp,
 								fontWeight = FontWeight.SemiBold,
 								maxLines = 1,
 								overflow = TextOverflow.Ellipsis,
 								textAlign = TextAlign.Center,
 								modifier = Modifier
 									.align(Alignment.Center)
 									.offset(x = textOffset)
 							)
 						}
					}
					if (index < 2) {
						Box(
							modifier = Modifier
								.width(1.dp)
								.fillMaxHeight()
								.background(MaterialTheme.colorScheme.outline)
						)
					}
				}
			}
		}

		Spacer(Modifier.height(16.dp))

		OutlinedTextField(
			value = fps,
			onValueChange = { closeDropdown(); fps = it.filter { c -> c.isDigit() } },
			label = { Crossfade(targetState = language, animationSpec = textFadeSpec) { _ -> Text(strings.fpsLabel, color = MaterialTheme.colorScheme.onSurface) } },
			singleLine = true,
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
			modifier = Modifier.fillMaxWidth().onFocusChanged { if (it.isFocused) closeDropdown() },
			colors = OutlinedTextFieldDefaults.colors()
		)

		Spacer(Modifier.height(12.dp))
		OutlinedTextField(
			value = sizeMb,
			onValueChange = { closeDropdown(); sizeMb = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
			label = { Crossfade(targetState = language, animationSpec = textFadeSpec) { _ -> Text(strings.imageSizeMbLabel, color = MaterialTheme.colorScheme.onSurface) } },
			singleLine = true,
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
			modifier = Modifier.fillMaxWidth().onFocusChanged { if (it.isFocused) closeDropdown() }
		)

		Spacer(Modifier.height(16.dp))

		// Localize placeholders and units for time fields
		val hPh = if (language == Language.RU) "чч" else "hh"
		val mPh = if (language == Language.RU) "мм" else "mm"
		val sPh = if (language == Language.RU) "сс" else "ss"
		val hUnit = if (language == Language.RU) "ч" else "h"
		val mUnit = if (language == Language.RU) "мин" else "min"
		val sUnit = if (language == Language.RU) "с" else "s"

		when (selectedTab) {
			Mode.Interval -> {
				TimeInputRow(
					label = strings.labelVideoDuration,
					hText = videoH,
					mText = videoM,
					sText = videoS,
					onHChange = { closeDropdown(); videoH = it.filter { c -> c.isDigit() }; errorMessage = computeError() },
					onMChange = { closeDropdown(); videoM = it.filter { c -> c.isDigit() }; errorMessage = computeError() },
					onSChange = { closeDropdown(); videoS = it.filter { c -> c.isDigit() }; errorMessage = computeError() },
					onAnyFocus = { closeDropdown() },
					hPlaceholder = hPh, mPlaceholder = mPh, sPlaceholder = sPh,
					hUnit = hUnit, mUnit = mUnit, sUnit = sUnit,
				)
				Spacer(Modifier.height(12.dp))
				TimeInputRow(
					label = strings.labelShootDuration,
					hText = shootH,
					mText = shootM,
					sText = shootS,
					onHChange = { closeDropdown(); shootH = it.filter { c -> c.isDigit() }; errorMessage = computeError() },
					onMChange = { closeDropdown(); shootM = it.filter { c -> c.isDigit() }; errorMessage = computeError() },
					onSChange = { closeDropdown(); shootS = it.filter { c -> c.isDigit() }; errorMessage = computeError() },
					onAnyFocus = { closeDropdown() },
					hPlaceholder = hPh, mPlaceholder = mPh, sPlaceholder = sPh,
					hUnit = hUnit, mUnit = mUnit, sUnit = sUnit,
				)
			}
			Mode.Video -> {
				TimeInputRow(
					label = strings.labelInterval,
					hText = intervalH,
					mText = intervalM,
					sText = intervalS,
					onHChange = { closeDropdown(); intervalH = it.filter { c -> c.isDigit() }; errorMessage = computeError() },
					onMChange = { closeDropdown(); intervalM = it.filter { c -> c.isDigit() }; errorMessage = computeError() },
					onSChange = { closeDropdown(); intervalS = it.filter { c -> c.isDigit() }; errorMessage = computeError() },
					onAnyFocus = { closeDropdown() },
					hPlaceholder = hPh, mPlaceholder = mPh, sPlaceholder = sPh,
					hUnit = hUnit, mUnit = mUnit, sUnit = sUnit,
				)
				Spacer(Modifier.height(12.dp))
				TimeInputRow(
					label = strings.labelShootDuration,
					hText = shootH,
					mText = shootM,
					sText = shootS,
					onHChange = { closeDropdown(); shootH = it.filter { c -> c.isDigit() }; errorMessage = computeError() },
					onMChange = { closeDropdown(); shootM = it.filter { c -> c.isDigit() }; errorMessage = computeError() },
					onSChange = { closeDropdown(); shootS = it.filter { c -> c.isDigit() }; errorMessage = computeError() },
					onAnyFocus = { closeDropdown() },
					hPlaceholder = hPh, mPlaceholder = mPh, sPlaceholder = sPh,
					hUnit = hUnit, mUnit = mUnit, sUnit = sUnit,
				)
			}
			Mode.Shoot -> {
				TimeInputRow(
					label = strings.labelInterval,
					hText = intervalH,
					mText = intervalM,
					sText = intervalS,
					onHChange = { closeDropdown(); intervalH = it.filter { c -> c.isDigit() }; errorMessage = computeError() },
					onMChange = { closeDropdown(); intervalM = it.filter { c -> c.isDigit() }; errorMessage = computeError() },
					onSChange = { closeDropdown(); intervalS = it.filter { c -> c.isDigit() }; errorMessage = computeError() },
					onAnyFocus = { closeDropdown() },
					hPlaceholder = hPh, mPlaceholder = mPh, sPlaceholder = sPh,
					hUnit = hUnit, mUnit = mUnit, sUnit = sUnit,
				)
				Spacer(Modifier.height(12.dp))
				TimeInputRow(
					label = strings.labelVideoDuration,
					hText = videoH,
					mText = videoM,
					sText = videoS,
					onHChange = { closeDropdown(); videoH = it.filter { c -> c.isDigit() }; errorMessage = computeError() },
					onMChange = { closeDropdown(); videoM = it.filter { c -> c.isDigit() }; errorMessage = computeError() },
					onSChange = { closeDropdown(); videoS = it.filter { c -> c.isDigit() }; errorMessage = computeError() },
					onAnyFocus = { closeDropdown() },
					hPlaceholder = hPh, mPlaceholder = mPh, sPlaceholder = sPh,
					hUnit = hUnit, mUnit = mUnit, sUnit = sUnit,
				)
			}
		}

		// Error message area above the button
		if (errorMessage.isNotBlank()) {
			Spacer(Modifier.height(8.dp))
			Crossfade(targetState = Pair(language, errorMessage), animationSpec = textFadeSpec) { (_, msg) ->
				Text(msg, color = Color(0xFFD32F2F), fontSize = 14.sp)
			}
		}

		Spacer(Modifier.height(20.dp))

		Button(
			onClick = {
				// Validate required inputs per mode
				errorMessage = computeError()
				if (errorMessage.isNotBlank()) return@Button

				fun parseDecimal(text: String): Double {
					if (text.isBlank()) return Double.NaN
					val normalized = text.replace(',', '.')
					return normalized.toDoubleOrNull() ?: Double.NaN
				}

				val parsedFps = parseDecimal(fps)
				val fpsProvided = !parsedFps.isNaN()
				val fpsEffective = if (fpsProvided) parsedFps else 30.0
				val parsedSize = parseDecimal(sizeMb)
				val sizeProvided = !parsedSize.isNaN()
				val sizeVal = if (sizeProvided) parsedSize else Double.NaN
				fun tripleToSeconds(h: String, m: String, s: String): Double {
					val hh = h.toDoubleOrNull() ?: 0.0
					val mm = m.toDoubleOrNull() ?: 0.0
					val ss = s.toDoubleOrNull() ?: 0.0
					return hh * 3600.0 + mm * 60.0 + ss
				}
				fun secondsToTriple(totalSeconds: Double): Triple<String,String,String> {
					if (totalSeconds.isNaN() || totalSeconds < 0) return Triple("","","")
					val total = totalSeconds.toLong()
					val hh = total / 3600
					val rem = total % 3600
					val mm = rem / 60
					val ss = rem % 60
					return Triple(hh.toString(), mm.toString(), ss.toString())
				}
				var interval = tripleToSeconds(intervalH, intervalM, intervalS)
				var video = tripleToSeconds(videoH, videoM, videoS)
				var shoot = tripleToSeconds(shootH, shootM, shootS)

				// Reset optional results; set them conditionally below
				resultPhotos = ""
				resultStorage = ""

				when (selectedTab) {
					Mode.Interval -> {
						if (!video.isNaN() && !shoot.isNaN() && fpsEffective > 0) {
							val frames = (video * fpsEffective)
							interval = if (frames > 0) shoot / frames else Double.NaN
							val (ih, im, isec) = secondsToTriple(interval)
							intervalH = ih; intervalM = im; intervalS = isec
							val photos = frames.toInt()
							resultPrimary = strings.resultIntervalPrefix + interval.format(3, strings.locale)
							if (fpsProvided) {
								resultPhotos = strings.photosCountPrefix + photos.toString()
							}
							if (sizeProvided && !sizeVal.isNaN()) {
								val storage = photos * sizeVal
								resultStorage = strings.storageSizePrefix + storage.format(2, strings.locale) + " MB"
							}
						}
					}
					Mode.Video -> {
						if (!interval.isNaN() && !shoot.isNaN() && fpsEffective > 0) {
							val frames = (shoot / interval)
							video = frames / fpsEffective
							val (vh, vm, vs) = secondsToTriple(video)
							videoH = vh; videoM = vm; videoS = vs
							val photos = frames.toInt()
							resultPrimary = strings.resultVideoPrefix + video.format(3, strings.locale)
							if (fpsProvided) {
								resultPhotos = strings.photosCountPrefix + photos.toString()
							}
							if (sizeProvided && !sizeVal.isNaN()) {
								val storage = photos * sizeVal
								resultStorage = strings.storageSizePrefix + storage.format(2, strings.locale) + " MB"
							}
						}
					}
					Mode.Shoot -> {
						if (!interval.isNaN() && !video.isNaN() && fpsEffective > 0) {
							val frames = (video * fpsEffective)
							shoot = frames * interval
							val (sh, sm, ss) = secondsToTriple(shoot)
							shootH = sh; shootM = sm; shootS = ss
							val photos = frames.toInt()
							resultPrimary = strings.resultShootPrefix + shoot.format(3, strings.locale)
							if (fpsProvided) {
								resultPhotos = strings.photosCountPrefix + photos.toString()
							}
							if (sizeProvided && !sizeVal.isNaN()) {
								val storage = photos * sizeVal
								resultStorage = strings.storageSizePrefix + storage.format(2, strings.locale) + " MB"
							}
						}
					}
				}
			},
			modifier = Modifier
				.fillMaxWidth(),
			colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E46A3))
		) {
			Crossfade(targetState = language, animationSpec = textFadeSpec) { _ ->
				Text(strings.buttonCalculate, color = Color.White)
			}
		}

		Spacer(Modifier.height(24.dp))
		if (resultPrimary.isNotBlank()) {
			Crossfade(targetState = Pair(language, resultPrimary), animationSpec = textFadeSpec) { (_, text) ->
				Text(text, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
			}
			if (resultPhotos.isNotBlank()) {
				Spacer(Modifier.height(12.dp))
				Crossfade(targetState = Pair(language, resultPhotos), animationSpec = textFadeSpec) { (_, text) ->
					Text(text, fontSize = 18.sp)
				}
			}
			if (resultStorage.isNotBlank()) {
				Spacer(Modifier.height(8.dp))
				Crossfade(targetState = Pair(language, resultStorage), animationSpec = textFadeSpec) { (_, text) ->
					Text(text, fontSize = 18.sp)
				}
			}
		}
	}
}

@Composable
private fun TimeInputRow(
	label: String,
	 hText: String,
	 mText: String,
	 sText: String,
	 onHChange: (String) -> Unit,
	 onMChange: (String) -> Unit,
	 onSChange: (String) -> Unit,
	 onAnyFocus: () -> Unit = {},
	 hPlaceholder: String,
	 mPlaceholder: String,
	 sPlaceholder: String,
	 hUnit: String,
	 mUnit: String,
	 sUnit: String,
) {
	Column(modifier = Modifier.fillMaxWidth()) {
		Crossfade(targetState = label, animationSpec = tween(240, easing = LinearOutSlowInEasing)) { text ->
			Text(text, color = MaterialTheme.colorScheme.onSurface)
		}
		Spacer(Modifier.height(6.dp))
		Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
			UnitField(value = hText, onChange = onHChange, placeholder = hPlaceholder, unit = hUnit, modifier = Modifier.weight(1f), onFocus = onAnyFocus)
			UnitField(value = mText, onChange = onMChange, placeholder = mPlaceholder, unit = mUnit, modifier = Modifier.weight(1f), onFocus = onAnyFocus)
			UnitField(value = sText, onChange = onSChange, placeholder = sPlaceholder, unit = sUnit, modifier = Modifier.weight(1f), onFocus = onAnyFocus)
		}
	}
}

@Composable
private fun UnitField(
	value: String,
	onChange: (String) -> Unit,
	placeholder: String,
	unit: String,
	modifier: Modifier = Modifier,
	onFocus: () -> Unit = {},
) {
	Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
		OutlinedTextField(
			value = value,
			onValueChange = { text -> onChange(text.filter { c -> c.isDigit() }) },
			singleLine = true,
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
			modifier = Modifier.weight(1f).onFocusChanged { if (it.isFocused) onFocus() },
			placeholder = { Text(placeholder) }
		)
		Spacer(Modifier.width(6.dp))
		Text(unit)
	}
}

private fun Double.formatGerman(): String {
	return String.format(Locale.GERMANY, "%.3f", this)
}

private fun Double.formatGermanShort(): String {
	return String.format(Locale.GERMANY, "%.3f", this)
}

private fun Double.formatMb(): String {
	return String.format(Locale.GERMANY, "%.2f", this)
}
