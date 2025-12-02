// src/features/tables/components/DraggableTableTile.tsx
import React from 'react';
import { View, Text, StyleSheet, Platform } from 'react-native';
import type { TableTile } from '@/types/tables';
import { useDraggableTableTile } from '@/features/tables/hooks/useDraggableTableTile';

type Props = {
  tile: TableTile;
  cellSize: number;
  onDrop: (id: string, newX: number, newY: number) => void;
  onToggleStatus: (id: string) => void;
};

export const DraggableTableTile: React.FC<Props> = ({
  tile,
  cellSize,
  onDrop,
  onToggleStatus,
}) => {
  const { panHandlers, position, isDragging } = useDraggableTableTile({
    tile,
    cellSize,
    onDrop,
    onToggleStatus,
  });

  const backgroundColor =
    tile.status === 'free'
      ? '#22c55e'
      : tile.status === 'reserved'
      ? '#eab308'
      : '#ef4444';

  return (
    <View
      {...panHandlers}
      style={[
        styles.tableTile,
        {
          left: position.left,
          top: position.top,
          width: cellSize,
          height: cellSize,
          backgroundColor,
          opacity: isDragging ? 0.7 : 1,
          transform: [{ scale: isDragging ? 1.08 : 1 }],
          ...(Platform.OS === 'web'
            ? ({ userSelect: 'none', cursor: 'grab' } as any)
            : {}),
        },
      ]}
    >
      <Text
        style={styles.tileText}
        selectable={false} // niente selezione "2" su web
      >
        {tile.seats > 0 ? tile.seats : ''}
      </Text>
    </View>
  );
};

const styles = StyleSheet.create({
  tableTile: {
    position: 'absolute',
    borderRadius: 6,
    alignItems: 'center',
    justifyContent: 'center',
  },
  tileText: {
    color: '#f9fafb',
    fontWeight: '700',
  },
});
