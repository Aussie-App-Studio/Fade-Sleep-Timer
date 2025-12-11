$file = "app\src\main\java\com\example\fadesleeptimer\MainActivity.kt"
$content = Get-Content $file -Raw

# Add BackHandler to Settings screen
$old = @"
    } else if (showSettings) {
        SettingsScreen(
            settings = settings,
            onSettingsChanged = { viewModel.updateSettings(it) },
            onBack = { showSettings = false }
        )
"@

$new = @"
    } else if (showSettings) {
        BackHandler {
            showSettings = false
        }
        SettingsScreen(
            settings = settings,
            onSettingsChanged = { viewModel.updateSettings(it) },
            onBack = { showSettings = false }
        )
"@

$content = $content -replace [regex]::Escape($old), $new
Set-Content $file $content -NoNewline
Write-Host "Added BackHandler to Settings screen!"
