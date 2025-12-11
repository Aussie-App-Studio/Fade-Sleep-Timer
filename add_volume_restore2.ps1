$file = "app\src\main\java\com\example\fadesleeptimer\service\TimerService.kt"
$lines = Get-Content $file

# Find the line with simulateMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
$lineIndex = -1
for ($i = 0; $i -lt $lines.Count; $i++) {
    if ($lines[$i] -match 'simulateMediaKey\(KeyEvent\.KEYCODE_MEDIA_PAUSE\)') {
        $lineIndex = $i
        break
    }
}

if ($lineIndex -ne -1) {
    # Insert two new lines after the simulateMediaKey line
    $newLines = @(
        "        ",
        "        // Restore initial volume after pausing",
        "        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, initialVolume, 0)"
    )
    
    $lines = $lines[0..$lineIndex] + $newLines + $lines[($lineIndex + 1)..($lines.Count - 1)]
    
    # Write back to file
    $lines | Set-Content $file
    Write-Host "Successfully added volume restoration!"
} else {
    Write-Host "Could not find the target line"
}
