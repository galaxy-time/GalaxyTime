package jp.lab75.galaxytime.utils

//import android.os.Build
//import androidx.annotation.RequiresApi
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.unit.sp
//
//class MatrixText {
//
//	val numbers = "._-—-·xX0123456789"
//	val letters = "._ABCDEFGHIJKLMNOPQRSTUVWXYZ"
//
//	@Composable
//	fun TypewriterText(
//		texts: List<String>,
//	) {
//
//		var textIndex by remember {
//			mutableStateOf(0)
//		}
//
//		var textToDisplay by remember {
//			mutableStateOf("")
//		}
//
//		val textCharsList: List<List<String>> = remember {
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//				texts.map {
//					it.splitToCodePoints()
//				}
//			} else {
//				texts.map { text ->
//					text.map {
//						it.toString()
//					}
//				}
//			}
//		}
//
//		LaunchedEffect(
//			key1 = texts,
//		) {
//			while (textIndex < textCharsList.size) {
//				textCharsList[textIndex].forEachIndexed { charIndex, _ ->
//					textToDisplay = textCharsList[textIndex]
//						.take(
//							n = charIndex + 1,
//						).joinToString(
//							separator = "",
//						)
//					Log.e("TXT", "$charIndex $textToDisplay")
//					delay(160)
//				}
//				textIndex = (textIndex + 1) % texts.size
//				delay(1000)
//			}
//		}
//
//		Text(
//			text = textToDisplay,
//			fontSize = 24.sp,
//			fontWeight = FontWeight.Bold,
//		)
//	}
//
//	@RequiresApi(Build.VERSION_CODES.N)
//	fun String.splitToCodePoints(): List<String> {
//		return codePoints()
//			.toList()
//			.map {
//				String(Character.toChars(it))
//			}
//	}
//
//}
