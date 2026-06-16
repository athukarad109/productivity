import { NextRequest, NextResponse } from 'next/server';
import fs from 'fs';
import path from 'path';
import { tasksToCSV, csvToTasks } from '@/lib/csv';
import { Task } from '@/lib/types';

function getFilePath(date: string) {
  return path.join(process.cwd(), 'data', 'plans', `${date}.csv`);
}

export async function GET(
  _req: NextRequest,
  { params }: { params: Promise<{ date: string }> }
) {
  const { date } = await params;
  const filePath = getFilePath(date);
  if (!fs.existsSync(filePath)) {
    return NextResponse.json({ tasks: [] });
  }
  const csv = fs.readFileSync(filePath, 'utf-8');
  return NextResponse.json({ tasks: csvToTasks(csv) });
}

export async function POST(
  req: NextRequest,
  { params }: { params: Promise<{ date: string }> }
) {
  const { date } = await params;
  const { tasks }: { tasks: Task[] } = await req.json();
  const dir = path.join(process.cwd(), 'data', 'plans');
  if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
  fs.writeFileSync(getFilePath(date), tasksToCSV(tasks), 'utf-8');
  return NextResponse.json({ success: true });
}
