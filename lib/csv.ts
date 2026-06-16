import { Task } from './types';

export function tasksToCSV(tasks: Task[]): string {
  const header = 'id,name,category,startTime,endTime,duration,notes';
  const rows = tasks.map(t =>
    [
      t.id,
      `"${t.name.replace(/"/g, '""')}"`,
      `"${t.category.replace(/"/g, '""')}"`,
      t.startTime,
      t.endTime,
      t.duration,
      `"${(t.notes || '').replace(/"/g, '""')}"`,
    ].join(',')
  );
  return [header, ...rows].join('\n');
}

export function csvToTasks(csv: string): Task[] {
  const lines = csv.trim().split('\n');
  if (lines.length < 2) return [];
  // skip header
  return lines.slice(1).map(line => {
    const cols = parseCSVLine(line);
    return {
      id: cols[0] || '',
      name: cols[1] || '',
      category: cols[2] || '',
      startTime: cols[3] || '',
      endTime: cols[4] || '',
      duration: parseInt(cols[5] || '0', 10) || 0,
      notes: cols[6] || '',
    };
  }).filter(t => t.id && t.name);
}

function parseCSVLine(line: string): string[] {
  const result: string[] = [];
  let current = '';
  let inQuotes = false;
  for (let i = 0; i < line.length; i++) {
    const ch = line[i];
    if (ch === '"') {
      if (inQuotes && line[i + 1] === '"') {
        current += '"';
        i++;
      } else {
        inQuotes = !inQuotes;
      }
    } else if (ch === ',' && !inQuotes) {
      result.push(current);
      current = '';
    } else {
      current += ch;
    }
  }
  result.push(current);
  return result;
}
