'use client';

import { useState } from 'react';

interface Props {
  categories: string[];
  onUpdate: (categories: string[]) => void;
  onClose: () => void;
}

export default function CategoryManager({ categories, onUpdate, onClose }: Props) {
  const [list, setList] = useState([...categories]);
  const [newCat, setNewCat] = useState('');

  function addCategory() {
    const trimmed = newCat.trim();
    if (trimmed && !list.includes(trimmed)) {
      setList([...list, trimmed]);
      setNewCat('');
    }
  }

  function removeCategory(idx: number) {
    setList(list.filter((_, i) => i !== idx));
  }

  function handleSave() {
    onUpdate(list);
    onClose();
  }

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-slate-800 rounded-2xl border border-slate-700 p-6 w-full max-w-md shadow-2xl">
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-lg font-semibold text-slate-100">Manage Categories</h2>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-200 transition-colors text-xl">✕</button>
        </div>

        <div className="space-y-2 mb-4 max-h-60 overflow-y-auto">
          {list.map((cat, idx) => (
            <div key={idx} className="flex items-center justify-between bg-slate-900 rounded-lg px-3 py-2">
              <span className="text-sm text-slate-200">{cat}</span>
              <button
                onClick={() => removeCategory(idx)}
                className="text-slate-500 hover:text-red-400 transition-colors text-sm"
              >
                ✕
              </button>
            </div>
          ))}
        </div>

        <div className="flex gap-2 mb-5">
          <input
            type="text"
            value={newCat}
            onChange={e => setNewCat(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && (e.preventDefault(), addCategory())}
            placeholder="New category name..."
            className="flex-1 bg-slate-900 border border-slate-600 rounded-lg px-3 py-2 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
          <button
            onClick={addCategory}
            className="bg-indigo-600 hover:bg-indigo-500 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors"
          >
            Add
          </button>
        </div>

        <div className="flex gap-3">
          <button
            onClick={handleSave}
            className="flex-1 bg-indigo-600 hover:bg-indigo-500 text-white font-medium py-2 rounded-lg text-sm transition-colors"
          >
            Save Changes
          </button>
          <button
            onClick={onClose}
            className="px-4 py-2 rounded-lg text-sm font-medium text-slate-400 hover:text-slate-200 hover:bg-slate-700 transition-colors"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
}
