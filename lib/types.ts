export interface Task {
  id: string;
  name: string;
  category: string;
  startTime: string;  // e.g. "09:00" or ""
  endTime: string;    // e.g. "10:00" or ""
  duration: number;   // in minutes
  notes: string;
}

export interface DayPlan {
  date: string;
  tasks: Task[];
}

export interface ComparisonData {
  date: string;
  planned: Task[];
  actual: Task[];
  score: number;
  matchedMinutes: number;
  totalPlannedMinutes: number;
  missedTasks: Task[];
  unplannedTasks: Task[];
}
