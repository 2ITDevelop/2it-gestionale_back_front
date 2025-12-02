// src/features/tables/config.ts
import type { FloorPlan, TableTile } from '@/types/tables';

export const GRID_COLS = 12;
export const GRID_ROWS = 8;
export const CELL_SIZE = 32;

const INITIAL_TILES: TableTile[] = [
  { id: 't1', x: 2, y: 2, seats: 2, status: 'free', groupId: 'g1' },
  { id: 't2', x: 3, y: 2, seats: 2, status: 'free', groupId: 'g1' },
  { id: 't3', x: 4, y: 2, seats: 2, status: 'free', groupId: 'g1' },
  { id: 't4', x: 6, y: 4, seats: 2, status: 'reserved', groupId: 'g2' },
];

export const INITIAL_PLAN: FloorPlan = {
  id: 'demo-plan-1',
  name: 'Sala Principale',
  width: GRID_COLS,
  height: GRID_ROWS,
  tiles: INITIAL_TILES,
};
