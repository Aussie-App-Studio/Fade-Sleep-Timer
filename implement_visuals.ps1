$file = "app\src\main\java\com\example\fadesleeptimer\MainActivity.kt"
$content = Get-Content $file -Raw

# 1. Add Imports
$imports = @"
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay
"@
$content = $content.Replace("import androidx.compose.material3.*", "import androidx.compose.material3.*`r`n$imports")

# 2. Insert Logic before permissions check
$marker = "onDispose {
            sensorManager.unregisterListener(listener)
        }"

$logic = @"
        onDispose {
            sensorManager.unregisterListener(listener)
        }

        // OLED Burn-in Protection
        var burnInOffset by remember { mutableStateOf(IntOffset(0, 0)) }
        LaunchedEffect(settings.burnInProtection) {
            while (settings.burnInProtection) {
                delay(60000) // 1 minute
                val x = (-3..3).random()
                val y = (-3..3).random()
                burnInOffset = IntOffset(x, y)
            }
            if (!settings.burnInProtection) {
                burnInOffset = IntOffset(0, 0)
            }
        }

        // Monochrome Logic
        val isMonochrome = settings.monochromeMode == MonochromeMode.IN_APP_ALWAYS || 
                           (settings.monochromeMode == MonochromeMode.IN_APP_ON_TIMER && isRunning)

        // Root Container for Global Effects
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { burnInOffset }
        ) {
"@

$content = $content.Replace($marker, $logic)

# 3. Close the Box at the end of FadeSleepTimerApp
# The function ends with a closing brace. We need to add another one before it.
# We need to find the end of the function.
# It ends with:
#     }
# }
# (End of else block, end of function)

# Actually, the structure is:
# if (!hasPermissions) { ... }
# else if (showSettings) { ... }
# else { ... }
#
# So we need to close the Box after the else block.

$endMarker = "    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // HEADER
"
# Wait, I can't easily find the end of the else block with simple replace.
# But I can append the closing brace and the Monochrome overlay at the end of the Box content.
# The Box wraps the *entire* if/else structure.

# Let's look for the end of the file.
# The last few lines are:
# }
# 
# @Composable
# fun OnIndicatorPill() {

# I'll look for the start of OnIndicatorPill and insert before it.
$pillMarker = "@Composable
fun OnIndicatorPill() {"

$closingLogic = @"
            // Monochrome Overlay
            if (isMonochrome) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        color = Color.Gray, // Color doesn't matter much for Saturation mode, but Gray/Black is standard
                        blendMode = BlendMode.Saturation
                    )
                }
            }
        } // End of Root Box

@Composable
fun OnIndicatorPill() {"

$content = $content.Replace($pillMarker, $closingLogic)

Set-Content $file $content -NoNewline
Write-Host "Implemented Burn-in and Monochrome!"
