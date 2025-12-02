// src/screens/TablesScreen.tsx
import React from 'react';
import { View, Text, StyleSheet, Button } from 'react-native';
import { useFloorPlan } from '@/features/tables/hooks/useFloorPlan';
import { FloorPlanView } from '@/features/tables/components/FloorPlanView';
import { TablesLegend } from '@/features/tables/components/TablesLegend';

const TablesScreen: React.FC = () => {
  const {
    plan,
    cellSize,
    totalSeats,
    toggleStatus,
    dropTile,
    addTile,
  } = useFloorPlan();

  return (
    <View style={styles.container}>
      {/* header */}
      <View style={styles.header}>
        <View>
          <Text style={styles.title}>{plan.name}</Text>
          <Text style={styles.subtitle}>
            Moduli tavolo: {plan.tiles.length} • Posti totali: {totalSeats}
          </Text>
        </View>
        <Button title="Aggiungi modulo tavolo" onPress={addTile} />
      </View>

      <FloorPlanView
        plan={plan}
        cellSize={cellSize}
        onDrop={dropTile}
        onToggleStatus={toggleStatus}
      />

      <TablesLegend />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
    gap: 12,
    backgroundColor: '#020617',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: 12,
  },
  title: {
    fontSize: 20,
    fontWeight: '700',
    color: '#f9fafb',
  },
  subtitle: {
    fontSize: 12,
    color: '#9ca3af',
    marginTop: 4,
  },
});

export default TablesScreen;
