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
import androidx.compose.material3.Divider
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			MaterialTheme {
				Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFAF4FF)) {
					TimelapseScreen()
				}
			}
		}
	}
}

private enum class Mode(val title: String) { Interval("Intervall"), Video("Videodauer"), Shoot("Drehzeit") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelapseScreen() {
	var selectedTab by remember { mutableStateOf(Mode.Interval) }
	var fps by remember { mutableStateOf("30") }
	var sizeMb by remember { mutableStateOf("5") }
	var intervalSec by remember { mutableStateOf("") }
	var videoSec by remember { mutableStateOf("") }
	var shootSec by remember { mutableStateOf("") }

	var resultPrimary by remember { mutableStateOf("") }
	var resultPhotos by remember { mutableStateOf("") }
	var resultStorage by remember { mutableStateOf("") }
	var errorMessage by remember { mutableStateOf("") }

	fun computeError(): String {
		return when (selectedTab) {
			Mode.Interval -> {
				val missingVideo = videoSec.isBlank()
				val missingShoot = shootSec.isBlank()
				when {
					missingVideo && missingShoot -> "Bitte Videodauer und Drehzeit eingeben."
					missingVideo -> "Bitte Videodauer eingeben."
					missingShoot -> "Bitte Drehzeit eingeben."
					else -> ""
				}
			}
			Mode.Video -> {
				val missingInterval = intervalSec.isBlank()
				val missingShoot = shootSec.isBlank()
				when {
					missingInterval && missingShoot -> "Bitte Intervall und Drehzeit eingeben."
					missingInterval -> "Bitte Intervall eingeben."
					missingShoot -> "Bitte Drehzeit eingeben."
					else -> ""
				}
			}
			Mode.Shoot -> {
				val missingInterval = intervalSec.isBlank()
				val missingVideo = videoSec.isBlank()
				when {
					missingInterval && missingVideo -> "Bitte Intervall und Videodauer eingeben."
					missingInterval -> "Bitte Intervall eingeben."
					missingVideo -> "Bitte Videodauer eingeben."
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
		Text(
			"Timelapse Rechner",
			fontSize = 34.sp,
			fontWeight = FontWeight.Bold,
			modifier = Modifier.fillMaxWidth(),
			textAlign = TextAlign.Center
		)
		Spacer(Modifier.height(24.dp))

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
							.clickable { selectedTab = mode; errorMessage = "" }
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

						Text(
							mode.title,
							color = Color.Black,
							fontSize = 12.sp,
							fontWeight = FontWeight.SemiBold,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
							modifier = Modifier
								.align(Alignment.Center)
								.offset(x = textOffset)
						)
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
			onValueChange = { fps = it.filter { c -> c.isDigit() } },
			label = { Text("FPS (Frames pro Sekunde)") },
			singleLine = true,
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
			modifier = Modifier.fillMaxWidth(),
			colors = OutlinedTextFieldDefaults.colors()
		)

		Spacer(Modifier.height(12.dp))
		OutlinedTextField(
			value = sizeMb,
			onValueChange = { sizeMb = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
			label = { Text("Bildgröße pro Foto (MB)") },
			singleLine = true,
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
			modifier = Modifier.fillMaxWidth()
		)

		Spacer(Modifier.height(16.dp))

		when (selectedTab) {
			Mode.Interval -> {
				OutlinedTextField(
					value = videoSec,
					onValueChange = {
						videoSec = it.filter { c -> c.isDigit() || c == '.' || c == ',' }
						errorMessage = computeError()
					},
					label = { Text("Videodauer (Sekunden)") },
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					modifier = Modifier.fillMaxWidth()
				)
				Spacer(Modifier.height(12.dp))
				OutlinedTextField(
					value = shootSec,
					onValueChange = {
						shootSec = it.filter { c -> c.isDigit() || c == '.' || c == ',' }
						errorMessage = computeError()
					},
					label = { Text("Drehzeit (Sekunden)") },
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					modifier = Modifier.fillMaxWidth()
				)
			}
			Mode.Video -> {
				OutlinedTextField(
					value = intervalSec,
					onValueChange = {
						intervalSec = it.filter { c -> c.isDigit() || c == '.' || c == ',' }
						errorMessage = computeError()
					},
					label = { Text("Intervall (Sekunden)") },
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					modifier = Modifier.fillMaxWidth()
				)
				Spacer(Modifier.height(12.dp))
				OutlinedTextField(
					value = shootSec,
					onValueChange = {
						shootSec = it.filter { c -> c.isDigit() || c == '.' || c == ',' }
						errorMessage = computeError()
					},
					label = { Text("Drehzeit (Sekunden)") },
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					modifier = Modifier.fillMaxWidth()
				)
			}
			Mode.Shoot -> {
				OutlinedTextField(
					value = intervalSec,
					onValueChange = {
						intervalSec = it.filter { c -> c.isDigit() || c == '.' || c == ',' }
						errorMessage = computeError()
					},
					label = { Text("Intervall (Sekunden)") },
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					modifier = Modifier.fillMaxWidth()
				)
				Spacer(Modifier.height(12.dp))
				OutlinedTextField(
					value = videoSec,
					onValueChange = {
						videoSec = it.filter { c -> c.isDigit() || c == '.' || c == ',' }
						errorMessage = computeError()
					},
					label = { Text("Videodauer (Sekunden)") },
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					modifier = Modifier.fillMaxWidth()
				)
			}
		}

		// Error message area above the button
		if (errorMessage.isNotBlank()) {
			Spacer(Modifier.height(8.dp))
			Text(errorMessage, color = Color(0xFFD32F2F), fontSize = 14.sp)
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

				val fpsVal = parseDecimal(fps)
				val sizeVal = parseDecimal(sizeMb)
				var interval = parseDecimal(intervalSec)
				var video = parseDecimal(videoSec)
				var shoot = parseDecimal(shootSec)

				if (fpsVal.isNaN() || sizeVal.isNaN()) {
					resultPrimary = ""
					resultPhotos = ""
					resultStorage = ""
					return@Button
				}

				when (selectedTab) {
					Mode.Interval -> {
						if (!video.isNaN() && !shoot.isNaN() && fpsVal > 0) {
							val frames = (video * fpsVal)
							interval = if (frames > 0) shoot / frames else Double.NaN
							intervalSec = interval.formatGerman()
							val photos = frames.toInt()
							val storage = photos * sizeVal
							resultPrimary = "Intervall (Sek.): ${interval.formatGermanShort()}"
							resultPhotos = "Anzahl Fotos: ${photos}"
							resultStorage = "Speichergröße: ${storage.formatMb()} MB"
						}
					}
					Mode.Video -> {
						if (!interval.isNaN() && !shoot.isNaN() && fpsVal > 0) {
							val frames = (shoot / interval)
							video = if (fpsVal > 0) frames / fpsVal else Double.NaN
							videoSec = video.formatGerman()
							val photos = frames.toInt()
							val storage = photos * sizeVal
							resultPrimary = "Videodauer (Sek.): ${video.formatGermanShort()}"
							resultPhotos = "Anzahl Fotos: ${photos}"
							resultStorage = "Speichergröße: ${storage.formatMb()} MB"
						}
					}
					Mode.Shoot -> {
						if (!interval.isNaN() && !video.isNaN() && fpsVal > 0) {
							val frames = (video * fpsVal)
							shoot = frames * interval
							shootSec = shoot.formatGerman()
							val photos = frames.toInt()
							val storage = photos * sizeVal
							resultPrimary = "Drehzeit (Sek.): ${shoot.formatGermanShort()}"
							resultPhotos = "Anzahl Fotos: ${photos}"
							resultStorage = "Speichergröße: ${storage.formatMb()} MB"
						}
					}
				}
			},
			modifier = Modifier
				.fillMaxWidth(),
			colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E46A3))
		) {
			Text("Berechnen", color = Color.White)
		}

		Spacer(Modifier.height(24.dp))
		if (resultPrimary.isNotBlank()) {
			Text(resultPrimary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
			Spacer(Modifier.height(12.dp))
			Text(resultPhotos, fontSize = 18.sp)
			Spacer(Modifier.height(8.dp))
			Text(resultStorage, fontSize = 18.sp)
		}
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
