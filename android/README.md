# Productivity Tracker — Android

Native offline-first productivity app. Built with Kotlin, Jetpack Compose, Room, and Hilt.

The existing Next.js web app in the repo root is unchanged. This Android app is a separate project under `android/`.

## Features

### Habits (Phase 1)
- **Today** — habits due today, mark done, track counts/duration
- **Manage habits** — create, edit, schedules, reminders, streaks
- **Stats** — 7-day completion rate, per-habit streaks, heatmap

### Daily planning (Phase 2)
- **Plan** — schedule tasks with category, duration or time range
- **Log** — record what actually happened; import from plan
- **Compare** — planned vs actual, productivity score, missed/unplanned tasks

All data is stored locally in SQLite. No account or cloud required.

## Requirements

- [Android Studio](https://developer.android.com/studio) Ladybug or newer
- JDK 17+
- Android SDK 35

## Open & run

1. Open Android Studio → **Open** → select the `android/` folder
2. Wait for Gradle sync
3. Connect a device or start an emulator (API 26+)
4. Run **app**

```bash
./gradlew assembleDebug
```

## CI (GitHub Actions)

On every push that changes `android/`, the workflow [`.github/workflows/android-apk.yml`](../.github/workflows/android-apk.yml) builds a debug APK on GitHub's runners.

1. Open your repo on GitHub → **Actions** → latest **Build Android APK** run
2. Download the **productivity-tracker-debug** artifact (contains `app-debug.apk`)
3. Install on your phone (enable "Install unknown apps" for your browser/files app)

No signing secrets are required — this produces an unsigned **debug** APK suitable for personal installs.

## Bottom navigation

| Tab | Purpose |
|-----|---------|
| Today | Habit check-in |
| Plan | Plan my day |
| Log | Log actual day |
| Compare | Plan vs actual score |
| Stats | Habit streaks & rates |

Habit management opens from the list icon on **Today** or **Manage habits** on **Stats**.

## Project structure

```
app/src/main/java/com/productivitytracker/habits/
  data/          Room DB, repositories
  domain/        Habits, comparison scoring, time formatting
  ui/            Compose screens, navigation, theme
  reminder/      WorkManager habit notifications
```

## Roadmap

| Phase | Status |
|-------|--------|
| Habits, streaks, reminders | ✅ |
| Plan, log, compare | ✅ |
| Goals + unified dashboard | Planned |
| Widgets, focus timer, export | Planned |

## Permissions

- `POST_NOTIFICATIONS` (Android 13+) — habit reminders
