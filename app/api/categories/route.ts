import { NextRequest, NextResponse } from 'next/server';
import fs from 'fs';
import path from 'path';

const filePath = path.join(process.cwd(), 'data', 'categories.json');

function readCategories(): string[] {
  if (!fs.existsSync(filePath)) return ['Work', 'Learning', 'Exercise', 'Personal'];
  return JSON.parse(fs.readFileSync(filePath, 'utf-8'));
}

export async function GET() {
  return NextResponse.json({ categories: readCategories() });
}

export async function POST(req: NextRequest) {
  const { categories }: { categories: string[] } = await req.json();
  const dir = path.dirname(filePath);
  if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
  fs.writeFileSync(filePath, JSON.stringify(categories, null, 2), 'utf-8');
  return NextResponse.json({ success: true });
}
