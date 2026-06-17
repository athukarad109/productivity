# Productivity Tracker — Android

Native offline-first productivity and habit tracking app for Android. Built with **Kotlin**, **Jetpack Compose**, **Room**, and **Hilt**.

The Next.js web app in the repo root is a separate, older companion. **This Android app is the main product** — all your data stays on your phone in a local SQLite database. No account, no cloud, no internet required after install.

---

## How the app works

Productivity Tracker combines three ideas into one daily loop:

1. **Habits** — recurring behaviors you want to build (meditate, exercise, read, etc.)
2. **Time blocking** — plan what you intend to do each day, then log what you actually did
3. **Goals** — weekly or monthly targets that tie habits and time together

Everything feeds a single **unified score** on the Dashboard so you can see whether you are improving over time.

### Daily workflow

A typical day looks like this:

```
Morning          During the day              Evening
────────         ───────────────             ───────
Plan tab    →    Today tab (check habits) →  Log tab
Add tasks        Mark habits done             Record actual work
by category      (+/− counts)                Import from plan if helpful
                                              Compare tab → see your score
                                              Dashboard → weekly trends
```

| Step | What you do | What the app stores |
|------|-------------|---------------------|
| **Plan** | Add tasks with category, duration or time range | Planned tasks for that date |
| **Today** | Complete habits due today | Habit log entries (done / partial / skipped) |
| **Log** | Record what you actually did | Actual tasks for that date |
| **Compare** | Review plan vs actual | Computes score on the fly (not stored) |
| **Dashboard** | See trends and goal progress | Aggregates habits + plans + actuals |

You can pick any date (not just today) on Plan, Log, Compare, and Dashboard.

### Habits

Habits are separate from daily tasks. Each habit has:

- **Schedule** — every day, weekdays, weekends, or X times per week
- **Target** — yes/no, a count (e.g. 8 glasses of water), or duration (e.g. 20 minutes)
- **Category** — Work, Learning, Exercise, etc. (8 defaults seeded on first launch)
- **Optional reminder** — local notification via WorkManager (no server)

The **Today** tab shows only habits due that day. Tapping the list icon opens habit management (create, edit, view streaks and heatmap on habit detail).

**Streaks** count consecutive days you completed a habit on its scheduled days. Today not yet done does not break an active streak.

### Plan vs actual (time tracking)

**Plan** and **Log** work like a time journal:

- Tasks have a **name**, **category**, **duration** (minutes), and optionally a **time range** (start/end)
- **Log** can **Import from Plan** when you have a plan but no log yet — copies planned tasks as a starting point for editing
- Data is stored per date; you can plan or log past/future days

### Productivity score (Compare tab)

When you open **Compare** for a date, the app scores how well your actual day matched your plan:

1. For each **planned** task, it looks for an **actual** task with the same name and category
2. If found, it credits the **minimum** of planned and actual duration (full match)
3. If only the **category** matches, it credits **50%** of that overlap (partial match)
4. **Score** = matched minutes ÷ total planned minutes × 100 (0% if nothing was planned)

It also highlights:

- **Missed tasks** — planned but not logged
- **Unplanned tasks** — logged but not in the plan

### Unified score (Dashboard)

The **Dashboard** blends habits and time tracking into one number per day:

| Component | How it is calculated |
|-----------|----------------------|
| **Habit score** | % of habits due that day that were completed |
| **Productivity score** | Plan vs actual score (same as Compare) |
| **Unified score** | Average of the two when both exist; otherwise whichever is available |

Weekly and monthly views show:

- Bar chart of daily unified scores
- Habit completion rate over the period
- Category breakdown (planned vs actual minutes per category)
- Progress toward active **goals**

### Goals

Goals are weekly or monthly targets. Three types:

| Type | Measures | Example |
|------|----------|---------|
| **Category time** | Actual logged minutes in a category | 300 min Learning / week |
| **Habit completions** | Times a habit was marked done | Meditate 5× / week |
| **Avg productivity** | Mean unified score % over the period | 80% avg / month |

Create goals from **Dashboard → Goals → +**. Progress updates from your habit logs and actual task data.

### Data & privacy

- All data lives in `productivity_tracker.db` on your device (Room / SQLite)
- Tables: categories, habits, habit_logs, day_tasks (plan + actual), goals
- No sync, no analytics, no backend
- Uninstalling the app removes local data unless you back up the DB manually

---

## Setup

### Option A — Android Studio (recommended for development)

**Requirements**

- [Android Studio](https://developer.android.com/studio) Ladybug or newer
- JDK 17+ (bundled with Android Studio is fine)
- Android SDK 35
- Device or emulator running **API 26+** (Android 8.0+)

**Steps**

1. Clone the repository:
   ```bash
   git clone <your-repo-url>
   cd productivity_tracker/android
   ```
2. Open Android Studio → **File → Open** → select the `android/` folder (not the repo root).
3. Wait for Gradle sync to finish. If prompted, accept SDK licenses and install missing components.
4. Connect a phone with **USB debugging** enabled, or create an emulator (**Device Manager → Create Device**).
5. Click **Run** (green play) or:
   ```bash
   ./gradlew assembleDebug
   ```
   APK output: `app/build/outputs/apk/debug/app-debug.apk`

**First launch**

- Default categories are created automatically
- On Android 13+, allow **notifications** if you want habit reminders
- Add habits from **Today → list icon**, then try planning a day on the **Plan** tab

### Option B — Install from GitHub Actions (no local build)

If the repo has CI enabled, every push that changes `android/` builds an APK automatically.

1. Push your code to GitHub (or use an existing build from **Actions**).
2. Open the repo → **Actions** → **Build Android APK** → latest successful run.
3. Download the **productivity-tracker-debug** artifact (`app-debug.apk`).
4. Transfer to your phone and install (enable **Install unknown apps** for your file manager or browser).

This produces an unsigned **debug** APK — fine for personal use. Play Store release would need a signed release build later.

### Troubleshooting

| Issue | Fix |
|-------|-----|
| Gradle sync fails / SDK not found | Install SDK 35 in Android Studio → **SDK Manager**; set `sdk.dir` in `android/local.properties` if needed |
| `assembleDebug` fails on CI | Check **Actions** log; ensure `Theme.kt` uses `object AppColors` (Kotlin 2.0) |
| App crashes after update | Room migrations handle schema changes; if you sideloaded an old DB, clear app data or reinstall |
| Reminders not showing | Grant notification permission; ensure habit has reminder time set |

---

## Navigation

| Tab | Purpose |
|-----|---------|
| **Today** | Habit check-in for the current day |
| **Plan** | Plan tasks for any date |
| **Log** | Log actual tasks; import from plan |
| **Compare** | Plan vs actual score and insights |
| **Dashboard** | Unified trends, categories, goals |

- **Manage habits** — list icon on **Today**, or **Dashboard → Habits**
- **Manage goals** — **Dashboard → Goals**

---

## Project structure

```
android/
  app/src/main/java/com/productivitytracker/habits/
    data/           Room entities, DAOs, repositories
    domain/         Habit logic, comparison scoring, dashboard, goals
    ui/             Compose screens, navigation, theme
    reminder/       WorkManager habit notifications
    di/             Hilt dependency injection
  .github/workflows/android-apk.yml   # CI build (repo root)
```

---

## Tech stack

| Layer | Technology |
|-------|------------|
| UI | Jetpack Compose, Material 3 |
| Database | Room (SQLite) |
| DI | Hilt |
| Background | WorkManager |
| Navigation | Navigation Compose |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |

---

## Roadmap

| Phase | Status |
|-------|--------|
| Habits, streaks, reminders | Done |
| Plan, log, compare | Done |
| Goals + unified dashboard | Done |
| Widgets, focus timer, JSON export | Planned |

---

## Permissions

- `POST_NOTIFICATIONS` (Android 13+) — optional habit reminders
- `RECEIVE_BOOT_COMPLETED` — reschedule reminders after reboot
