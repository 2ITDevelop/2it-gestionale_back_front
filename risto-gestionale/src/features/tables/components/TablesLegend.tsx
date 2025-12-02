// src/features/tables/components/TablesLegend.tsx
import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

type LegendItemProps = { color: string; label: string };

const LegendItem: React.FC<LegendItemProps> = ({ color, label }) => (
  <View style={styles.legendItem}>
    <View style={[styles.legendColor, { backgroundColor: color }]} />
    <Text style={styles.legendText}>{label}</Text>
  </View>
);

export const TablesLegend: React.FC = () => {
  return (
    <View style={styles.legend}>
      <LegendItem color="#22c55e" label="Libero" />
      <LegendItem color="#eab308" label="Prenotato" />
      <LegendItem color="#ef4444" label="Occupato" />
    </View>
  );
};

const styles = StyleSheet.create({
  legend: {
    flexDirection: 'row',
    justifyContent: 'center',
    gap: 16,
    marginBottom: 8,
  },
  legendItem: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  legendColor: {
    width: 16,
    height: 16,
    borderRadius: 4,
  },
  legendText: {
    color: '#e5e7eb',
  },
});
