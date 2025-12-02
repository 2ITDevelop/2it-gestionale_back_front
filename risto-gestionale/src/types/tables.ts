// src/types/tables.ts

export type TableStatus = 'free' | 'reserved' | 'occupied';

export interface TableTile {
  id: string;
  x: number; // colonna nella griglia
  y: number; // riga nella griglia
  groupId?: string;
  seats: number;
  status: TableStatus;
}

export interface FloorPlan {
  id: string;
  name: string;
  width: number;
  height: number;
  tiles: TableTile[];
}
