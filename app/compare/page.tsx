'use client';

import { useState, useEffect, useCallback } from 'react';
import Link from 'next/link';
import TaskCard from '@/components/TaskCard';
import { Task } from '@/lib/types';

function todayDate() {
  return new Date().toISOString().split('T')[0];
}

function formatDateLabel(date: string) {
  const d = new Date(date + 'T00:00:00');
  return d.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' });
}

function totalMinutes(tasks: Task[]) {
  return tasks.reduce((s, t) => s + (t.duration || 0), 0);
}

function fmtMins(mins: number) {
  if (mins === 0) return '0m';
  if (mins < 60) return `${mins}m`;
  return `${Math.floor(mins / 60)}h ${mins % 60 > 0 ? mins % 60 + 'm' : ''}`.trim();
}

function matchedMinutes(planned: Task[], actual: Task[]): number {
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

function getMissedTasks(planned: Task[], actual: Task[]): Task[] {
  return planned.filter(p =>
    !actual.some(
      a => a.name.toLowerCase() === p.name.toLowerCase() &&
           a.category.toLowerCase() === p.category.toLowerCase()
    )
  );
}

function getUnplannedTasks(planned: Task[], actual: Task[]): Task[] {
  return actual.filter(a =>
    !planned.some(
      p => p.name.toLowerCase() === a.name.toLowerCase() &&
           p.category.toLowerCase() === a.category.toLowerCase()
    )
  );
}

function ScoreRing({ score }: { score: number }) {
  const radius = 54;
  const circumference = 2 * Math.PI * radius;
  const progress = (score / 100) * circumference;
  const color = score >= 80 ? '#22c55e' : score >= 50 ? '#f59e0b' : '#ef4444';

  return (
    <div className="relative inline-flex items-center justify-center">
      <svg width="140" height="140" className="-rotate-90">
        <circle cx="70" cy="70" r={radius} stroke="#1e293b" strokeWidth="12" fill="none" />
        <circle
          cx="70" cy="70" r={radius}
          stroke={color}
          strokeWidth="12"
          fill="none"
          strokeDasharray={circumference}
          strokeDashoffset={circumference - progress}
          strokeLinecap="round"
          style={{ transition: 'stroke-dashoffset 1s ease' }}
        />
      </svg>
      <div className="absolute text-center">
        <div className="text-3xl font-bold text-slate-100">{score}%</div>
        <div className="text-xs text-slate-400">score</div>
      </div>
    </div>
  );
}

export default function ComparePage() {
  const [date, setDate] = useState(todayDate());
  const [planned, setPlanned] = useState<Task[]>([]);
  const [actual, setActual] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);

  const loadData = useCallback(async () => {
    setLoading(true);
    const [plansRes, actualsRes] = await Promise.all([
      fetch(`/api/plans/${date}`),
      fetch(`/api/actuals/${date}`),
    ]);
    const plansData = await plansRes.json();
    const actualsData = await actualsRes.json();
    setPlanned(plansData.tasks || []);
    setActual(actualsData.tasks || []);
    setLoading(false);
  }, [date]);

  useEffect(() => { loadData(); }, [loadData]);

  const planTotal = totalMinutes(planned);
  const actualTotal = totalMinutes(actual);
  const matched = matchedMinutes(planned, actual);
  const score = planTotal > 0 ? Math.round((matched / planTotal) * 100) : 0;
  const missedTasks = getMissedTasks(planned, actual);
  const unplannedTasks = getUnplannedTasks(planned, actual);

  const hasData = planned.length > 0 || actual.length > 0;

  return (
    <div>
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-2xl font-bold text-slate-100">Daily Comparison</h1>
          <p className="text-slate-400 text-sm mt-1">{formatDateLabel(date)}</p>
        </div>
        <input
          type="date"
          value={date}
          onChange={e => setDate(e.target.value)}
          className="bg-slate-800 border border-slate-600 rounded-lg px-3 py-2 text-sm text-slate-100 focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
      </div>

      {loading ? (
        <div className="text-center py-16 text-slate-500">Loading...</div>
      ) : !hasData ? (
        <div className="text-center py-16 text-slate-500">
          <div className="text-5xl mb-4">⚖️</div>
          <p className="text-lg font-medium text-slate-400">No data for this date</p>
          <p className="text-sm mt-1 mb-6">Add a plan and log your day first</p>
          <div className="flex gap-3 justify-center">
            <Link href="/" className="bg-indigo-600 hover:bg-indigo-500 text-white px-5 py-2 rounded-lg text-sm font-medium transition-colors">
              Plan Day
            </Link>
            <Link href="/log" className="bg-slate-700 hover:bg-slate-600 text-slate-200 px-5 py-2 rounded-lg text-sm font-medium transition-colors">
              Log Day
            </Link>
          </div>
        </div>
      ) : (
        <>
          {/* Score Summary */}
          <div className="bg-slate-800 rounded-2xl border border-slate-700 p-6 mb-6">
            <div className="flex flex-col sm:flex-row items-center gap-8">
              <ScoreRing score={score} />
              <div className="flex-1 grid grid-cols-2 sm:grid-cols-4 gap-4 w-full">
                <div className="text-center">
                  <div className="text-xl font-bold text-indigo-400">{fmtMins(planTotal)}</div>
                  <div className="text-xs text-slate-400 mt-1">Planned</div>
                </div>
                <div className="text-center">
                  <div className="text-xl font-bold text-emerald-400">{fmtMins(actualTotal)}</div>
                  <div className="text-xs text-slate-400 mt-1">Actual</div>
                </div>
                <div className="text-center">
                  <div className="text-xl font-bold text-red-400">{missedTasks.length}</div>
                  <div className="text-xs text-slate-400 mt-1">Missed Tasks</div>
                </div>
                <div className="text-center">
                  <div className="text-xl font-bold text-amber-400">{unplannedTasks.length}</div>
                  <div className="text-xs text-slate-400 mt-1">Unplanned Tasks</div>
                </div>
              </div>
            </div>
          </div>

          {/* Side by Side */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
            {/* Planned */}
            <div>
              <div className="flex items-center justify-between mb-3">
                <h2 className="font-semibold text-slate-200 flex items-center gap-2">
                  <span className="w-3 h-3 rounded-full bg-indigo-500 inline-block"></span>
                  Planned ({planned.length} tasks)
                </h2>
                <span className="text-xs text-slate-400">{fmtMins(planTotal)}</span>
              </div>
              <div className="space-y-2">
                {planned.length === 0 ? (
                  <div className="text-center py-8 text-slate-500 bg-slate-800/50 rounded-xl border border-slate-700">
                    <p className="text-sm">No plan for this day</p>
                    <Link href="/" className="text-indigo-400 hover:text-indigo-300 text-xs mt-1 inline-block">Plan now →</Link>
                  </div>
                ) : (
                  planned.map(task => (
                    <TaskCard
                      key={task.id}
                      task={task}
                      variant={missedTasks.some(m => m.id === task.id) ? 'missed' : 'planned'}
                    />
                  ))
                )}
              </div>
            </div>

            {/* Actual */}
            <div>
              <div className="flex items-center justify-between mb-3">
                <h2 className="font-semibold text-slate-200 flex items-center gap-2">
                  <span className="w-3 h-3 rounded-full bg-emerald-500 inline-block"></span>
                  Actual ({actual.length} tasks)
                </h2>
                <span className="text-xs text-slate-400">{fmtMins(actualTotal)}</span>
              </div>
              <div className="space-y-2">
                {actual.length === 0 ? (
                  <div className="text-center py-8 text-slate-500 bg-slate-800/50 rounded-xl border border-slate-700">
                    <p className="text-sm">Nothing logged yet</p>
                    <Link href="/log" className="text-indigo-400 hover:text-indigo-300 text-xs mt-1 inline-block">Log now →</Link>
                  </div>
                ) : (
                  actual.map(task => (
                    <TaskCard
                      key={task.id}
                      task={task}
                      variant={unplannedTasks.some(u => u.id === task.id) ? 'unplanned' : 'actual'}
                    />
                  ))
                )}
              </div>
            </div>
          </div>

          {/* Insights */}
          {(missedTasks.length > 0 || unplannedTasks.length > 0) && (
            <div className="bg-slate-800 rounded-2xl border border-slate-700 p-6">
              <h2 className="font-semibold text-slate-200 mb-4">Day Insights</h2>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {missedTasks.length > 0 && (
                  <div>
                    <h3 className="text-sm font-medium text-red-400 mb-2 flex items-center gap-1">
                      ❌ Missed ({missedTasks.length})
                    </h3>
                    <div className="space-y-2">
                      {missedTasks.map(t => (
                        <TaskCard key={t.id} task={t} variant="missed" />
                      ))}
                    </div>
                  </div>
                )}
                {unplannedTasks.length > 0 && (
                  <div>
                    <h3 className="text-sm font-medium text-emerald-400 mb-2 flex items-center gap-1">
                      ✨ Unplanned ({unplannedTasks.length})
                    </h3>
                    <div className="space-y-2">
                      {unplannedTasks.map(t => (
                        <TaskCard key={t.id} task={t} variant="unplanned" />
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
