// src/components/GlassCard.tsx
import React from 'react';
import { View, StyleSheet, ViewStyle } from 'react-native';
import { BlurView } from 'expo-blur';

interface GlassCardProps {
  children: React.ReactNode;
  style?: ViewStyle;
  intensity?: number;
  theme: {
    glassBorder: string;
  };
}

const GlassCard = ({ children, style, intensity = 50, theme }: GlassCardProps) => {
  return (
    <View style={[styles.container, style, { borderColor: theme.glassBorder }]}>
      <BlurView 
        intensity={intensity} 
        tint={theme.glassBorder.includes('rgba(0,0,0') ? 'light' : 'dark'} // Heuristic to determine tint
        style={StyleSheet.absoluteFill} 
      />
      <View style={styles.content}>
        {children}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    overflow: 'hidden',
    borderRadius: 20,
    borderWidth: 1,
  },
  content: {
    padding: 15,
  }
});

export default GlassCard;