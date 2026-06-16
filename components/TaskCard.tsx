'use client';

import { Task } from '@/lib/types';

const CATEGORY_COLORS: Record<string, string> = {
  Work: 'bg-blue-500/20 text-blue-300 border-blue-500/30',
  Learning: 'bg-purple-500/20 text-purple-300 border-purple-500/30',
  Exercise: 'bg-green-500/20 text-green-300 border-green-500/30',
  Personal: 'bg-yellow-500/20 text-yellow-300 border-yellow-500/30',
  Health: 'bg-rose-500/20 text-rose-300 border-rose-500/30',
  Social: 'bg-orange-500/20 text-orange-300 border-orange-500/30',
  Creative: 'bg-pink-500/20 text-pink-300 border-pink-500/30',
  Admin: 'bg-slate-500/20 text-slate-300 border-slate-500/30',
};

function getColor(category: string) {
  return CATEGORY_COLORS[category] || 'bg-indigo-500/20 text-indigo-300 border-indigo-500/30';
}

function formatTime(t: string) {
  if (!t) return '';
  const [h, m] = t.split(':').map(Number);
  const period = h >= 12 ? 'PM' : 'AM';
  const hour = h % 12 || 12;
  return `${hour}:${m.toString().padStart(2, '0')} ${period}`;
}

interface Props {
  task: Task;
  onEdit?: () => void;
  onDelete?: () => void;
  variant?: 'planned' | 'actual' | 'missed' | 'unplanned';
}

export default function TaskCard({ task, onEdit, onDelete, variant }: Props) {
  const colorClass = getColor(task.category);

  const variantStyle = variant === 'missed'
    ? 'border-red-500/30 bg-red-900/10'
    : variant === 'unplanned'
    ? 'border-emerald-500/30 bg-emerald-900/10'
    : 'border-slate-700 bg-slate-800';

  return (
    <div className={`rounded-xl p-4 border ${variantStyle} flex items-start justify-between gap-3 group transition-all`}>
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 flex-wrap mb-1">
          <h3 className="font-medium text-slate-100 text-sm truncate">{task.name}</h3>
          <span className={`text-xs px-2 py-0.5 rounded-full border ${colorClass} font-medium`}>
            {task.category}
          </span>
          {variant === 'missed' && (
            <span className="text-xs px-2 py-0.5 rounded-full bg-red-500/20 text-red-300 border border-red-500/30 font-medium">
              Missed
            </span>
          )}
          {variant === 'unplanned' && (
            <span className="text-xs px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 font-medium">
              Unplanned
            </span>
          )}
        </div>
        <div className="flex items-center gap-3 text-xs text-slate-400">
          {task.startTime && task.endTime ? (
            <span>🕐 {formatTime(task.startTime)} – {formatTime(task.endTime)}</span>
          ) : null}
          {task.duration > 0 && (
            <span>⏱ {task.duration >= 60 ? `${Math.floor(task.duration / 60)}h ${task.duration % 60 > 0 ? task.duration % 60 + 'm' : ''}`.trim() : `${task.duration}m`}</span>
          )}
        </div>
        {task.notes && (
          <p className="text-xs text-slate-500 mt-1 truncate">{task.notes}</p>
        )}
      </div>
      {(onEdit || onDelete) && (
        <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
          {onEdit && (
            <button
              onClick={onEdit}
              className="p-1.5 rounded-lg hover:bg-slate-700 text-slate-400 hover:text-slate-200 transition-colors"
              title="Edit"
            >
              ✏️
            </button>
          )}
          {onDelete && (
            <button
              onClick={onDelete}
              className="p-1.5 rounded-lg hover:bg-red-900/50 text-slate-400 hover:text-red-300 transition-colors"
              title="Delete"
            >
              🗑️
            </button>
          )}
        </div>
      )}
    </div>
  );
}
