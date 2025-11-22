// src/components/PriceItem.tsx
import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as ClipboardAPI from 'expo-clipboard';

interface PriceItemProps {
  label: string;
  value: number;
  icon: keyof typeof Ionicons.glyphMap;
  theme: {
    text: string;
    iconColor: string;
  };
}

export default function PriceItem({ label, value, icon, theme }: PriceItemProps) {
  const handleCopy = async (text: string) => {
    if(text) await ClipboardAPI.setStringAsync(text);
  };

  return (
    <TouchableOpacity onPress={() => handleCopy(String(value))} style={styles.priceItem}>
      <Ionicons name={icon} size={24} color={theme.iconColor} />
      <Text style={[styles.priceVal, { color: theme.text }]}>
        {value ? value.toFixed(2) : '...'}
      </Text>
      <Text style={styles.priceLabel}>{label}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  priceItem: { alignItems: 'center', flex: 1 },
  priceVal: { fontSize: 16, fontWeight: '700', marginVertical: 4 },
  priceLabel: { fontSize: 10, color: '#888', fontWeight: '600' },
});
