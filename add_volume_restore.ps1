$file = "app\src\main\java\com\example\fadesleeptimer\service\TimerService.kt"
$content = Get-Content $file -Raw

# Add volume restoration after the pause command
$old = @"
    // Also send PAUSE key for good measure
    simulateMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
}
"@

$new = @"
    // Also send PAUSE key for good measure
    simulateMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
    
    // Restore initial volume after pausing
    audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, initialVolume, 0)
}
"@

$content = $content -replace [regex]::Escape($old), $new
Set-Content $file $content -NoNewline
Write-Host "Added volume restoration to stopPlayback!"
