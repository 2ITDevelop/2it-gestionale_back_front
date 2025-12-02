// src/components/navigation/TabBarIcon.tsx
import React from 'react';
import { Ionicons } from '@expo/vector-icons';

type TabBarIconProps = {
  name: React.ComponentProps<typeof Ionicons>['name'];
  color: string;
};

export function TabBarIcon({ name, color }: TabBarIconProps) {
  return <Ionicons name={name} color={color} size={28} style={{ marginBottom: -3 }} />;
}
