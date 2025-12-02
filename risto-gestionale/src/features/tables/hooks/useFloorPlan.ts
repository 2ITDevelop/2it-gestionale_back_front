// src/features/tables/hooks/useFloorPlan.ts
import { useMemo, useState } from 'react';
import type { FloorPlan, TableStatus, TableTile } from '@/types/tables';
import { GRID_COLS, GRID_ROWS, CELL_SIZE, INITIAL_PLAN } from '../config';


const MODULE_SEATS = 2;

// trova gruppi di moduli adiacenti (sopra/sotto/destra/sinistra)
function findGroups(tiles: TableTile[]): TableTile[][] {
  const groups: TableTile[][] = [];
  const visited = new Set<string>();

  for (const tile of tiles) {
    if (visited.has(tile.id)) continue;

    const queue: TableTile[] = [tile];
    const group: TableTile[] = [];
    visited.add(tile.id);

    while (queue.length) {
      const current = queue.shift()!;
      group.push(current);

      for (const other of tiles) {
        if (visited.has(other.id)) continue;
        const dist =
          Math.abs(other.x - current.x) + Math.abs(other.y - current.y);
        if (dist === 1) {
          visited.add(other.id);
          queue.push(other);
        }
      }
    }

    groups.push(group);
  }

  return groups;
}

// calcola i posti totali di un gruppo in base alla "forma"
function computeGroupSeats(group: TableTile[]): number {
  if (group.length === 1) {
    // 1 modulo = 2 posti
    return MODULE_SEATS;
  }

  const xs = group.map(t => t.x);
  const ys = group.map(t => t.y);
  const minX = Math.min(...xs);
  const maxX = Math.max(...xs);
  const minY = Math.min(...ys);
  const maxY = Math.max(...ys);

  const width = maxX - minX + 1;
  const height = maxY - minY + 1;

  // caso esplicito 1x2 / 2x1 = 6 posti come da tua regola
  if (
    (width === 2 && height === 1) ||
    (width === 1 && height === 2)
  ) {
    return 6;
  }

  const longSide = Math.max(width, height);
  const shortSide = Math.min(width, height);

  // euristica: 2 persone per lato lungo, 1 per lato corto
  const perimeterSeats = longSide * 2 + shortSide * 2;

  // comunque non meno di 2 posti per modulo
  const minSeats = group.length * MODULE_SEATS;

  return Math.max(perimeterSeats, minSeats);
}

// assegna groupId e seats ai moduli
function recomputeGroups(tiles: TableTile[]): TableTile[] {
  const groups = findGroups(tiles);
  const now = Date.now();

  const byId = new Map<string, TableTile>();
  tiles.forEach(t => {
    // reset groupId, seats base
    byId.set(t.id, { ...t, groupId: undefined, seats: MODULE_SEATS });
  });

  groups.forEach((group, index) => {
    const groupTiles = group.map(g => byId.get(g.id)!);

    if (groupTiles.length === 1) {
      // modulo singolo: niente groupId, 2 posti
      const single = groupTiles[0];
      single.groupId = undefined;
      single.seats = MODULE_SEATS;
      return;
    }

    const groupSeats = computeGroupSeats(groupTiles);
    const groupId = `g-${now}-${index}`;

    // scelgo un "modulo principale" (il più in alto, poi più a sinistra)
    const head = [...groupTiles].sort(
      (a, b) => a.y - b.y || a.x - b.x
    )[0];

    groupTiles.forEach(tile => {
      tile.groupId = groupId;
      tile.seats = tile.id === head.id ? groupSeats : 0;
    });
  });

  return Array.from(byId.values());
}


export function useFloorPlan() {
  const [plan, setPlan] = useState<FloorPlan>(INITIAL_PLAN);

    const toggleStatus = (tileId: string) => {
    setPlan(prev => {
      const target = prev.tiles.find(t => t.id === tileId);
      if (!target) return prev;

      const groupTiles = target.groupId
        ? prev.tiles.filter(t => t.groupId === target.groupId)
        : [target];

      const nextStatus: TableStatus =
        target.status === 'free'
          ? 'reserved'
          : target.status === 'reserved'
          ? 'occupied'
          : 'free';

      const tiles = prev.tiles.map(tile =>
        groupTiles.some(g => g.id === tile.id)
          ? { ...tile, status: nextStatus }
          : tile
      );

      return { ...prev, tiles };
    });
  };


    const dropTile = (tileId: string, newX: number, newY: number) => {
    setPlan(prev => {
      // clamp dentro i limiti del piano
      const targetX = Math.max(0, Math.min(prev.width - 1, newX));
      const targetY = Math.max(0, Math.min(prev.height - 1, newY));

      // se la cella è già occupata da un altro modulo → annulla movimento
      const occupiedByOther = prev.tiles.some(
        t => t.id !== tileId && t.x === targetX && t.y === targetY
      );
      if (occupiedByOther) {
        return prev;
      }

      const movedTiles = prev.tiles.map(tile =>
        tile.id === tileId ? { ...tile, x: targetX, y: targetY } : tile
      );

      const tiles = recomputeGroups(movedTiles);

      return { ...prev, tiles };
    });
  };


  const addTile = () => {
    setPlan(prev => {
      const occupied = new Set(prev.tiles.map(t => `${t.x},${t.y}`));
      let freeX = 0;
      let freeY = 0;
      let found = false;

      for (let row = 0; row < prev.height; row++) {
        for (let col = 0; col < prev.width; col++) {
          const key = `${col},${row}`;
          if (!occupied.has(key)) {
            freeX = col;
            freeY = row;
            found = true;
            break;
          }
        }
        if (found) break;
      }

            if (!found) return prev;

      const newTile: TableTile = {
        id: `t-${Date.now()}`,
        x: freeX,
        y: freeY,
        seats: MODULE_SEATS,
        status: 'free',
      };

      const tiles = recomputeGroups([...prev.tiles, newTile]);

      return { ...prev, tiles };
    });
  };


  const totalSeats = useMemo(
    () => plan.tiles.reduce((sum, t) => sum + t.seats, 0),
    [plan.tiles]
  );

  return {
    plan,
    cellSize: CELL_SIZE,
    totalSeats,
    toggleStatus,
    dropTile,
    addTile,
  };
}
