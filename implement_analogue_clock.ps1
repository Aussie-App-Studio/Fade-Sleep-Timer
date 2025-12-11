$file = "app\src\main\java\com\example\fadesleeptimer\MainActivity.kt"
$content = Get-Content $file -Raw

$old = @"
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
            CircularProgressIndicator(
                progress = 1f,
                modifier = Modifier.fillMaxSize(),
                color = Slate800,
                strokeWidth = 8.dp
            )
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxSize(),
                color = Indigo500,
                strokeWidth = 8.dp,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "`${timeLeft / 60}:`${(timeLeft % 60).toString().padStart(2, '0')}",
                    fontSize = 60.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-2).sp
                )
                Text(
                    "REMAINING",
                    color = Slate700,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
"@

$new = @"
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
            if (settings.analogueClock) {
                // Analogue Clock (Shrinking Pie Chart)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Background Circle
                    drawCircle(
                        color = Slate800.copy(alpha = 0.5f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    
                    // The Shrinking Wedge
                    val sweepAngle = 360f * progress
                    drawArc(
                        color = Indigo500,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                }
            } else {
                // Digital Timer (Ring + Text)
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.fillMaxSize(),
                    color = Slate800,
                    strokeWidth = 8.dp
                )
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxSize(),
                    color = Indigo500,
                    strokeWidth = 8.dp,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "`${timeLeft / 60}:`${(timeLeft % 60).toString().padStart(2, '0')}",
                        fontSize = 60.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-2).sp
                    )
                    Text(
                        "REMAINING",
                        color = Slate700,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
"@

# Note: PowerShell string interpolation might mess up ${...} so I escaped them with `
# But wait, in the Here-String @"...", variables are expanded.
# I need to be careful with ${timeLeft...}
# Actually, I'll use simple string replacement with exact matching, but I need to handle the variable expansion in PowerShell.
# A better way is to read the file, find the block start and end, and replace it.

$lines = Get-Content $file
$startLine = 0
$endLine = 0

for ($i = 0; $i -lt $lines.Count; $i++) {
    if ($lines[$i].Trim() -eq 'Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {') {
        $startLine = $i
    }
    if ($startLine -gt 0 -and $lines[$i].Trim() -eq '}' -and $lines[$i-1].Trim() -eq '}') {
        # This is tricky to find the exact closing brace of the Box.
        # The Box ends at line 872 in the previous view_file.
        # Let's rely on the context from view_file.
    }
}

# Let's try a simpler replacement using the exact text I saw in view_file, but escaping special chars for regex.
# Or just use the exact string replacement without regex.

$content = [System.IO.File]::ReadAllText($file)
$oldText = "        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
            CircularProgressIndicator(
                progress = 1f,
                modifier = Modifier.fillMaxSize(),
                color = Slate800,
                strokeWidth = 8.dp
            )
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxSize(),
                color = Indigo500,
                strokeWidth = 8.dp,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = `"`${timeLeft / 60}:`${(timeLeft % 60).toString().padStart(2, '0')}`",
                    fontSize = 60.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-2).sp
                )
                Text(
                    `"REMAINING`",
                    color = Slate700,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }"

# I need to match the exact file content including newlines.
# The view_file output showed standard indentation.

$newText = "        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
            if (settings.analogueClock) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Slate800.copy(alpha = 0.5f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    val sweepAngle = 360f * progress
                    drawArc(
                        color = Indigo500,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                }
            } else {
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.fillMaxSize(),
                    color = Slate800,
                    strokeWidth = 8.dp
                )
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxSize(),
                    color = Indigo500,
                    strokeWidth = 8.dp,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = `"`${timeLeft / 60}:`${(timeLeft % 60).toString().padStart(2, '0')}`",
                        fontSize = 60.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-2).sp
                    )
                    Text(
                        `"REMAINING`",
                        color = Slate700,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }"

# Perform replacement
$content = $content.Replace($oldText, $newText)
[System.IO.File]::WriteAllText($file, $content)
Write-Host "Implemented Analogue Clock!"
