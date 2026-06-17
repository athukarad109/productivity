# Productivity Tracker

A personal productivity and habit tracking system. Plan your day, log what actually happened, track habits and streaks, set weekly/monthly goals, and see unified progress over time.

This repository contains two apps:

| App | Location | Status |
|-----|----------|--------|
| **Android** (native, offline-first) | [`android/`](android/) | Primary — habits, planning, goals, dashboard |
| **Web** (Next.js) | repo root | Legacy companion — plan/log/compare via browser |

For **how the Android app works** and **setup instructions** (Android Studio, CI APK install), see **[android/README.md](android/README.md)**.

---

## Android app (recommended)

The native Android app stores everything locally on your device (SQLite). No account or internet required.

### What it does

- **Habits** — daily check-ins, streaks, schedules, optional reminders
- **Plan / Log** — time-block your day and record what you actually did
- **Compare** — productivity score (planned vs actual), missed and unplanned tasks
- **Dashboard** — unified score, weekly/monthly trends, category breakdown
- **Goals** — targets for category time, habit completions, or average score

### Quick setup

**From source (Android Studio)**

```bash
git clone <your-repo-url>
cd productivity_tracker/android
```

Open the `android/` folder in Android Studio, sync Gradle, run on a device or emulator (API 26+).

**From GitHub (no build tools)**

Push to GitHub → **Actions** → **Build Android APK** → download `app-debug.apk` from artifacts.

Full details: [android/README.md](android/README.md)

---

## Web app (optional)

A browser-based version that saves data as CSV files on the machine running the dev server.

### Features

- **Plan My Day** — tasks with category, duration or time range
- **Log Actual Day** — log work; import from plan
- **Daily Comparison** — productivity score, missed/unplanned tasks
- **Dashboard** — weekly/monthly charts

### Setup

```bash
npm install
npm run dev
```

Open [http://localhost:3000](http://localhost:3000).

### Data storage (web only)

```
data/
  plans/          # One CSV per day: data/plans/2026-03-30.csv
  actuals/        # One CSV per day: data/actuals/2026-03-30.csv
  categories.json # Custom categories
```

CSV format: `id,name,category,startTime,endTime,duration,notes`

The web and Android apps do **not** sync with each other.

---

## Tech stack

| Android | Web |
|---------|-----|
| Kotlin, Jetpack Compose, Room, Hilt | Next.js 14, Tailwind, Recharts |
| Offline SQLite | Local CSV via API routes |

---

## Repository layout

```
productivity_tracker/
  android/              # Native Android app (see android/README.md)
  app/                  # Next.js pages (web)
  components/           # Web UI components
  lib/                  # Web types & CSV utilities
  data/                 # Web local data (CSV/JSON)
  .github/workflows/    # Android APK CI build
```
