  - **Behavior**: Toast warning at 2 minutes of inactivity, Screen Lock at 3 minutes.
  - **Condition**: Only when timer is running and app is in foreground.
- **Analogue Clock**: Restored as the primary timer visualization.
- **Digital Timer**: Hidden when Analogue Clock is active.

## Verification
- **Build**: Successful (`./gradlew assembleDebug`).
- **Tests**:
  - Verify "Time Saved" card appears in Settings > Info.
  - Verify "Time Saved" increases after a timer finishes naturally.
  - Verify "ON" button appears when timer starts.
  - Verify "ON" button pulses.
  - Verify "ON" button is to the left of Settings.
  - Verify Screen Off toast appears after 2 mins of inactivity.
  - Verify Screen turns off after 3 mins of inactivity (requires Accessibility permission).

### 3. Morning Quality Check & Default Timer
- **Goal**: Prompt users to adjust default timer if they extend often, and show default indicator.
- **Implementation**:
    - Added `MorningCheckOverlay` composable with "Disable in Settings" footer.
    - Implemented `checkMorningPrompt` in `TimerViewModel`:
        - Checks if last session was **8-24 hours ago**.
        - Ensures specific session hasn't been checked yet.
    - Added "Adjust Default Timer" section in `WellnessSettings`:
        - Main toggle to enable customization.
        - Nested slider (snaps correctly to presets, matches button formatting) and "Timer Check-in" toggle.
    - Moved "Timer Presets" to `WellnessSettings` and renamed to "Change Timer Buttons".
        - Implemented smart snapping: Custom buttons > 60m snap to nearest 30m (e.g. 87m -> 1.5h).
        - Fixed formatting to support decimal hours (e.g. 90m -> "1.5h").
    - Added "(default)" indicator to `PresetCard` in `MainActivity`.
- **Sleep Cycle Calculator**:
    - Added toggle in `WellnessSettings`.
    - Implemented `SleepForecastCard` in `PreTimerScreen` showing estimated wake time (Now + Duration + 8h).
    - Updated `MainActivity` to pass duration to `PreTimerScreen`.
- **Verification**:
    - Verified build success.
    - Confirmed `TimerService` tracks extensions and persists session data.
    - Confirmed `TimerViewModel` loads/saves settings correctly.
