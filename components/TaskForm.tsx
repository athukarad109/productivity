'use client';

import { useState, useEffect } from 'react';
import { Task } from '@/lib/types';

interface Props {
  categories: string[];
  onSubmit: (task: Task) => void;
  onCancel: () => void;
  initial?: Task | null;
}

function generateId() {
  return Date.now().toString(36) + Math.random().toString(36).slice(2);
}

function timeToMinutes(start: string, end: string): number {
  if (!start || !end) return 0;
  const [sh, sm] = start.split(':').map(Number);
  const [eh, em] = end.split(':').map(Number);
  return Math.max(0, (eh * 60 + em) - (sh * 60 + sm));
}

export default function TaskForm({ categories, onSubmit, onCancel, initial }: Props) {
  const [name, setName] = useState(initial?.name || '');
  const [category, setCategory] = useState(initial?.category || (categories[0] || ''));
  const [startTime, setStartTime] = useState(initial?.startTime || '');
  const [endTime, setEndTime] = useState(initial?.endTime || '');
  const [duration, setDuration] = useState(initial?.duration?.toString() || '');
  const [notes, setNotes] = useState(initial?.notes || '');
  const [useTimeRange, setUseTimeRange] = useState(!!(initial?.startTime && initial?.endTime));

  useEffect(() => {
    if (useTimeRange && startTime && endTime) {
      const mins = timeToMinutes(startTime, endTime);
      if (mins > 0) setDuration(mins.toString());
    }
  }, [startTime, endTime, useTimeRange]);

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!name.trim()) return;
    const dur = parseInt(duration) || (useTimeRange ? timeToMinutes(startTime, endTime) : 0);
    onSubmit({
      id: initial?.id || generateId(),
      name: name.trim(),
      category,
      startTime: useTimeRange ? startTime : '',
      endTime: useTimeRange ? endTime : '',
      duration: dur,
      notes: notes.trim(),
    });
  }

  return (
    <form onSubmit={handleSubmit} className="bg-slate-800 rounded-xl p-5 border border-slate-700 space-y-4">
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div className="sm:col-span-2">
          <label className="block text-xs font-medium text-slate-400 mb-1">Task Name *</label>
          <input
            type="text"
            value={name}
            onChange={e => setName(e.target.value)}
            placeholder="e.g. Deep work session"
            required
            className="w-full bg-slate-900 border border-slate-600 rounded-lg px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
          />
        </div>

        <div>
          <label className="block text-xs font-medium text-slate-400 mb-1">Category *</label>
          <select
            value={category}
            onChange={e => setCategory(e.target.value)}
            className="w-full bg-slate-900 border border-slate-600 rounded-lg px-3 py-2 text-sm text-slate-100 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            {categories.map(c => (
              <option key={c} value={c}>{c}</option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-xs font-medium text-slate-400 mb-1">Time Entry Method</label>
          <div className="flex rounded-lg overflow-hidden border border-slate-600">
            <button
              type="button"
              onClick={() => setUseTimeRange(false)}
              className={`flex-1 px-3 py-2 text-xs font-medium transition-colors ${!useTimeRange ? 'bg-indigo-600 text-white' : 'bg-slate-900 text-slate-400 hover:text-slate-200'}`}
            >
              Duration (min)
            </button>
            <button
              type="button"
              onClick={() => setUseTimeRange(true)}
              className={`flex-1 px-3 py-2 text-xs font-medium transition-colors ${useTimeRange ? 'bg-indigo-600 text-white' : 'bg-slate-900 text-slate-400 hover:text-slate-200'}`}
            >
              Time Range
            </button>
          </div>
        </div>

        {useTimeRange ? (
          <>
            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1">Start Time</label>
              <input
                type="time"
                value={startTime}
                onChange={e => setStartTime(e.target.value)}
                className="w-full bg-slate-900 border border-slate-600 rounded-lg px-3 py-2 text-sm text-slate-100 focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1">End Time</label>
              <input
                type="time"
                value={endTime}
                onChange={e => setEndTime(e.target.value)}
                className="w-full bg-slate-900 border border-slate-600 rounded-lg px-3 py-2 text-sm text-slate-100 focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </>
        ) : (
          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1">Duration (minutes) *</label>
            <input
              type="number"
              value={duration}
              onChange={e => setDuration(e.target.value)}
              min="1"
              placeholder="e.g. 60"
              className="w-full bg-slate-900 border border-slate-600 rounded-lg px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
        )}

        <div className="sm:col-span-2">
          <label className="block text-xs font-medium text-slate-400 mb-1">Notes (optional)</label>
          <input
            type="text"
            value={notes}
            onChange={e => setNotes(e.target.value)}
            placeholder="Any additional notes..."
            className="w-full bg-slate-900 border border-slate-600 rounded-lg px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>
      </div>

      <div className="flex gap-3 pt-1">
        <button
          type="submit"
          className="flex-1 bg-indigo-600 hover:bg-indigo-500 text-white font-medium py-2 px-4 rounded-lg text-sm transition-colors"
        >
          {initial ? 'Update Task' : 'Add Task'}
        </button>
        <button
          type="button"
          onClick={onCancel}
          className="px-4 py-2 rounded-lg text-sm font-medium text-slate-400 hover:text-slate-200 hover:bg-slate-700 transition-colors"
        >
          Cancel
        </button>
      </div>
    </form>
  );
}
