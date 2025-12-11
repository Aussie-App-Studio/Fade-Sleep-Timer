# Fix backup2 by inserting missing lines
with open('app/src/main/java/com/example/fadesleeptimer/MainActivity.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Insert missing code after line 354 (index 353)
# Lines 354-358 currently are:
# 354:         }
# 355: 
# 356:                 .background(Slate950)
# 357:                 .padding(16.dp)
# 358:         ) {

# We need to insert between 354 and 356
insert_at = 354  # After line 354 (0-indexed: 353)

missing_code = """
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
"""

# Remove the orphaned lines 355-357
# Keep line 354 and line 358+
new_lines = lines[:354] + [missing_code] + lines[357:]

with open('app/src/main/java/com/example/fadesleeptimer/MainActivity.kt', 'w', encoding='utf-8') as f:
    f.writelines(new_lines)

print("Fixed!")
