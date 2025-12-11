$file = "app\src\main\java\com\example\fadesleeptimer\MainActivity.kt"
$content = Get-Content $file -Raw

$old = @"
        SettingsScreen(
            settings = settings,
            onSettingsChanged = { viewModel.updateSettings(it) },
            onBack = { showSettings = false }
        )
"@

$new = @"
        SettingsScreen(
            settings = settings,
            onSettingsChanged = { viewModel.updateSettings(it) },
            onBack = { showSettings = false },
            isTimerActive = isRunning,
            timeLeft = timeLeft,
            onStopTimer = { viewModel.stopTimer(context) }
        )
"@

$content = $content -replace [regex]::Escape($old), $new
Set-Content $file $content -NoNewline
Write-Host "Connected SettingsScreen to timer state!"
