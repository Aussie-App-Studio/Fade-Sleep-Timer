$file = "app\src\main\java\com\example\fadesleeptimer\service\TimerService.kt"
$content = Get-Content $file -Raw

# 1. Replace toggleNightFilter calls
$content = $content.Replace("toggleNightFilter(true)", "startNightFilter()")
$content = $content.Replace("toggleNightFilter(false)", "stopNightFilter()")

# 2. Replace toggleNightFilter implementation with start/stop
# I need to find the implementation block.
# It starts with 'private fun toggleNightFilter(enable: Boolean) {'
# And ends with '}'
# Since I can't easily match the whole block with regex due to nesting,
# I'll use a unique string inside it to identify it.

$oldImplStart = "private fun toggleNightFilter(enable: Boolean) {"
$newImpl = @"
    private fun startNightFilter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) return

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager

        if (overlayView == null) {
            overlayView = android.view.View(this).apply {
                setBackgroundColor(0x55FF0000.toInt()) // Transparent Red (User Requirement: #55FF0000)
            }
            val params = android.view.WindowManager.LayoutParams(
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                android.graphics.PixelFormat.TRANSLUCENT
            )
            params.gravity = android.view.Gravity.TOP or android.view.Gravity.START
            
            try {
                windowManager.addView(overlayView, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopNightFilter() {
        if (overlayView != null) {
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
            try {
                windowManager.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayView = null
        }
    }

    // Deprecated: Replaced by start/stop
    private fun toggleNightFilter_UNUSED(enable: Boolean) {
"@

# I'll replace the start of the function.
# But I need to remove the old body.
# Actually, I'll just replace the whole file content if I can match the block.
# Or I can comment out the old function and add the new ones.

# Let's try to match the start and assume the structure.
# The old function was:
# private fun toggleNightFilter(enable: Boolean) {
#     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) return
# 
#     val windowManager = getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
# 
#     if (enable) {
#         if (overlayView == null) {
#             overlayView = android.view.View(this).apply {
#                 setBackgroundColor(0x66FF0000.toInt()) // Semi-transparent Red
#             }
#             val params = android.view.WindowManager.LayoutParams(
#                 android.view.WindowManager.LayoutParams.MATCH_PARENT,
#                 android.view.WindowManager.LayoutParams.MATCH_PARENT,
#                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
#                     android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
#                 else
#                     android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
#                 android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
#                         android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
#                         android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
#                 android.graphics.PixelFormat.TRANSLUCENT
#             )
#             windowManager.addView(overlayView, params)
#         }
#     } else {
#         if (overlayView != null) {
#             windowManager.removeView(overlayView)
#             overlayView = null
#         }
#     }
# }

# I'll use a regex to replace this block.
# Note: The old code had `0x66FF0000.toInt()`.
# Note: The old code had `FLAG_LAYOUT_IN_SCREEN` but not `FLAG_LAYOUT_NO_LIMITS`. User requested "entire screen", so `NO_LIMITS` is good too.

# I'll construct a regex that matches the start and enough of the body to be unique, then replace it with new functions.

$regex = "(?s)private fun toggleNightFilter\(enable: Boolean\) \{.*?windowManager\.removeView\(overlayView\).*?overlayView = null\s*\}\s*\}\s*\}"
# This regex is tricky.

# Simpler approach:
# 1. Rename `toggleNightFilter` to `toggleNightFilter_OLD`.
# 2. Add `startNightFilter` and `stopNightFilter` before it.
# 3. Comment out `toggleNightFilter_OLD` body or just leave it unused (Kotlin might warn).

$content = $content.Replace("private fun toggleNightFilter(enable: Boolean) {", "$newImpl`r`n    private fun toggleNightFilter_OLD(enable: Boolean) {")

Set-Content $file $content -NoNewline
Write-Host "Refactored Night Filter!"
