# Productivity Tracker

A personal daily productivity tracker built with Next.js 14. Plan your day, log what actually happened, compare planned vs actual, and visualize trends over time.

## Features

- **Plan My Day** — Add tasks with name, category, time range or duration. Manage custom categories. Auto-saves to CSV.
- **Log Actual Day** — Log what you actually did. Import from your plan as a starting point.
- **Daily Comparison** — Side-by-side planned vs actual with a productivity score, missed tasks, and unplanned tasks.
- **Dashboard** — Weekly/monthly charts: productivity score over time, planned vs actual minutes, and category breakdown.

## Getting Started

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

## Data Storage

All data is stored locally as CSV files and JSON:

```
data/
  plans/          # One CSV per day: data/plans/2026-03-30.csv
  actuals/        # One CSV per day: data/actuals/2026-03-30.csv
  categories.json # Your custom categories list
```

You can open any CSV in Excel/Google Sheets. The format is:
```
id,name,category,startTime,endTime,duration,notes
```

## Tech Stack

- **Next.js 14** (App Router)
- **Tailwind CSS**
- **Recharts** for charts
- **Node.js fs** for local CSV read/write via API routes

## Project Structure

```
app/
  page.tsx              # Plan My Day
  log/page.tsx          # Log Actual Day
  compare/page.tsx      # Daily Comparison
  dashboard/page.tsx    # Dashboard with charts
  api/
    plans/[date]/       # GET/POST plan for a date
    actuals/[date]/     # GET/POST actuals for a date
    categories/         # GET/POST category list
    dashboard/          # GET aggregated stats
components/
  NavBar.tsx
  TaskForm.tsx
  TaskCard.tsx
  CategoryManager.tsx
lib/
  types.ts              # Shared TypeScript types
  csv.ts                # CSV serialization utilities
```
