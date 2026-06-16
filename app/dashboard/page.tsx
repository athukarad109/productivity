'use client';

import { useState, useEffect } from 'react';
import {
  LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, Legend, ResponsiveContainer, Cell,
} from 'recharts';

function todayDate() {
  return new Date().toISOString().split('T')[0];
}

function fmtMins(mins: number) {
  if (mins === 0) return '0m';
  if (mins < 60) return `${mins}m`;
  return `${Math.floor(mins / 60)}h ${mins % 60 > 0 ? mins % 60 + 'm' : ''}`.trim();
}

interface DailyScore {
  date: string;
  score: number | null;
  plannedMinutes: number;
  actualMinutes: number;
}

interface CategoryData {
  category: string;
  planned: number;
  actual: number;
}

const COLORS = ['#6366f1', '#22c55e', '#f59e0b', '#ef4444', '#ec4899', '#14b8a6', '#8b5cf6', '#f97316'];

function formatShortDate(dateStr: string) {
  const d = new Date(dateStr + 'T00:00:00');
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

function CustomTooltip({ active, payload, label }: { active?: boolean; payload?: { name: string; value: number; color: string }[]; label?: string }) {
  if (!active || !payload || !payload.length) return null;
  return (
    <div className="bg-slate-800 border border-slate-600 rounded-xl p-3 shadow-xl text-xs">
      <p className="text-slate-300 font-medium mb-2">{label}</p>
      {payload.map((p, i) => (
        <div key={i} className="flex items-center gap-2 mb-1">
          <span className="w-2 h-2 rounded-full inline-block" style={{ background: p.color }}></span>
          <span className="text-slate-400">{p.name}:</span>
          <span className="text-slate-200 font-medium">
            {p.name === 'Score' ? `${p.value}%` : fmtMins(p.value)}
          </span>
        </div>
      ))}
    </div>
  );
}

export default function DashboardPage() {
  const [mode, setMode] = useState<'weekly' | 'monthly'>('weekly');
  const [refDate] = useState(todayDate());
  const [dailyScores, setDailyScores] = useState<DailyScore[]>([]);
  const [categoryBreakdown, setCategoryBreakdown] = useState<CategoryData[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      setLoading(true);
      const res = await fetch(`/api/dashboard?mode=${mode}&date=${refDate}`);
      const data = await res.json();
      setDailyScores(data.dailyScores || []);
      setCategoryBreakdown(data.categoryBreakdown || []);
      setLoading(false);
    }
    load();
  }, [mode, refDate]);

  const chartData = dailyScores.map(d => ({
    ...d,
    label: formatShortDate(d.date),
    score: d.score ?? undefined,
  }));

  const hasAnyData = dailyScores.some(d => d.plannedMinutes > 0 || d.actualMinutes > 0);

  const avgScore = (() => {
    const scored = dailyScores.filter(d => d.score !== null);
    if (scored.length === 0) return null;
    return Math.round(scored.reduce((s, d) => s + (d.score ?? 0), 0) / scored.length);
  })();

  const totalPlanned = dailyScores.reduce((s, d) => s + d.plannedMinutes, 0);
  const totalActual = dailyScores.reduce((s, d) => s + d.actualMinutes, 0);
  const daysLogged = dailyScores.filter(d => d.actualMinutes > 0).length;

  return (
    <div>
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-2xl font-bold text-slate-100">Dashboard</h1>
          <p className="text-slate-400 text-sm mt-1">
            {mode === 'weekly' ? 'Last 7 days' : 'Last 30 days'} overview
          </p>
        </div>
        <div className="flex rounded-xl overflow-hidden border border-slate-700">
          <button
            onClick={() => setMode('weekly')}
            className={`px-5 py-2 text-sm font-medium transition-colors ${mode === 'weekly' ? 'bg-indigo-600 text-white' : 'bg-slate-800 text-slate-400 hover:text-slate-200'}`}
          >
            Weekly
          </button>
          <button
            onClick={() => setMode('monthly')}
            className={`px-5 py-2 text-sm font-medium transition-colors ${mode === 'monthly' ? 'bg-indigo-600 text-white' : 'bg-slate-800 text-slate-400 hover:text-slate-200'}`}
          >
            Monthly
          </button>
        </div>
      </div>

      {loading ? (
        <div className="text-center py-16 text-slate-500">Loading...</div>
      ) : !hasAnyData ? (
        <div className="text-center py-16 text-slate-500">
          <div className="text-5xl mb-4">📊</div>
          <p className="text-lg font-medium text-slate-400">No data yet</p>
          <p className="text-sm mt-1">Start planning and logging your days to see insights here</p>
        </div>
      ) : (
        <>
          {/* Summary Stats */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-6">
            <div className="bg-slate-800 rounded-xl p-4 border border-slate-700 text-center">
              <div className="text-2xl font-bold text-indigo-400">
                {avgScore !== null ? `${avgScore}%` : '—'}
              </div>
              <div className="text-xs text-slate-400 mt-1">Avg Score</div>
            </div>
            <div className="bg-slate-800 rounded-xl p-4 border border-slate-700 text-center">
              <div className="text-2xl font-bold text-emerald-400">{daysLogged}</div>
              <div className="text-xs text-slate-400 mt-1">Days Logged</div>
            </div>
            <div className="bg-slate-800 rounded-xl p-4 border border-slate-700 text-center">
              <div className="text-xl font-bold text-sky-400">{fmtMins(totalPlanned)}</div>
              <div className="text-xs text-slate-400 mt-1">Total Planned</div>
            </div>
            <div className="bg-slate-800 rounded-xl p-4 border border-slate-700 text-center">
              <div className="text-xl font-bold text-violet-400">{fmtMins(totalActual)}</div>
              <div className="text-xs text-slate-400 mt-1">Total Actual</div>
            </div>
          </div>

          {/* Productivity Score Chart */}
          <div className="bg-slate-800 rounded-2xl border border-slate-700 p-6 mb-6">
            <h2 className="font-semibold text-slate-200 mb-6">Productivity Score Over Time</h2>
            <ResponsiveContainer width="100%" height={240}>
              <LineChart data={chartData} margin={{ top: 5, right: 10, left: -10, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                <XAxis dataKey="label" tick={{ fill: '#94a3b8', fontSize: 11 }} axisLine={false} tickLine={false} />
                <YAxis domain={[0, 100]} tick={{ fill: '#94a3b8', fontSize: 11 }} axisLine={false} tickLine={false} tickFormatter={v => `${v}%`} />
                <Tooltip content={<CustomTooltip />} />
                <Line
                  type="monotone"
                  dataKey="score"
                  name="Score"
                  stroke="#6366f1"
                  strokeWidth={2.5}
                  dot={{ fill: '#6366f1', r: 4, strokeWidth: 0 }}
                  activeDot={{ r: 6, fill: '#818cf8' }}
                  connectNulls={false}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>

          {/* Planned vs Actual Time Chart */}
          <div className="bg-slate-800 rounded-2xl border border-slate-700 p-6 mb-6">
            <h2 className="font-semibold text-slate-200 mb-6">Planned vs Actual Time (minutes)</h2>
            <ResponsiveContainer width="100%" height={240}>
              <BarChart data={chartData} margin={{ top: 5, right: 10, left: -10, bottom: 5 }} barCategoryGap="30%">
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                <XAxis dataKey="label" tick={{ fill: '#94a3b8', fontSize: 11 }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fill: '#94a3b8', fontSize: 11 }} axisLine={false} tickLine={false} />
                <Tooltip content={<CustomTooltip />} />
                <Legend
                  wrapperStyle={{ fontSize: 12, paddingTop: 16, color: '#94a3b8' }}
                />
                <Bar dataKey="plannedMinutes" name="Planned" fill="#6366f1" radius={[4, 4, 0, 0]} />
                <Bar dataKey="actualMinutes" name="Actual" fill="#22c55e" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>

          {/* Category Breakdown */}
          {categoryBreakdown.length > 0 && (
            <div className="bg-slate-800 rounded-2xl border border-slate-700 p-6">
              <h2 className="font-semibold text-slate-200 mb-6">Time by Category (minutes)</h2>
              <ResponsiveContainer width="100%" height={280}>
                <BarChart
                  data={categoryBreakdown}
                  layout="vertical"
                  margin={{ top: 5, right: 20, left: 60, bottom: 5 }}
                  barCategoryGap="25%"
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" horizontal={false} />
                  <XAxis type="number" tick={{ fill: '#94a3b8', fontSize: 11 }} axisLine={false} tickLine={false} />
                  <YAxis type="category" dataKey="category" tick={{ fill: '#94a3b8', fontSize: 11 }} axisLine={false} tickLine={false} width={55} />
                  <Tooltip content={<CustomTooltip />} />
                  <Legend
                    wrapperStyle={{ fontSize: 12, paddingTop: 16, color: '#94a3b8' }}
                  />
                  <Bar dataKey="planned" name="Planned" radius={[0, 4, 4, 0]}>
                    {categoryBreakdown.map((_, index) => (
                      <Cell key={index} fill={COLORS[index % COLORS.length]} fillOpacity={0.7} />
                    ))}
                  </Bar>
                  <Bar dataKey="actual" name="Actual" radius={[0, 4, 4, 0]}>
                    {categoryBreakdown.map((_, index) => (
                      <Cell key={index} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>

              {/* Category Legend Table */}
              <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                {categoryBreakdown.map((cat, idx) => (
                  <div key={cat.category} className="flex items-center gap-3 bg-slate-900/50 rounded-lg p-3">
                    <span
                      className="w-3 h-3 rounded-full flex-shrink-0"
                      style={{ background: COLORS[idx % COLORS.length] }}
                    />
                    <div className="flex-1 min-w-0">
                      <p className="text-xs font-medium text-slate-300 truncate">{cat.category}</p>
                      <p className="text-xs text-slate-500">
                        P: {fmtMins(cat.planned)} · A: {fmtMins(cat.actual)}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
