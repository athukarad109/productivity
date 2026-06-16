import { NextRequest, NextResponse } from 'next/server';
import fs from 'fs';
import path from 'path';
import { csvToTasks } from '@/lib/csv';
import { Task } from '@/lib/types';

function getDateRange(mode: string, refDate: string): string[] {
  const ref = new Date(refDate);
  const dates: string[] = [];

  if (mode === 'weekly') {
    // last 7 days
    for (let i = 6; i >= 0; i--) {
      const d = new Date(ref);
      d.setDate(ref.getDate() - i);
      dates.push(d.toISOString().split('T')[0]);
    }
  } else {
    // last 30 days
    for (let i = 29; i >= 0; i--) {
      const d = new Date(ref);
      d.setDate(ref.getDate() - i);
      dates.push(d.toISOString().split('T')[0]);
    }
  }
  return dates;
}

function loadTasks(type: 'plans' | 'actuals', date: string): Task[] {
  const filePath = path.join(process.cwd(), 'data', type, `${date}.csv`);
  if (!fs.existsSync(filePath)) return [];
  return csvToTasks(fs.readFileSync(filePath, 'utf-8'));
}

function totalMinutes(tasks: Task[]): number {
  return tasks.reduce((sum, t) => sum + (t.duration || 0), 0);
}

function matchedMinutes(planned: Task[], actual: Task[]): number {
  // match by category similarity and overlap
  let matched = 0;
  const actualCopy = [...actual];
  for (const p of planned) {
    const idx = actualCopy.findIndex(
      a => a.category.toLowerCase() === p.category.toLowerCase() &&
           a.name.toLowerCase() === p.name.toLowerCase()
    );
    if (idx !== -1) {
      matched += Math.min(p.duration, actualCopy[idx].duration);
      actualCopy.splice(idx, 1);
    } else {
      // partial match by category
      const catIdx = actualCopy.findIndex(
        a => a.category.toLowerCase() === p.category.toLowerCase()
      );
      if (catIdx !== -1) {
        matched += Math.min(p.duration, actualCopy[catIdx].duration) * 0.5;
        actualCopy.splice(catIdx, 1);
      }
    }
  }
  return Math.round(matched);
}

export async function GET(req: NextRequest) {
  const { searchParams } = new URL(req.url);
  const mode = searchParams.get('mode') || 'weekly';
  const refDate = searchParams.get('date') || new Date().toISOString().split('T')[0];

  const dates = getDateRange(mode, refDate);

  const dailyScores = dates.map(date => {
    const planned = loadTasks('plans', date);
    const actual = loadTasks('actuals', date);
    const planMins = totalMinutes(planned);
    const score = planMins > 0 ? Math.round((matchedMinutes(planned, actual) / planMins) * 100) : null;
    return { date, score, plannedMinutes: planMins, actualMinutes: totalMinutes(actual) };
  });

  // category breakdown
  const categoryMap: Record<string, { planned: number; actual: number }> = {};
  for (const date of dates) {
    for (const t of loadTasks('plans', date)) {
      if (!categoryMap[t.category]) categoryMap[t.category] = { planned: 0, actual: 0 };
      categoryMap[t.category].planned += t.duration;
    }
    for (const t of loadTasks('actuals', date)) {
      if (!categoryMap[t.category]) categoryMap[t.category] = { planned: 0, actual: 0 };
      categoryMap[t.category].actual += t.duration;
    }
  }

  const categoryBreakdown = Object.entries(categoryMap).map(([category, data]) => ({
    category,
    planned: data.planned,
    actual: data.actual,
  }));

  return NextResponse.json({ dailyScores, categoryBreakdown });
}
