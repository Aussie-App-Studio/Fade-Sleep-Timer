$file = "app\src\main\java\com\example\fadesleeptimer\MainActivity.kt"
$content = Get-Content $file -Raw

# Replace the broken section
$old = @"
        if (settings.pocketMode) {
            sensorManager.registerListener(listener, proximity, SensorManager.SENSOR_DELAY_NORMAL)
        }

                .background(Slate950)
                .padding(16.dp)
        ) {
"@

$new = @"
        if (settings.pocketMode) {
            sensorManager.registerListener(listener, proximity, SensorManager.SENSOR_DELAY_NORMAL)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Check permissions initially
    if (!hasPermissions) {
        PermissionsScreen(onAllGranted = { hasPermissions = true })
    } else if (showSettings) {
        SettingsScreen(
            settings = settings,
            onSettingsChanged = { viewModel.updateSettings(it) },
            onBack = { showSettings = false }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950)
                .padding(16.dp)
        ) {
"@

$content = $content -replace [regex]::Escape($old), $new
Set-Content $file $content -NoNewline
Write-Host "Fixed!"
