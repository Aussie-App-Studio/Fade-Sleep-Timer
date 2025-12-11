$file = "app\src\main\java\com\example\fadesleeptimer\MainActivity.kt"
$content = Get-Content $file -Raw

# 1. Fix DisposableEffect closure
# I inserted code after onDispose but before the closing brace of DisposableEffect.
# I need to insert '}' before '// OLED Burn-in Protection'
$content = $content.Replace("// OLED Burn-in Protection", "} `r`n        // OLED Burn-in Protection")

# 2. Add Imports
$imports = @"
import com.example.fadesleeptimer.ui.MonochromeMode
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
"@
$content = $content.Replace("import androidx.compose.material3.*", "import androidx.compose.material3.*`r`n$imports")

Set-Content $file $content -NoNewline
Write-Host "Fixed DisposableEffect and Imports!"
