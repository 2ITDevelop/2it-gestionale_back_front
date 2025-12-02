import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { PanResponder, PanResponderGestureState } from 'react-native';
import type { TableTile } from '@/types/tables';

type Params = {
  tile: TableTile;
  cellSize: number;
  onDrop: (id: string, newX: number, newY: number) => void;
  onToggleStatus: (id: string) => void;
};

const TAP_THRESHOLD = 5;

export function useDraggableTableTile({
  tile,
  cellSize,
  onDrop,
  onToggleStatus,
}: Params) {
  const [isDragging, setIsDragging] = useState(false);
  const [drag, setDrag] = useState({ dx: 0, dy: 0 });

  const startLeft = useRef(tile.x * cellSize);
  const startTop = useRef(tile.y * cellSize);

  const isDraggingRef = useRef(false);
  const frameIdRef = useRef<number | null>(null);

  // quando cambiano coordinate logiche (es. caricamento da DB),
  // riallineo solo la base, senza toccare drag/isDragging
  useEffect(() => {
    startLeft.current = tile.x * cellSize;
    startTop.current = tile.y * cellSize;

    if (frameIdRef.current != null) {
      cancelAnimationFrame(frameIdRef.current);
      frameIdRef.current = null;
    }
  }, [tile.x, tile.y, cellSize]);

  const resetDrag = () => {
    setIsDragging(false);
    isDraggingRef.current = false;
    setDrag({ dx: 0, dy: 0 });

    if (frameIdRef.current != null) {
      cancelAnimationFrame(frameIdRef.current);
      frameIdRef.current = null;
    }
  };

  const finalize = useCallback(
    (gestureState: PanResponderGestureState) => {
      const { dx, dy } = gestureState;
      const dist = Math.sqrt(dx * dx + dy * dy);

      if (dist < TAP_THRESHOLD) {
        resetDrag();
        onToggleStatus(tile.id);
        return;
      }

      const finalLeft = startLeft.current + dx;
      const finalTop = startTop.current + dy;

      const newX = Math.round(finalLeft / cellSize);
      const newY = Math.round(finalTop / cellSize);

      // 🔥 Allineo SUBITO la base alla nuova cella
      startLeft.current = newX * cellSize;
      startTop.current = newY * cellSize;

      // così il prossimo render disegna già il tavolo nel punto giusto
      resetDrag();

      onDrop(tile.id, newX, newY);
    },
    [cellSize, onDrop, onToggleStatus, tile.id]
  );

  const panResponder = useMemo(
    () =>
      PanResponder.create({
        onStartShouldSetPanResponder: () => true,
        onMoveShouldSetPanResponder: () => true,

        onPanResponderGrant: () => {
          // non tocco la base, è già allineata dall'useEffect
          resetDrag();
        },

        onPanResponderMove: (_evt, gestureState) => {
          const { dx, dy } = gestureState;
          const dist = Math.sqrt(dx * dx + dy * dy);

          if (dist < TAP_THRESHOLD) return;

          if (!isDraggingRef.current) {
            isDraggingRef.current = true;
            setIsDragging(true);
          }

          if (frameIdRef.current != null) {
            cancelAnimationFrame(frameIdRef.current);
          }

          frameIdRef.current = requestAnimationFrame(() => {
            setDrag({ dx, dy });
          });
        },

        onPanResponderRelease: (_evt, gestureState) => {
          finalize(gestureState);
        },
        onPanResponderTerminate: (_evt, gestureState) => {
          finalize(gestureState);
        },
      }),
    [finalize]
  );

  const left = startLeft.current + drag.dx;
  const top = startTop.current + drag.dy;

  return {
    panHandlers: panResponder.panHandlers,
    position: { left, top },
    isDragging,
  };
}
