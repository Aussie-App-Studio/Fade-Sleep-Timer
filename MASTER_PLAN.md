# Fade Sleep Timer - Master Technical Plan

> **Version:** 1.0 (Build 14)  
> **Last Updated:** December 2024  
> **Package:** com.gollan.fadesleeptimer  
> **Developer:** Gollan

This document serves as an exhaustive technical specification for the Fade Sleep Timer Android application. It contains sufficient detail that a developer could rebuild the entire application from this document alone.

---

## Table of Contents

1. [App Vision & Philosophy](#1-app-vision--philosophy)
2. [Core Architecture](#2-core-architecture)
3. [Permissions Deep Dive](#3-permissions-deep-dive)
4. [The Timer System](#4-the-timer-system)
5. [Audio & Playback System](#5-audio--playback-system)
6. [Pre-Timer Experience](#6-pre-timer-experience)
7. [Wellness & Spirituality Features](#7-wellness--spirituality-features)
8. [Visual & Display System](#8-visual--display-system)
9. [Sensor-Based Controls](#9-sensor-based-controls)
10. [System Integration](#10-system-integration)
11. [Gamification System](#11-gamification-system)
12. [Onboarding & Discovery](#12-onboarding--discovery)
13. [Premium & Monetization](#13-premium--monetization)
14. [Widget System](#14-widget-system)
15. [Design Language & UI Patterns](#15-design-language--ui-patterns)
16. [Data & Persistence](#16-data--persistence)
17. [Notification System](#17-notification-system)
18. [Voice Assistant & Shortcuts](#18-voice-assistant--shortcuts)
19. [Internationalization](#19-internationalization)
20. [Technical Dependencies](#20-technical-dependencies)

---

## 1. App Vision & Philosophy

### 1.1 Core Purpose

Fade Sleep Timer exists to solve a universal problem: people who fall asleep listening to audio (music, podcasts, audiobooks, white noise) often wake up hours later with their media still playing, draining battery and data. Unlike simply setting a system timer that abruptly stops playback, Fade Sleep Timer provides a **gradual fade-out** that mimics natural sleep onset.

### 1.2 Design Philosophy

**Privacy-First Architecture:**
The app makes a strong privacy commitment. It collects zero data, makes no network requests (except for Play Store billing), and stores all user data locally. The source code is open-source so users can verify these claims. This is a core differentiator and should be prominently communicated.

**Sleep-Optimized Aesthetics:**
Every visual decision prioritizes sleep readiness. The app uses dark, muted color palettes by default. Bright colors are avoided. Animation is subtle and calming. The goal is that using the app should never feel jarring or wake-inducing.

**Feature Depth Over Breadth:**
Rather than offering shallow features, each feature is thoughtfully designed with configuration options, edge case handling, and user education. For example, the "Shake to Extend" feature includes debouncing, sensitivity thresholds, a first-use onboarding dialog, and a settings toggle.

**Christian Spiritual Focus:**
The app includes optional Christian content (scripture verses, theological quotes, prayer reminders) that can be enabled or disabled. This positions the app for a niche audience while remaining approachable for secular users who can simply disable these features.

### 1.3 Target Users

1. **Primary:** People who fall asleep to audio content and want reliable auto-stop
2. **Secondary:** Users seeking pre-sleep rituals (breathing, prayer, journaling)
3. **Tertiary:** Christian users wanting scripture-based sleep experiences
4. **Quaternary:** Power users who customize every aspect of their sleep routine

---

## 2. Core Architecture

### 2.1 Technology Choices

| Layer | Technology | Justification |
|-------|------------|---------------|
| UI Framework | Jetpack Compose + Material 3 | Modern, declarative UI with excellent theming support |
| Architecture | MVVM | Clean separation between UI and business logic |
| Async | Kotlin Coroutines + Flow | Native Kotlin async with lifecycle-aware streams |
| Background | Foreground Service | Required for reliable timer operation when screen is off |
| Database | Room | Type-safe SQLite with Kotlin integration |
| Widgets | Jetpack Glance | Compose-based widget framework |
| DI | Manual (No Hilt/Dagger) | Keeps app simple and reduces complexity |

### 2.2 Key Components

**MainActivity (Entry Point):**
The single activity hosts all Compose navigation. It handles:
- Initial permission checks
- Google Assistant App Actions routing
- Notification channel creation
- Lifecycle coordination with TimerService
- Inactivity monitoring for auto-screen-off

**TimerService (Core Engine):**
A 955-line foreground service that is the beating heart of the app. It:
- Manages timer countdown state
- Controls audio volume fading via AudioManager
- Coordinates Do Not Disturb mode
- Manages the red night filter overlay
- Broadcasts state updates to UI and widgets
- Records completed sessions to database
- Handles all timer-related intents (start, stop, extend)

**ViewModels:**
- `TimerViewModel`: Timer state, session tracking, brain dump, bedtime reminders
- `SettingsViewModel`: All app settings, premium unlocks, persistence
- `AudioViewModel`: TTS preview state for in-app audio testing

### 2.3 Navigation Structure

The app uses a single-activity architecture with Compose Navigation. Key destinations:

```
MainScreen (Home)
├── SetupScreen (Timer configuration - embedded)
├── PreTimerScreen (Pre-sleep ritual)
├── ActiveTimerScreen (During timer)
├── SettingsScreen
│   ├── Audio Settings
│   ├── Visuals Settings
│   ├── Awesome Features Settings
│   ├── Personalization Settings
│   ├── Controls Settings
│   ├── System Settings
│   └── Info Settings
├── SupportScreen
├── HallOfFameScreen
├── ThemeSelectionScreen
├── IconSelectionScreen
├── BreathingGuideScreen
├── BedtimeConsistencyScreen
└── PermissionsScreen (Onboarding)
```

---

## 3. Permissions Deep Dive

### 3.1 Core Permissions

**FOREGROUND_SERVICE + FOREGROUND_SERVICE_MEDIA_PLAYBACK:**
Required for the timer to run when the screen is off. Android requires services that play audio to declare the media_playback type. Even though Fade doesn't always play audio, it controls audio, so this type is used.

**POST_NOTIFICATIONS:**
Required on Android 13+ to show the timer notification. The timer cannot function without a notification as foreground services require one. On first launch, the app requests this permission and explains why it's needed.

**WAKE_LOCK:**
Prevents the CPU from sleeping while the timer runs. Without this, the timer might drift or stop when the device enters deep sleep. Used judiciously only during active timer.

**MODIFY_AUDIO_SETTINGS:**
Allows the app to programmatically adjust the media volume. This is the core of the fade-out functionality. The app reads the current volume, then gradually reduces it to zero over the fade duration.

### 3.2 Optional Permissions

**SYSTEM_ALERT_WINDOW (Overlay Permission):**
Required for two features:
1. **Red Night Filter:** A translucent red overlay that covers the entire screen to block blue light
2. **Open on Charge:** Allows the app to launch itself when the device is plugged in, even from the background

This permission requires user to manually enable it in Settings. The app provides a guided flow with step-by-step instructions.

**WRITE_SETTINGS:**
Required for the "Gradual Screen Dim" feature which adjusts system brightness. Users must manually grant this in Settings. The app detects if the permission is missing and shows a prompt to grant it.

**Accessibility Service (via settings):**
Required for two features:
1. **Auto Screen-Off:** Locks the screen after inactivity
2. **Anti-Doomscroll:** Detects when user opens blocked apps and returns them to home

This is not a manifest permission but requires user to enable the service in Accessibility Settings.

**NOTIFICATION_POLICY_ACCESS:**
Required for Do Not Disturb control. The app checks if access is granted and if not, opens the DND access settings page when the user tries to enable the feature.

### 3.3 Other Permissions

| Permission | Purpose |
|------------|---------|
| `RECEIVE_BOOT_COMPLETED` | Used to re-schedule exact alarms for Bedtime Reminders and Open-on-Charge jobs after a device reboot. Without this, reminders would fail after a restart. |
| `VIBRATE` | Required for haptic feedback in three specific features: 1) Breathing Guide (tactile pulses at inhale/exhale), 2) Face-down start (confirmation buzz), 3) Shake-to-extend (confirmation buzz). |
| `SCHEDULE_EXACT_ALARM` | Required for the Bedtime Reminder feature to fire precisely at the user's chosen time (e.g., 10:00 PM). Inexact alarms can be delayed by Android's battery optimizations, effectively making a bedtime reminder useless. |
| `SET_ALARM` | Allows the "Sleep Cycle Calculator" feature to seamlessly open the system clock app and set an alarm for the calculated optimal wake time. |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Critical for timer reliability. The app prompts users to whitelist it from Doze mode so the timer service isn't killed by the OS during long timers (e.g., 60+ minutes). |
| `BILLING` | Enables Google Play In-App Billing 7.0+ for processing purchases of the Visual Pack and other premium tiers. |

### 3.4 Package Queries

Android 11+ requires declaring which packages the app can query. The app queries for:
- Apps that handle audio/* content (music players)
- Apps with MUSIC_PLAYER intent (Spotify, etc.)
- Apps that handle video/* content (YouTube)
- All launcher apps (for media button app picker)

This allows the app to show available media apps for the quick-launch buttons.

---

## 4. The Timer System

### 4.1 Timer Lifecycle

**Phase 1: Configuration (SetupScreen)**
User selects a duration from preset buttons (5, 10, 15, 30, 45, 60 min by default). They can also:
- Choose audio source (system audio or TTS boring tales)
- Adjust volume via slider
- Toggle "Resume music on start"
- Access media app quick-launch buttons

**Phase 2: Pre-Timer (PreTimerScreen)**
Based on enabled settings, the user sees a sequence of cards:
1. Sleep Cycle Calculator (if enabled)
2. Airplane Mode reminder (if enabled)
3. Scripture verse (if enabled)
4. Wisdom quote (if enabled)
5. Prayer reminder (if enabled, appears with delay animation)

User taps "START TIMER" to proceed.

**Phase 3: Active Timer**
TimerService starts as foreground service. During this phase:
- Notification shows remaining time
- Widget updates with countdown
- Shake detection active (if enabled)
- Smart Extend movement detection active (if enabled)
- DND mode active (if enabled)
- Night filter overlay shown (if enabled)

**Phase 4: Fade-Out**
When the timer reaches the fade duration threshold (e.g., 5 minutes remaining):
- Volume begins gradual reduction
- If "Gradual Screen Dim" is enabled, brightness also fades
- If "Smart Wait" is enabled and music is playing, the fade may extend to wait for track end

**Phase 5: Completion**
When timer reaches zero (or track ends if Smart Wait):
- Media is paused (via simulated KEYCODE_MEDIA_PAUSE)
- Volume is restored to original level
- DND is disabled
- Night filter is removed
- Session is recorded to database
- Sleep streak is checked and updated
- Notification is dismissed

### 4.2 Timer Actions

**Start Timer:**
Can be triggered by:
- Tapping a preset duration button
- Widget "Start" button
- Google Assistant ("Start 30 minute sleep timer")
- Face-down gesture (after 5-second hold)
- App shortcut (long-press launcher icon)

**Extend Timer:**
Adds 15 minutes to the running timer. Triggered by:
- Shake gesture
- Widget "+15" button
- Notification action
- Smart Extend (auto-triggered by movement)

**Stop Timer:**
Immediately ends the timer without fade. Triggered by:
- Widget "Stop" button
- Notification action
- Google Assistant ("Stop sleep timer")

### 4.3 Timer State Broadcasting

TimerService broadcasts updates via LocalBroadcastManager:
- `com.gollan.fadesleeptimer.TIMER_TICK` - Every second with remaining time
- `com.gollan.fadesleeptimer.TIMER_FINISHED` - When timer completes
- `com.gollan.fadesleeptimer.TIMER_CANCELLED` - When manually stopped

MainScreen registers a receiver to update UI in real-time.

### 4.4 Smart Wait Feature

When enabled, the timer doesn't stop immediately when it reaches zero. Instead:
1. It checks if music is currently playing via AudioManager.isMusicActive()
2. If music is playing, it starts a polling loop (every 5 seconds)
3. When music stops (track ends), it completes the timer
4. A maximum wait time of 10 minutes prevents infinite waiting
5. The notification shows "Waiting for track to end..."

---

## 5. Audio & Playback System

### 5.1 System Audio Mode

The default mode where the app fades the user's own audio:
- Reads current media volume via AudioManager.STREAM_MUSIC
- Calculates fade steps: one step per second during fade duration
- At each step, reduces volume by (currentVolume / remainingSeconds)
- Uses a smooth exponential curve for natural-sounding fade
- Restores original volume when timer ends or is cancelled

### 5.2 Volume Safety Cap

When enabled, limits the maximum volume to 40% to protect hearing:
- The volume slider is scaled to only show 0-40% range
- A visual indicator shows "Limit Active"
- The cap applies to both initial volume and any manual adjustments
- This is particularly useful for users who fall asleep with earbuds

### 5.3 Boring Tales (TTS Audio)

An alternative to system audio where the app reads boring content via TTS:

**Content Categories:**
- **General Facts:** Mundane trivia designed to be unengaging
- **Biographies:** Dry biographical information
- **History:** Historical facts without narrative excitement

**TTS Implementation:**
- Uses Android's native TextToSpeech engine
- Supports voice gender selection: Male, Female, or Default
- The app checks for English language support and prompts user to install voice data if missing
- Content is stored in BoringTalesRepository (156KB of curated boring text)
- Stories are read continuously; when one ends, the next begins
- A shuffle system ensures varied content without immediate repeats

**Voice Preview:**
Users can preview voices in SetupScreen:
- Play button starts TTS preview
- Pause button stops preview
- Gender toggle switches between Male/Female/Default
- Preview automatically stops when navigating away

### 5.4 Play on Start

When enabled, the app simulates pressing KEYCODE_MEDIA_PLAY when the timer starts:
- This resumes paused media from any app
- Useful for users who pause their podcast, configure the timer, then want it to auto-resume
- The app broadcasts the key event via AudioManager.dispatchMediaKeyEvent()

---

## 6. Pre-Timer Experience

### 6.1 Purpose

The PreTimerScreen serves multiple purposes:
1. Creates a deliberate pause between deciding to sleep and actually starting the timer
2. Provides opportunities for pre-sleep rituals (prayer, reading, breathing)
3. Shows useful information (sleep forecast)
4. Builds anticipation and intentionality around the sleep experience

### 6.2 Sleep Cycle Calculator

Shows the optimal wake-up time based on 90-minute sleep cycles:

**Calculation Logic:**
1. Start with current time
2. Add timer duration to get estimated sleep onset
3. Add 8 hours (5 complete 90-minute cycles) to get optimal wake time
4. Round to nearest 15 minutes for cleaner display

**Display:**
- Shows "OPTIMAL WAKE TIME" label
- Large time display (e.g., "7:15 AM")
- Suffix "(5 Cycles)" to explain the calculation

**Set Alarm Integration:**
If enabled in settings, shows a "SET ALARM" button that:
- Opens the system clock app with pre-filled time
- Uses AlarmClock.ACTION_SET_ALARM intent
- Sets alarm message to "Fade Wake Up"
- Skips the clock UI to return quickly to Fade

### 6.3 Airplane Mode Reminder

A card prompting the user to enable airplane mode:
- Shows airplane icon and explanation
- "SETTINGS" button opens Android's airplane mode settings
- Purpose: Eliminate distractions and reduce EMF exposure

### 6.4 Scripture Card

Displays a calming Bible verse:
- Verse is selected based on user's topic preference or random
- Shows the verse text prominently
- Shows the reference (e.g., "— Psalm 23:4")
- New verse selected each time the screen is shown
- Topics include: Peace & Trust, God's Promises, Strength & Courage, etc.

### 6.5 Wisdom Quote Card

Displays a theological quote from famous Christian thinkers:
- Quote text in large, readable font
- Author attribution below
- Optional "sparkle" button to get an AI-generated explanation (feature in development)
- Categories can be individually toggled:
  - Peace & Comfort
  - God's Glory & Sovereignty
  - The Gospel & Grace
  - Wisdom & Living

### 6.6 Prayer Card

A gentle reminder to pray before sleep:
- Appears with a slide-in animation (delayed if other content is showing)
- Simple card with prayer icon
- Can be dismissed or left on screen
- Non-intrusive design that doesn't require action

### 6.7 First-Time Hint

On first view of PreTimerScreen:
- Shows a toast: "Tip: You can customize or disable these features in Settings > Awesome Features"
- Sets `hasSeenPreTimerHint` flag to not show again

---

## 7. Wellness & Spirituality Features

### 7.1 Bedtime Reminder

A scheduled notification to remind users to go to bed:

**Configuration:**
- Toggle to enable/disable
- Time picker to set reminder time (default: 10:00 PM)
- Stored as hour + minute values

**Notification Behavior:**
- Uses exact alarms (SCHEDULE_EXACT_ALARM) for reliability
- If music is playing when the alarm fires, delays 5 minutes and retries
- Shows notification with two actions: "Extend 15m" and "Dismiss"
- Tapping notification opens the app
- "Extend 15m" reschedules the notification

**Implementation:**
BedtimeReceiver handles three actions:
- ACTION_TRIGGER_REMINDER: Show notification or schedule retry
- ACTION_EXTEND: Dismiss and reschedule in 15 minutes
- ACTION_DISMISS: Just dismiss

### 7.2 Prayer Reminder

A visual reminder on PreTimerScreen (not a notification):
- Shows a prayer-themed card before timer starts
- Appears with a deliberate delay animation to draw attention
- No forced interaction; user can simply proceed to timer
- Designed as a gentle spiritual prompt, not an obligation

### 7.3 Brain Dump

A journaling feature to clear racing thoughts:

**Entry Flow:**
1. User opens Brain Dump dialog (icon in main screen)
2. Large text field for writing (85% screen height)
3. Retention selector: 12h or 24h
4. "Save & Sleep" or "Skip & Sleep" button

**Auto-Delete System:**
- Notes auto-delete after the retention period
- Two reminder notifications before deletion:
  - 2 hours before: "Your note will be deleted in about 2 hours"
  - 30 minutes before: "Your note will be deleted in about 30 minutes"
- Tapping notification opens the note viewer

**Purpose:**
Many people can't sleep because their mind is racing with tomorrow's to-dos or unresolved thoughts. Writing them down externalizes the thoughts, allowing the mind to relax. The auto-delete prevents accumulation of worry notes.

### 7.4 Haptic Breathing Guide

A guided breathing exercise with vibration feedback:

**Breathing Patterns:**

| Pattern | Timing | Description |
|---------|--------|-------------|
| Deep Sleep (4-7-8) | Inhale 4s, Hold 7s, Exhale 8s | Dr. Andrew Weil's technique for activating parasympathetic nervous system |
| Tactical Calm (4-4-4-4) | All phases 4s | Box breathing used by Navy SEALs for stress control |
| Instant Reset (4-6) | Inhale 4s, Exhale 6s | Quick technique using extended exhale to trigger relaxation |

**Visual Design:**
- Full-screen experience with animated "lung" circle
- Circle expands during inhale, contracts during exhale
- Phase indicator text: "Breathe In", "Hold", "Breathe Out"
- Progress indicator showing time remaining
- Skip button for users who want to exit early

**Haptic Feedback:**
- Subtle vibration pulses at phase transitions
- Can be disabled via toggle in settings
- Uses VibrationEffect.createOneShot() for cross-API compatibility

**Auto-Start Option:**
When enabled, breathing guide automatically launches before timer starts:
- User completes breathing session first
- Then proceeds to timer automatically
- Can be skipped at any time

### 7.5 Scripture Repository

144 Bible verses organized into 8 categories:

1. **Peace & Trust:** Verses about finding peace in God
2. **God's Promises:** Verses about God's faithfulness
3. **Strength & Courage:** Verses about God providing strength
4. **Love & Grace:** Verses about God's unconditional love
5. **Wisdom & Guidance:** Verses about seeking God's direction
6. **Joy & Gratitude:** Verses about thankfulness
7. **Faith & Hope:** Verses about trusting in the unseen
8. **Protection & Safety:** Verses about God's protection

**Selection Logic:**
- If user selects specific topic, verse is randomly chosen from that category
- If user selects "Surprise Me!", verse is chosen from all categories
- Each verse stored as text + reference string

### 7.6 Wisdom Quote Repository

100+ theological quotes from Christian thinkers including:
- Charles Spurgeon
- C.S. Lewis
- A.W. Tozer
- John Piper
- Corrie ten Boom
- Martin Luther
- John Calvin
- Augustine

Each quote includes text, author, and category classification.

---

## 8. Visual & Display System

### 8.1 Theme Architecture

The app supports 30+ themes, each defining a complete color palette:

**Theme Definition:**
Each theme specifies:
- `primary`: Main accent color for buttons, highlights
- `background`: Main background color
- `surface`: Card and component background
- Name for display in picker

**Theme Application:**
MaterialTheme.colorScheme is overridden based on selected theme. All UI components use the theme colors via MaterialTheme.colorScheme references, ensuring consistent theming throughout.

**Theme Categories:**

*Top Picks (Most Popular):*
- Default (Slate): Indigo accent on dark slate - the default app experience
- Glassy & Elegant: White accent with frosted glass effect
- OLED Black: Pure black for AMOLED screens - saves battery
- Bitcoin Orange: Orange accent for crypto fans
- Royal Gold: Luxurious gold on black
- Slate & Stone: Muted gray tones
- Red Night: Dark red aesthetic

*Nature Inspired:*
- Forest Mist, Ocean Depth, Desert Dune, Arctic Frost, Lavender Haze

*Tech/Cyberpunk:*
- Cyberpunk Neon, Retro Synthwave, Matrix Code, Space Gray, High Contrast

*Vibrant:*
- Toxic Lime, Solar Flare, Cotton Candy, Deep Magenta, Electric Violet

*Luxury:*
- Midnight Velvet, Rose Gold, Coffee House

*Demographics:*
- Girly Pink, Boyish Blue, Ukrainian (flag colors), CRCA Maroon

### 8.2 Theme Selection Screen

Visual grid of theme previews:
- 2-column grid layout
- Each cell shows a mini mockup of the theme
- Mockup includes header, content bars, and button to demonstrate colors
- Selected theme shows checkmark overlay
- Theme name displayed at bottom of each cell
- Tapping a theme immediately applies it

### 8.3 App Icon System

14 launcher icon variants using activity-alias technique:

**Implementation:**
AndroidManifest declares multiple activity-alias entries, each pointing to MainActivity but with different icons. Only one alias is enabled at a time. Changing icons involves:
1. Disabling the current enabled alias
2. Enabling the new alias
3. Showing a dialog warning that the app may close

**Available Icons:**
- Default: Moon symbol (standard)
- Gold: Golden moon (premium feel)
- Eyes: Sleepy closed eyes
- Ear: Ear symbol (for listening)
- White: Minimalist white on black
- Retro: Vintage aesthetic
- Disco: Fun disco ball
- Future: Futuristic style
- Sheep: Counting sheep reference
- Lamp: Bedside lamp
- Christian: Cross symbol
- Bitcoin: Bitcoin logo
- Cat: Cat face

**Icon Selection Screen:**
- Grid layout with icon previews
- Animated background for visual interest
- Selected icon highlighted with border
- Confirmation dialog before applying

### 8.4 Night Modes (Monochrome/Night Filter)

Multiple options for reducing stimulating light:

**Off:** Normal full-color display

**In-App Timer Only:** App displays in grayscale only while timer is running. Implemented via ColorMatrix filter that removes saturation.

**In-App Always:** App always displays in grayscale, regardless of timer state.

**Red Night Light:** A semi-transparent red overlay covers the entire screen (including other apps). Purpose is to block blue light which suppresses melatonin. Requires SYSTEM_ALERT_WINDOW permission. The overlay is a WindowManager View with red background at ~70% opacity.

**System Manual:** Directs user to system grayscale settings (varies by Android version).

### 8.5 Super Dim Overlay

An extremely dark overlay for use in pitch-black rooms:
- When enabled, a semi-transparent black overlay covers the screen
- Reduces effective brightness below system minimum
- Useful when even lowest brightness is too bright
- Toggle in Visuals settings

### 8.6 Wedge Clock (Analogue Display)

Visual representation of remaining time:
- Draws a pie/wedge shape that shrinks as time passes
- Full circle = full timer duration
- Shrinks clockwise as time elapses
- Provides visual feedback without requiring number reading
- Toggle in Visuals settings

### 8.7 Burn-In Protection

For OLED screens that can suffer from static image burn-in:
- When enabled, UI elements subtly shift position periodically
- Movement is small enough to be unnoticeable
- Prevents static pixels from staying on too long
- Important for timer display that may be on for extended periods

### 8.8 Gradual Screen Dimming

Dims the screen in sync with audio fade:

**Behavior:**
- Stores original screen brightness when timer starts
- When fade phase begins, gradually reduces brightness
- Brightness reaches minimum (5%) at same time volume reaches zero
- If user manually adjusts brightness upward, feature disengages for that session
- Restores original brightness when timer ends

**Permission Requirement:**
Requires WRITE_SETTINGS permission which user must grant manually in Android Settings.

---

## 9. Sensor-Based Controls

### 9.1 SensorHelper Architecture

A single class manages all accelerometer and proximity sensor needs:
- Registered as SensorEventListener
- Enables/disables specific sensors based on active features
- Implements debouncing and thresholds for gesture detection

### 9.2 Face-Down to Start

Start timer by placing phone face-down:

**Detection Logic:**
1. Monitor accelerometer for Z-axis < -8.0 (phone inverted)
2. Also check X and Y axes are small (phone is flat, not tilted)
3. Track timestamp when face-down detected
4. If held for 5 seconds continuously, trigger start
5. If phone moves, reset the timer

**User Experience:**
- Useful for nightstand charging position
- Natural gesture: put phone down, it starts timer
- 5-second delay prevents accidental triggers
- Haptic feedback confirms trigger
- Option to auto-lock screen after trigger

### 9.3 Shake to Extend

Add 15 minutes by shaking the phone:

**Detection Logic:**
1. Calculate total G-force: sqrt(x² + y² + z²) / GRAVITY_EARTH
2. If G-force > 2.5, a shake is detected
3. Apply 10-second cooldown between detections (prevents multiple triggers)
4. Only active when timer is running

**First-Time Onboarding:**
When user shakes for the first time:
1. This counts as one of their first 3 shakes
2. After 3 shakes, a dialog appears:
   - "Shake to Extend: You just used Shake to Extend! Do you want to keep this feature enabled?"
   - Options: "Keep On" or "Turn Off"
3. User choice is recorded; dialog doesn't show again

This pattern respects users who accidentally triggered the feature.

### 9.4 Smart Extend (Movement Detection)

Automatically extend timer if user is still moving:

**Use Case:**
User sets 30-minute timer but hasn't fallen asleep yet. Rather than having audio abruptly stop (even with fade), the app detects they're still moving and adds 10 minutes.

**Detection Logic:**
1. Monitor accelerometer continuously during timer
2. Track delta between samples: abs(current - previous)
3. Compare max delta against sensitivity threshold
4. If significant movement detected in the last 5 minutes before timer ends, auto-extend

**Sensitivity Settings:**
- LOW (threshold 1.5 G-delta): Only triggers on significant movement like rolling over
- MEDIUM (threshold 0.8 G-delta): Balanced detection, recommended default
- HIGH (threshold 0.3 G-delta): Triggers on subtle shifts and fidgeting

**User Education:**
Settings show explanatory text: "For best results, place your phone on your mattress near your pillow. The accelerometer will detect movement ripples through the mattress."

### 9.5 Pocket Mode

Block accidental touches when phone is in pocket:

**Detection:**
Uses proximity sensor to detect when something is close to the screen.

**Behavior:**
When triggered:
1. Shows a semi-transparent overlay: "Pocket Mode Active"
2. Text: "Double tap to unlock"
3. All touch events are consumed except double-tap
4. Double-tap dismisses overlay and restores normal operation

**Purpose:**
For users who put phone in pocket or under pillow during sleep. Prevents accidental timer cancellation.

---

## 10. System Integration

### 10.1 Do Not Disturb Control

Automatic DND during sleep:

**Modes:**
- PRIORITY: DND with priority interruptions allowed (starred contacts, repeat callers)
- ALARMS_ONLY: Strict DND, only alarms can break through

**Flow:**
1. When timer starts, check current DND state
2. If DND is off, enable it with user's preferred mode
3. Store that we enabled it (to know to disable later)
4. When timer ends, restore previous DND state

**Permission:**
Requires Notification Policy Access. If not granted, attempting to enable the feature opens Android's DND access settings page.

### 10.2 Battery Guard

Auto-stop timer if battery gets critically low:

**Behavior:**
1. User configures threshold (5-50%, default 15%)
2. Timer periodically checks battery level
3. If below threshold, timer stops immediately
4. Toast notification: "Timer stopped: Low battery"

**Purpose:**
Prevents waking up to a dead phone if user forgot to charge.

### 10.3 Go Home on Start

Minimize app when timer starts:

**Behavior:**
When enabled, after timer starts:
1. Small delay (500ms) for user to see confirmation
2. Send ACTION_MAIN + CATEGORY_HOME intent
3. User lands on home screen with timer running in background

**Purpose:**
Users don't need to see the app while sleeping. This reduces screen time and makes the phone feel "put away."

### 10.4 Open on Charge (Nightstand Mode)

Auto-launch app when phone is plugged in:

**Time Windows:**
- ALWAYS: Any time power is connected
- STRICT (8PM-4AM): Evening/night hours
- LATE (1AM-5AM): Very late night only
- MORNING (5AM-10AM): Morning charging

**Implementation:**
Uses JobScheduler with setRequiresCharging(true) constraint:
1. When feature enabled, schedule job waiting for charging
2. When charging detected, check current time against selected window
3. If in window, either:
   - Launch app directly (if SYSTEM_ALERT_WINDOW permission granted)
   - Show notification (if permission not granted)
4. Schedule next job to wait for unplug, then wait for charge again

**Purpose:**
Many users plug in phone on nightstand before bed. This saves them having to open the app manually.

### 10.5 Boot Receiver

Restore state after device reboot:

**Behavior:**
1. Received BOOT_COMPLETED intent
2. Re-schedule any pending bedtime reminders
3. Re-schedule Open on Charge job if enabled

**Note:**
Boot receiver is disabled by default in manifest. Enabled dynamically when features that need it are turned on.

### 10.6 Anti-Doomscroll Lock

Block addictive apps during sleep time:

**Configuration:**
- User adds apps to blocklist via app picker dialog
- Grace period configurable (0-30 minutes, default 5)

**Runtime Behavior (via Accessibility Service):**
1. When timer is running and feature enabled
2. ScreenLockService monitors for TYPE_WINDOW_STATE_CHANGED events
3. When foreground app changes to a blocked app:
   - Start grace period timer
   - After grace period, send user to home screen
4. If user leaves blocked app, cancel grace period

**User Experience:**
- Toast shows: "Sleep Focus: Closing in X mins"
- When grace period ends: "Sleep Focus Active" and returns to home
- Designed to break the doomscrolling habit, not completely block

---

## 11. Gamification System

### 11.1 Sleep Streaks

Track consecutive nights of consistent bedtimes:

**How It Works:**
1. User sets a bedtime in settings (e.g., 10:00 PM)
2. When timer is started within ±45 minutes of bedtime, it counts as "on time"
3. If yesterday's date had an "on time" start, streak increments
4. If yesterday was missed, streak resets to 1
5. If today already had a valid start, streak stays same (no double-counting)

**Streak Display:**
- Current streak shown in Sleep Dashboard
- Format: "🔥 7 day streak"
- Streak count persisted in settings

### 11.2 Badges

Achievement badges for streak milestones:

| Days | Badge Name | Icon | Color |
|------|------------|------|-------|
| 3 | Consistency Starter | Star | Bronze |
| 7 | Sleep Week Warrior | Fire | Silver |
| 30 | Master of Rest | Trophy | Gold |
| 100 | Legendary Slumber | Medal | Pink/Epic |

**Badge Unlocking:**
- When streak check runs, compare streak against badge thresholds
- If new badges are earned, add to unlockedBadges set
- Show celebration animation
- Badge display in Sleep Dashboard shows all earned badges

### 11.3 Sleep Dashboard

Visual display of sleep journey:

**Components:**
- Weekly sleep history graph (bar chart of session durations)
- Current streak count with fire emoji
- Badge collection display
- Total sessions count

**Access:**
Found in Settings > Info > Sleep Journey

### 11.4 Morning Check-In

Learn from user behavior:

**Trigger:**
When app is opened in the morning after a session where user extended 2+ times:

**Overlay:**
- "TIMER CHECK-IN"
- "Did you extend the timer last night?"
- Two buttons: "Yes" (thumbs up) and "No" (thumbs down)

**Outcomes:**
- If Yes: Toast suggests "Try selecting a longer duration tonight"
- If No: Toast says "Great! We'll keep your preferences as is."

**Purpose:**
Helps users realize they might need longer initial durations, improving experience over time.

---

## 12. Onboarding & Discovery

### 12.1 Permissions Onboarding

First-launch experience:

**Screen 1: Permissions Needed**
Explains why permissions are required with privacy assurance:
- Privacy Promise card explaining zero data collection
- Open Source card with link to source code

**Required Permissions:**
1. Notifications: "Allow" button triggers system dialog
2. Battery: "Fix" button opens battery optimization settings

**Optional Permissions:**
3. Auto Screen-Off: "Enable" button opens accessibility settings with instructions

**Completion:**
"Continue" button enabled once required permissions granted.

### 12.2 Spotlight Tour

Interactive feature discovery for new users:

**Trigger:**
On first app open after onboarding, if user has not yet started a timer.

**Three Steps:**

1. **Timer Presets:**
   - Spotlight highlights the duration buttons
   - "Choose Your Duration ⏱️"
   - Explains gradual fade and automatic pause

2. **Media Buttons:**
   - Spotlight highlights the Configure button
   - "Quick Media Access 🎵"
   - Explains custom app buttons

3. **Settings:**
   - Spotlight highlights settings icon
   - "Discover More ⚙️"
   - Teases breathing exercises, prayer reminders, 20+ features

**Visual Design:**
- Dark overlay covers screen (85% opacity)
- Circular "spotlight" glow on highlighted area
- Info card with title, description, and Next/Got it button
- Smooth animation between targets

### 12.3 Encouragement Glow

Subtle pulsing glow on undiscovered features:

**Behavior:**
- New users see a gentle pulsing glow on:
  - Settings icon (until clicked)
  - Configure button (until clicked)
  - Timer buttons (until first timer started)
- Once interacted with, glow never returns for that element

**Implementation:**
Flags track interaction: `hasClickedSettings`, `hasClickedConfigure`, `hasStartedTimer`

### 12.4 Feature Breadcrumbs

Suggest features based on usage:

**System:**
- Track which features user has seen/used
- When user opens settings, highlight features they haven't tried
- **Visual:** The setting row is marked with a **primary-colored indicator dot** and a **"NEW" badge**.
- **Persistence:** Once the user toggles or interacts with the setting, the breadcrumb is marked as seen and removed.

**Purpose:**
With 20+ features, users may not discover everything. Breadcrumbs guide them.

---

## 13. Premium & Monetization

### 13.1 Philosophy

The app is fully functional for free. Premium features are:
- Cosmetic (themes, icons)
- Social (Hall of Fame)
- Bonus features that enhance but don't gate core functionality

**Free Unlocks:**
Several ways to unlock premium without payment:
- Rate the app on Play Store
- Share the app with friends
- Check out developer's other apps
- Beta testing (3-tap easter egg)

### 13.2 Products

| Product ID | Name | What It Unlocks |
|------------|------|-----------------|
| visual_pack | Visual Pack | 30+ themes |
| hall_of_fame_icons | Hall of Fame & Icons | Name in app, premium icons |
| sponsor_feature | Sponsor | All features + special badge |
| lego_tier_ultimate | Ultimate | Everything + future features |

### 13.3 Hall of Fame

Immortalize supporter names in the app:

**Flow:**
1. User purchases Hall of Fame tier
2. Dialog prompts for name entry
3. Name is stored and displayed in Hall of Fame screen
4. Beautiful animated screen shows all supporter names

**Design:**
- Animated gradient background
- Names displayed with special styling
- Scrollable list if many names

### 13.4 Supporter Badge

Visual indicator of support:

**Earning:**
Any of: rating, sharing, checking other apps, or purchasing

**Display:**
- Golden badge appears next to app title in header
- Badge text: "Supporter"
- Can be hidden via settings toggle

### 13.5 Beta Unlock Easter Egg

For testing premium features without purchase:

**Trigger:**
Tap the gift icon 3 times on Support Screen:
- First tap: Toast "2 more..."
- Second tap: Toast "1 more..."
- Third tap: Celebration animation, all features unlocked

**Purpose:**
Allows beta testers to test premium features. Not advertised to regular users.

---

## 14. Widget System

### 14.1 Fade Widget (Primary)

Full-featured Glance AppWidget:

**Compact Size (< 150dp wide or < 100dp tall):**
- Idle: Single "💤 Sleep" button to start timer
- Active: Time left + "✕" stop button

**Expanded Size:**
- Idle: Moon emoji, "Ready to rest?" text, large "Start Timer" button
- Active: "SLEEPING IN" label, large time display, "+15" and "Stop" buttons

**Visual Style:**
- Custom gradient background drawable
- Purple/indigo accent colors
- Premium feel with proper spacing

**State Management:**
- Widget reads state from DataStore preferences
- TimerService updates widget after every state change
- Uses GlanceStateDefinition to persist isRunning and timeLeft

### 14.2 Quick Sleep Widget

Simple single-action widget:
- One button that starts timer with default duration
- Minimal footprint for users who want fastest access
- Icon-only option for smallest size class

### 14.3 Dashboard Widget

Informational widget showing sleep stats:
- Current streak displayed
- Most recently earned badge icon
- Tapping opens main app

### 14.4 Widget Updates

Widgets are updated from TimerService:
- On timer start/stop/extend
- Every minute during active timer
- Uses WorkManager or direct GlanceAppWidget.update() calls

---

## 15. Design Language & UI Patterns

### 15.1 Color Philosophy

All default theme colors designed for sleep:
- Deep slate backgrounds (#020617, #0F172A)
- Soft indigo accent (#6366F1)
- Muted text colors for reduced eye strain
- No pure white (highest is #F1F5F9)

### 15.2 Typography

Material 3 type scale with system fonts:
- Headlines: Bold, larger sizes for emphasis
- Body: Regular weight, comfortable reading size
- Labels: Smaller, often with letter spacing for category headers

### 15.3 Component Patterns

**Cards:**
- Rounded corners (12-16dp radius)
- Subtle border (1dp, low opacity outline color)
- Surface or surface variant background
- Consistent internal padding (16dp)

**Buttons:**
- Primary: Filled with primary color, rounded corners
- Secondary: Surface variant background
- Danger: Red tint for destructive actions

**Toggles:**
- Material 3 Switch component
- Icon prefix for visual identification
- Title + description text pattern

**Dialogs:**
- AlertDialog with rounded corners
- Consistent button placement (cancel left, confirm right)
- Surface color background

### 15.4 Animation

**Entrance Animations:**
- Prayer card slides in from right
- Spotlight tour uses animated offset
- Celebration confetti falls from top

**State Animations:**
- Breathing guide lung expands/contracts
- Pulsing glow uses infinite transition
- Widget time updates smoothly

**Philosophy:**
Animations should be calming, never jarring. Slower transitions (300-600ms), ease-out curves, subtle movement.

### 15.5 Iconography

Uses Material Icons Extended library:
- Rounded icon style for softer appearance
- Consistent icon sizes (24dp for inline, 48dp for featured)
- Icons always have content descriptions for accessibility

---

## 16. Data & Persistence

### 16.1 Room Database

**Entity: SleepSession**
- `id`: Auto-generated primary key
- `startTime`: Unix timestamp when timer started
- `endTime`: Unix timestamp when timer ended
- `durationMinutes`: Configured duration (not actual elapsed time)

**Queries:**
- Get all sessions
- Get sessions in date range
- Get recent N sessions for history display

### 16.2 SharedPreferences / DataStore

All settings persisted to local storage:

**Timer Settings:**
- `timerPresets`: List of integers for preset buttons
- `defaultDuration`: Default timer duration in minutes
- `fadeDuration`: Fade-out duration (1-10 minutes)

**Audio Settings:**
- `playOnStart`: Resume media on timer start
- `volumeSafetyCap`: Limit volume to 40%
- `smartWait`: Wait for track end
- `audioSelection`: SYSTEM, WIKI_GENERAL, WIKI_BIO, or WIKI_HISTORY
- `voiceGender`: MALE, FEMALE, or DEFAULT

**Visual Settings:**
- `theme`: Theme identifier string
- `appIcon`: Icon variant identifier
- `monochromeMode`: OFF, IN_APP_ON_TIMER, IN_APP_ALWAYS, etc.
- `superDim`: Boolean for extra dark overlay
- `analogueClock`: Boolean for wedge display
- `burnInProtection`: Boolean for OLED protection

**Wellness Settings:**
- `prayerReminder`: Boolean
- `brainDumpEnabled`: Boolean
- `scriptureModeEnabled`: Boolean
- `scriptureTopic`: String for selected category
- `wisdomQuotesEnabled`: Boolean + individual category booleans
- `sleepCalculatorEnabled`: Boolean + alarm button option
- `breathingGuideEnabled`: Boolean + pattern, duration, vibration settings
- `bedtimeReminderEnabled`: Boolean + hour/minute values

**Control Settings:**
- `faceDownStart`: Boolean
- `shakeToExtend`: Boolean
- `pocketMode`: Boolean
- `smartExtendEnabled`: Boolean + sensitivity level
- `gradualDimEnabled`: Boolean

**System Settings:**
- `dndEnabled`: Boolean + mode (PRIORITY/ALARMS_ONLY)
- `batteryGuard`: Boolean + threshold percentage
- `goHomeOnStart`: Boolean
- `openOnChargeEnabled`: Boolean + mode (ALWAYS/STRICT/LATE/MORNING)

**Doomscroll Settings:**
- `doomscrollLockEnabled`: Boolean
- `doomscrollBlockedApps`: Set of package names
- `doomscrollGraceMinutes`: Integer (0-30)

**Gamification:**
- `sleepStreakCount`: Current streak number
- `lastSleepDate`: YYYY-MM-DD format
- `unlockedBadges`: Set of badge IDs

**Premium:**
- `isPremiumUnlocked`: Master unlock flag
- `isVisualPackPurchased`: Boolean
- `isHallOfFamePurchased`: Boolean
- `isSponsorPurchased`: Boolean
- `hasHallOfFameAccess`: Boolean
- `hasVisualPack`: Boolean
- `isImmortalized`: Boolean
- `hallOfFameName`: String

**Onboarding:**
- `hasSeenOnboarding`: Boolean
### 16.2 SharedPreferences / DataStore

All settings are persisted to local storage using the `AppSettings` data class structure.

**Key Settings Schema:**

| Category | Key | Type | Default | Description |
|----------|-----|------|---------|-------------|
| **Timer** | `defaultDuration` | Int | `15` | Default minutes when starting timer |
| | `fadeDuration` | Int | `5` | Minutes to fade out (1-10) |
| | `timerPresets` | List<Int> | `5, 10, 15...` | Quick-select buttons |
| | `playOnStart` | Boolean | `false` | Resume media on start |
| | `smartWait` | Boolean | `false` | Wait for track end |
| **Audio** | `audioSelection` | Enum | `SYSTEM` | Source (System vs Boring Tales) |
| | `voiceGender` | String | `DEFAULT` | TTS Voice (Male/Female/Default) |
| | `volumeSafetyCap` | Boolean | `false` | Limit max volume to 40% |
| **Visuals** | `theme` | String | `default` | Selected app theme ID |
| | `appIcon` | String | `default` | Launcher icon alias ID |
| | `monochromeMode` | Enum | `OFF` | Grayscale behavior |
| | `superDim` | Boolean | `false` | Extra dark filtered overlay |
| | `analogueClock` | Boolean | `false` | Show wedge clock |
| | `burnInProtection` | Boolean | `false` | Shift UI pixels |
| **Wellness** | `prayerReminder` | Boolean | `true` | Show prayer card (Default ON) |
| | `scriptureModeEnabled` | Boolean | `true` | Show verses (Default ON) |
| | `wisdomQuotesEnabled` | Boolean | `true` | Show quotes (Default ON) |
| | `breathingGuideEnabled` | Boolean | `false` | Show breathing exercise card |
| | `brainDumpEnabled` | Boolean | `false` | Enable journaling feature |
| | `bedtimeReminderEnabled` | Boolean | `false` | Schedule daily notification |
| **Controls** | `shakeToExtend` | Boolean | `true` | Shake phone to add time |
| | `faceDownStart` | Boolean | `false` | Flip to start timer |
| | `smartExtendEnabled` | Boolean | `false` | Accelerometer movement detect |
| | `pocketMode` | Boolean | `false` | Block touches if covered |
| | `gradualDimEnabled` | Boolean | `false` | Dim screen brightness |
| **System** | `dndEnabled` | Boolean | `true` | Enable DND during timer |
| | `dndMode` | Enum | `PRIORITY` | PRIORITY or ALARMS_ONLY |
| | `batteryGuard` | Boolean | `false` | Stop if battery low |
| | `openOnChargeEnabled` | Boolean | `false` | Auto-launch on plug-in |

**Tracking & Gamification Flags:**

| Key | Default | Purpose |
|-----|---------|---------|
| `sleepStreakCount` | `0` | Consecutive nights streak |
| `savedMinutes` | `0` | Total minutes of battery saved (calc) |
| `unlockedBadges` | `[]` | Set of earned badge IDs |
| `hasSeenOnboarding` | `false` | Completed initial setup |
| `hasSeenPreTimerHint` | `false` | Dismissed pre-timer tip |
| `isPremiumUnlocked` | `false` | Master text-to-unlock flag |
| `isRated`, `isShared` | `false` | Actions for Supporter Badge |

**Media Button Visibility:**
12 separate booleans control specific app launch buttons (Spotify, YouTube Music, Audible, etc.). Only `configureButtonVisible` is `true` by default; others must be enabled by user.

---

## 17. Notification System

### 17.1 Timer Notification

Primary notification shown during timer:

**Channel:** Default (importance: Default)

**Content:**
- Small icon: App icon
- Title: "Fade Sleep Timer"
- Text: Dynamic countdown ("45:00 remaining")
- Actions:
  - "Stop" - Sends ACTION_STOP intent
  - "Extend +15m" - Sends ACTION_EXTEND intent

**Properties:**
- Ongoing: true (cannot be dismissed)
- Priority: Low (doesn't make sound)
- Category: Service

### 17.2 Bedtime Reminder Notification

**Channel:** bedtime_channel (importance: High)

**Content:**
- Title: "Bedtime Reminder"
- Text: "It's time to sleep."
- Actions:
  - "Extend 15m" - Reschedules in 15 minutes
  - "Dismiss" - Just cancels

**Properties:**
- Auto-cancel: true
- Priority: High (makes sound/vibration)
- Tap action: Opens app

### 17.3 Brain Dump Reminder Notification

**Channel:** brain_dump_channel (importance: Default)

**Content:**
- Title: "Brain Dump Disappearing Soon"
- Text: "Your note will be deleted in {time}. Tap to view."

**Sent:**
- 2 hours before deletion
- 30 minutes before deletion

### 17.4 Nightstand Mode Notification

**Channel:** nightstand_channel (importance: High)

**Content:**
- Title: "Power Connected"
- Text: "Tap to open Sleep Timer"

**Properties:**
- Auto-cancel: true
- Timeout: 30 seconds
- Shown only when direct app launch fails

---

## 18. Voice Assistant & Shortcuts

### 18.1 Google Assistant App Actions

Supported voice commands via Built-In Intents:

**"Start sleep timer" / "Start 30 minute sleep timer":**
- Parses duration from natural language
- Supports: minutes, hours, "half hour", etc.
- Starts timer with parsed or default duration

**"Stop sleep timer":**
- Sends stop intent to TimerService

**"Extend sleep timer":**
- Adds 15 minutes to running timer

**"Open (app name) settings":**
- Opens SettingsScreen

**Implementation:**
MainActivity.handleAppActionIntent() parses incoming intents and routes to appropriate action.

### 18.2 App Shortcuts

Long-press launcher icon shortcuts:

1. **Quick Start:** Starts timer with default duration
2. **30 min:** Starts 30-minute timer
3. **Extend +15m:** Extends running timer
4. **Settings:** Opens settings screen

Defined in res/xml/shortcuts.xml with maxShortcutCountPerActivity of 4.

---

## 19. Internationalization

### 19.1 Supported Languages

| Code | Language |
|------|----------|
| (default) | English |
| sv | Swedish |
| pl | Polish |
| ko | Korean |
| in | Indonesian |

### 19.2 String Extraction

All user-visible text in res/values/strings.xml (431 strings):
- UI labels and descriptions
- Button text
- Error messages
- Toast messages
- Notification content
- Feature explanations

### 19.3 Translation Guidelines

Strings include placeholders where needed:
- `%1$d` for integers (minutes, percentages)
- `%1$s` for strings (time, names)
- `%1$.1f` for decimals (hours)

Some strings use HTML entities:
- `&amp;` for ampersand
- `&lt;` and `&gt;` for brackets

---

## 20. Technical Dependencies

### 20.1 Core Android

```
androidx.core:core-ktx:1.17.0
androidx.lifecycle:lifecycle-runtime-ktx:2.10.0
androidx.activity:activity-compose:1.12.0
```

### 20.2 Jetpack Compose

```
androidx.compose.ui:ui:1.9.5
androidx.compose.ui:ui-graphics:1.9.5
androidx.compose.ui:ui-tooling-preview:1.9.5
androidx.compose.material3:material3:1.4.0
androidx.compose.material:material-icons-extended:1.7.8
androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0
androidx.navigation:navigation-compose:2.9.6
```

### 20.3 Data

```
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1
// Room compiler via KSP
```

### 20.4 Widgets

```
androidx.glance:glance-appwidget:1.1.0
androidx.glance:glance-material3:1.1.0
```

### 20.5 Monetization

```
com.android.billingclient:billing:7.1.1
com.google.android.play:review-ktx:2.0.1
```

### 20.6 Build Configuration

- Kotlin: 1.9.0
- KSP: 1.9.0-1.0.13
- Compose Compiler: 1.5.1
- MinSDK: 26 (Android 8.0)
- TargetSDK: 35
- CompileSDK: 36
- Java: 17

---

## Appendix: Quick Reference

### User Flows

**Main Flow:**
Home → Select Duration → PreTimer → Start → Active Timer → Fade → Complete

**Settings Flow:**
Home → Settings → Category → Toggle/Configure → Back

**Support Flow:**
Settings → Support → Select Tier → Purchase/Unlock → Celebration

### State Machine

```
IDLE → (start) → PRE_TIMER → (start) → ACTIVE → (fade) → FADING → (complete) → IDLE
                                     ↓
                              (extend) → ACTIVE
                                     ↓
                               (stop) → IDLE
```

### Key Files by Size

| File | Lines | Purpose |
|------|-------|---------|
| BoringTalesRepository.kt | 1,306 | TTS content |
| TimerService.kt | 955 | Core timer |
| MainScreen.kt | 856 | Home screen |
| SettingsScreen.kt | 816 | Settings hub |
| SetupScreen.kt | 785 | Timer config |
| SupportScreen.kt | 695 | Premium store |
| TimerViewModel.kt | 685 | Timer state |

---

*This document was generated from comprehensive codebase analysis on December 20, 2024.*
