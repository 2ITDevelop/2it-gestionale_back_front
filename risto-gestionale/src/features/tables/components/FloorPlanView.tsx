// src/features/tables/components/FloorPlanView.tsx
import React from 'react';
import { View, StyleSheet } from 'react-native';
import type { FloorPlan } from '@/types/tables';
import { DraggableTableTile } from './DraggableTableTile';

type Props = {
  plan: FloorPlan;
  cellSize: number;
  onDrop: (id: string, newX: number, newY: number) => void;
  onToggleStatus: (id: string) => void;
};

export const FloorPlanView: React.FC<Props> = ({
  plan,
  cellSize,
  onDrop,
  onToggleStatus,
}) => {
  const gridCols = plan.width;
  const gridRows = plan.height;

  return (
    <View style={styles.planWrapper}>
      <View
        style={[
          styles.plan,
          { width: gridCols * cellSize, height: gridRows * cellSize },
        ]}
      >
        {/* griglia */}
        {Array.from({ length: gridRows }).map((_, row) => (
          <View key={row} style={styles.gridRow}>
            {Array.from({ length: gridCols }).map((__, col) => (
              <View
                key={col}
                style={[
                  styles.gridCell,
                  { width: cellSize, height: cellSize },
                ]}
              />
            ))}
          </View>
        ))}

        {/* tavoli */}
        {plan.tiles.map(tile => (
          <DraggableTableTile
            key={tile.id}
            tile={tile}
            cellSize={cellSize}
            onDrop={onDrop}
            onToggleStatus={onToggleStatus}
          />
        ))}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  planWrapper: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  plan: {
    backgroundColor: '#0f172a',
    borderRadius: 12,
    // padding: 4,        👈 TOGLI QUESTO
    position: 'relative',
    overflow: 'hidden',   // opzionale, per tagliare eventuali bordi
  },
  gridRow: {
    flexDirection: 'row',
  },
  gridCell: {
    borderWidth: 0.5,
    borderColor: '#1f2937',
  },
});

