'use client';

import { useState, useEffect, useCallback } from 'react';
import TaskForm from '@/components/TaskForm';
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
  if (mins < 60) return `${mins}m`;
  return `${Math.floor(mins / 60)}h ${mins % 60 > 0 ? mins % 60 + 'm' : ''}`.trim();
}

export default function LogPage() {
  const [date, setDate] = useState(todayDate());
  const [tasks, setTasks] = useState<Task[]>([]);
  const [planned, setPlanned] = useState<Task[]>([]);
  const [categories, setCategories] = useState<string[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [saving, setSaving] = useState(false);
  const [saveMsg, setSaveMsg] = useState('');
  const [importingPlanned, setImportingPlanned] = useState(false);

  const loadData = useCallback(async () => {
    const [actualsRes, plansRes, catsRes] = await Promise.all([
      fetch(`/api/actuals/${date}`),
      fetch(`/api/plans/${date}`),
      fetch('/api/categories'),
    ]);
    const actualsData = await actualsRes.json();
    const plansData = await plansRes.json();
    const catsData = await catsRes.json();
    setTasks(actualsData.tasks || []);
    setPlanned(plansData.tasks || []);
    setCategories(catsData.categories || []);
  }, [date]);

  useEffect(() => { loadData(); }, [loadData]);

  async function saveTasks(newTasks: Task[]) {
    setSaving(true);
    await fetch(`/api/actuals/${date}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ tasks: newTasks }),
    });
    setSaving(false);
    setSaveMsg('Saved!');
    setTimeout(() => setSaveMsg(''), 2000);
  }

  function addTask(task: Task) {
    const newTasks = [...tasks, task];
    setTasks(newTasks);
    saveTasks(newTasks);
    setShowForm(false);
  }

  function updateTask(task: Task) {
    const newTasks = tasks.map(t => t.id === task.id ? task : t);
    setTasks(newTasks);
    saveTasks(newTasks);
    setEditingTask(null);
  }

  function deleteTask(id: string) {
    const newTasks = tasks.filter(t => t.id !== id);
    setTasks(newTasks);
    saveTasks(newTasks);
  }

  function importFromPlan() {
    if (planned.length === 0) return;
    const toImport = planned.filter(p => !tasks.some(t => t.id === p.id || t.name === p.name));
    const newTasks = [...tasks, ...toImport.map(t => ({ ...t, id: Date.now().toString(36) + Math.random().toString(36).slice(2) }))];
    setTasks(newTasks);
    saveTasks(newTasks);
    setImportingPlanned(false);
  }

  const total = totalMinutes(tasks);
  const plannedTotal = totalMinutes(planned);

  return (
    <div>
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-2xl font-bold text-slate-100">Log Actual Day</h1>
          <p className="text-slate-400 text-sm mt-1">{formatDateLabel(date)}</p>
        </div>
        <div className="flex items-center gap-3">
          <input
            type="date"
            value={date}
            onChange={e => setDate(e.target.value)}
            className="bg-slate-800 border border-slate-600 rounded-lg px-3 py-2 text-sm text-slate-100 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>
      </div>

      {/* Import from Plan banner */}
      {planned.length > 0 && tasks.length === 0 && (
        <div className="bg-indigo-900/30 border border-indigo-500/30 rounded-xl p-4 mb-6 flex items-center justify-between gap-4">
          <div>
            <p className="text-sm font-medium text-indigo-300">You have a plan for today!</p>
            <p className="text-xs text-indigo-400/80 mt-0.5">
              {planned.length} tasks planned ({fmtMins(plannedTotal)}). Import as starting point?
            </p>
          </div>
          <button
            onClick={importFromPlan}
            className="bg-indigo-600 hover:bg-indigo-500 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors whitespace-nowrap"
          >
            Import Plan
          </button>
        </div>
      )}

      {/* Stats */}
      {tasks.length > 0 && (
        <div className="grid grid-cols-3 gap-4 mb-6">
          <div className="bg-slate-800 rounded-xl p-4 border border-slate-700 text-center">
            <div className="text-2xl font-bold text-indigo-400">{tasks.length}</div>
            <div className="text-xs text-slate-400 mt-1">Tasks Logged</div>
          </div>
          <div className="bg-slate-800 rounded-xl p-4 border border-slate-700 text-center">
            <div className="text-2xl font-bold text-emerald-400">{fmtMins(total)}</div>
            <div className="text-xs text-slate-400 mt-1">Total Logged</div>
          </div>
          {plannedTotal > 0 && (
            <div className="bg-slate-800 rounded-xl p-4 border border-slate-700 text-center">
              <div className="text-2xl font-bold text-amber-400">
                {Math.round((total / plannedTotal) * 100)}%
              </div>
              <div className="text-xs text-slate-400 mt-1">vs Planned</div>
            </div>
          )}
        </div>
      )}

      {/* Add Task Button */}
      {!showForm && !editingTask && (
        <button
          onClick={() => setShowForm(true)}
          className="w-full bg-slate-800 hover:bg-slate-750 border border-dashed border-slate-600 hover:border-indigo-500 rounded-xl p-4 text-slate-400 hover:text-indigo-400 text-sm font-medium transition-all flex items-center justify-center gap-2 mb-4"
        >
          <span className="text-lg">+</span> Log a Task
        </button>
      )}

      {/* Task Form */}
      {showForm && !editingTask && (
        <div className="mb-4">
          <TaskForm
            categories={categories}
            onSubmit={addTask}
            onCancel={() => setShowForm(false)}
          />
        </div>
      )}

      {/* Task List */}
      <div className="space-y-3">
        {tasks.length === 0 && !showForm ? (
          <div className="text-center py-16 text-slate-500">
            <div className="text-5xl mb-4">✅</div>
            <p className="text-lg font-medium text-slate-400">Nothing logged yet</p>
            <p className="text-sm mt-1">Log what you actually did today</p>
          </div>
        ) : (
          tasks.map(task =>
            editingTask?.id === task.id ? (
              <div key={task.id}>
                <TaskForm
                  categories={categories}
                  onSubmit={updateTask}
                  onCancel={() => setEditingTask(null)}
                  initial={editingTask}
                />
              </div>
            ) : (
              <TaskCard
                key={task.id}
                task={task}
                onEdit={() => setEditingTask(task)}
                onDelete={() => deleteTask(task.id)}
              />
            )
          )
        )}
      </div>

      {/* Save indicator */}
      {(saving || saveMsg) && (
        <div className="fixed bottom-6 right-6 bg-slate-800 border border-slate-600 rounded-xl px-4 py-2 text-sm text-slate-200 shadow-lg">
          {saving ? '💾 Saving...' : '✅ ' + saveMsg}
        </div>
      )}
    </div>
  );
}
