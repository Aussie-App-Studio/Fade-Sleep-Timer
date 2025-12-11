$file = "app\src\main\java\com\example\fadesleeptimer\service\TimerService.kt"
$content = Get-Content $file -Raw

# 1. Add Fields
$fieldsMarker = "private val serviceScope = CoroutineScope(Dispatchers.Main + Job())"
$newFields = "private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var windowManager: android.view.WindowManager? = null
    private var overlayView: android.view.View? = null"
$content = $content.Replace($fieldsMarker, $newFields)

# 2. Add toggleNightFilter method (at the end of class, before closing brace)
# I'll look for the last closing brace.
# But wait, I can just add it before `override fun onBind`.
# Or just append it before the last brace.
# Let's find `override fun onBind` if it exists.
# It's a Service, so it must have onBind.
# Let's check if onBind is in the file.
# I'll view the file to check.
# Actually, I'll just add it after `stopTimerService` or similar.

# Let's verify where to put it.
# I'll put it after `simulateMediaKey` function.

# 3. Update startTimer
$startTimerMarker = "startForeground(NOTIFICATION_ID, createNotification(`"Timer Running: `$durationMinutes min`"))"
$startTimerLogic = "startForeground(NOTIFICATION_ID, createNotification(`"Timer Running: `$durationMinutes min`"))

        // 3. Night Filter
        if (monochromeModeName == `"SYSTEM_NIGHT_FILTER`") {
            toggleNightFilter(true)
        }"
$content = $content.Replace($startTimerMarker, $startTimerLogic)

# 4. Update stopTimerService
$stopTimerMarker = "stopForeground(true)"
$stopTimerLogic = "stopForeground(true)
        toggleNightFilter(false)"
$content = $content.Replace($stopTimerMarker, $stopTimerLogic)

# 5. Update onDestroy
$onDestroyMarker = "super.onDestroy()"
$onDestroyLogic = "super.onDestroy()
        toggleNightFilter(false)"
$content = $content.Replace($onDestroyMarker, $onDestroyLogic)

# 6. Add the method definition
# I'll append it before the class closing brace.
# I'll assume the last line is "}" and replace it.
# This is risky if there are multiple braces.
# I'll insert it after `simulateMediaKey` function.
# I need to know the content of simulateMediaKey to match it.

# Let's use a simpler approach for the method.
# I'll insert it after `stopTimerService` function.
# I need to find the end of `stopTimerService`.

# Let's read the file content again to be sure about insertion points.
