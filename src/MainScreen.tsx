// src/MainScreen.tsx
import React, { useState, useMemo, useRef, useEffect } from 'react';
import {
  View, Text, StyleSheet, TextInput, TouchableOpacity, Image, StatusBar, Keyboard, Animated
} from 'react-native';
import { SafeAreaProvider, SafeAreaView } from 'react-native-safe-area-context';
import { LinearGradient } from 'expo-linear-gradient';
import * as ClipboardAPI from 'expo-clipboard';
import { Ionicons } from '@expo/vector-icons'; // Usaremos iconos vectoriales para modernizar
import PriceItem from './components/PriceItem';
import { theme } from './constants/themes';
import i18n from './constants/i18n';
import { useRates } from './hooks/useRates';
import GlassCard from './components/GlassCard';

export default function MainScreen() {
  // Theme State
  const [isDarkMode, setIsDarkMode] = useState(true); // Por defecto oscuro
  const currentTheme = isDarkMode ? theme.dark : theme.light;

  // App Logic State
  const { rates, loading, refresh, error, clearError } = useRates();
  const [isDollarToBs, setIsDollarToBs] = useState(true);
  const [activeType, setActiveType] = useState<'bcv' | 'digital' | 'average'>('bcv');
  const [input, setInput] = useState('');
  const [isSwapped, setIsSwapped] = useState(false); // Reintroduced state for visual swapping

  // Animation for result fade in
  const fadeAnim = useRef(new Animated.Value(0)).current;

  // Cálculo
  const result = useMemo(() => {
    const val = parseFloat(input.replace(',', '.'));
    if (!input || isNaN(val)) return '';
    const rate = rates[activeType];
    return isDollarToBs ? (val * rate).toFixed(2) : (val / rate).toFixed(2);
  }, [input, rates, activeType, isDollarToBs]);

  const rightLabel = useMemo(() => {
    return isDollarToBs ? 'Bs' : '$';
  }, [isDollarToBs]);

  // Handlers
  const toggleTheme = () => setIsDarkMode(!isDarkMode);
  const handleCopy = async (text: string) => {
    if(text) await ClipboardAPI.setStringAsync(text);
  };
  const toggleAllSwap = () => { // New combined handler for swap
    setIsDollarToBs(prev => !prev);
    setIsSwapped(prev => !prev);
  };

  // Animation trigger
  useEffect(() => {
    if (result) {
      Animated.timing(fadeAnim, {
        toValue: 1,
        duration: 300,
        useNativeDriver: true,
      }).start();
    } else {
      fadeAnim.setValue(0);
    }
  }, [result, fadeAnim]);

  const leftPill = (
    <TouchableOpacity
        onPress={() => setActiveType(prev => prev === 'bcv' ? 'digital' : prev === 'digital' ? 'average' : 'bcv')}
        style={[styles.pillButton, { borderColor: currentTheme.primary, backgroundColor: currentTheme.primarySoft, width: '33%' }]}
        accessibilityLabel={i18n.t('changeRateType')}
    >
        <Text style={{ color: currentTheme.primary, fontWeight: 'bold', textAlign: 'center' }}>
            {i18n.t(activeType)}
        </Text>
    </TouchableOpacity>
  );

  const rightPill = (
    <View style={[styles.pillButton, { borderColor: currentTheme.primary, width: '33%' }]}>
        <Text style={{ color: currentTheme.primary, fontWeight: 'bold', textAlign: 'center' }}>
            {isDollarToBs ? 'USD' : 'VES'}
        </Text>
    </View>
  );

  return (
    <LinearGradient
      colors={currentTheme.background}
      style={styles.container}
      start={{ x: 0, y: 0 }}
      end={{ x: 1, y: 1 }}
    >
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} translucent />
      
      <SafeAreaProvider>
        <SafeAreaView style={styles.safeArea}>
        
        {/* Header con Toggle de Tema */}
        <View style={styles.header}>
            {/* Boton Refresh */}
            <TouchableOpacity onPress={refresh} style={[styles.iconBtn, { backgroundColor: currentTheme.softHighlight }]} accessibilityLabel={i18n.t('refreshRates')}>
                <Ionicons name={loading ? "hourglass" : "refresh"} size={26} color={currentTheme.text} />
            </TouchableOpacity>

            {/* Boton Tema */}
            <TouchableOpacity onPress={toggleTheme} style={[styles.iconBtn, { backgroundColor: currentTheme.softHighlight }]} accessibilityLabel={i18n.t('toggleTheme')}>
                <Ionicons name={isDarkMode ? "sunny" : "moon"} size={26} color={currentTheme.text} />
            </TouchableOpacity>
        </View>

        <View style={styles.content}>
            {/* Precios Rápidos en Tarjeta Glass */}
            <GlassCard theme={currentTheme} style={styles.ratesCard}>
                <View style={styles.ratesRow}>
                    <PriceItem label={i18n.t('bcv')} value={rates.bcv} icon="business" theme={currentTheme} />
                    <View style={styles.divider} />
                    <PriceItem label={i18n.t('average')} value={rates.average} icon="stats-chart" theme={currentTheme} />
                    <View style={styles.divider} />
                    <PriceItem label={i18n.t('digital')} value={rates.digital} icon="cloud" theme={currentTheme} />
                </View>
            </GlassCard>

            {/* Controles de Conversión */}
            <View style={[styles.controlsRow, { justifyContent: 'space-evenly' }]}>
                {isSwapped ? rightPill : leftPill}

                {/* Botón Swap */}
                <View style={{ width: '33%', alignItems: 'center', justifyContent: 'center' }}>
                  <TouchableOpacity onPress={toggleAllSwap} accessibilityLabel={i18n.t('swapCurrencies')}>
                      <Ionicons name="swap-horizontal" size={32} color={currentTheme.primary} />
                  </TouchableOpacity>
                </View>

                {isSwapped ? leftPill : rightPill}
            </View>

            {/* Input Grande Glass */}
            <GlassCard theme={currentTheme} style={styles.inputCard}>
                <TextInput
                    value={input}
                    onChangeText={setInput}
                    placeholder={isDollarToBs ? i18n.t('amountInUsd') : i18n.t('amountInBs')}
                    placeholderTextColor={currentTheme.placeholder}
                    keyboardType="numeric"
                    style={[styles.input, { color: currentTheme.text }]}
                    textAlign="center"
                />
            </GlassCard>

            {/* Resultado Glass con Gradiente interno opcional */}
            {result ? (
              <TouchableOpacity onPress={() => handleCopy(result)} activeOpacity={0.9} style={styles.resultBoxContainer} disabled={!result}>
                <Animated.View style={[styles.resultBox, { opacity: fadeAnim }]}>
                  <LinearGradient colors={currentTheme.resultGradientColors} start={{ x: 0, y: 0 }} end={{ x: 1, y: 1 }} style={styles.resultBoxInner}>
                    <Text style={[styles.resultText, { color: currentTheme.white }]}>{result ? `${result} ${rightLabel}` : '---'}</Text>
                    {result !== '' && <Text style={[styles.copyText, { color: currentTheme.copyText }]}>{i18n.t('tapToCopy')}</Text>}
                  </LinearGradient>
                </Animated.View>
              </TouchableOpacity>
            ) : null}
        </View>

        {/* Manejo de Errores simple */}
        {error && (
            <View style={[styles.errorBanner, { backgroundColor: currentTheme.errorBackground }]}>
                <Text style={[styles.errorText, { color: currentTheme.white }]}>{i18n.t(error)}</Text>
                <TouchableOpacity onPress={clearError} accessibilityLabel={i18n.t('dismissError')}><Ionicons name="close-circle" size={24} color={currentTheme.white} /></TouchableOpacity>
            </View>
        )}

        </SafeAreaView>
      </SafeAreaProvider>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  safeArea: { flex: 1 },
  header: { flexDirection: 'row', justifyContent: 'space-between', paddingHorizontal: 25, marginBottom: 20, marginTop: 20 },
  iconBtn: { padding: 8, borderRadius: 12 },
  content: { paddingHorizontal: 20, alignItems: 'center' },
  ratesCard: { width: '100%', marginBottom: 30 },
  ratesRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  priceItem: { alignItems: 'center', flex: 1 },
  priceVal: { fontSize: 16, fontWeight: '700', marginVertical: 4 },
  priceLabel: { fontSize: 10, color: '#888', fontWeight: '600' },
  divider: { width: 1, height: 30, backgroundColor: 'rgba(150,150,150,0.3)' },
  controlsRow: { flexDirection: 'row', width: '100%', justifyContent: 'space-between', alignItems: 'center', marginBottom: 25 },
  pillButton: { paddingVertical: 10, paddingHorizontal: 20, borderRadius: 20, borderWidth: 1.5 },
  inputCard: { width: '100%', height: 80, marginBottom: 25 },
  input: { fontSize: 22, fontWeight: 'bold', paddingVertical: 10 },
  resultBoxContainer: {
    width: '100%',
    height: 100,
    borderRadius: 16,
    overflow: 'hidden',
  },
  resultBox: {
    borderRadius: 16,
    shadowColor: '#9b59b6',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.25,
    shadowRadius: 25,
    elevation: 12,
  },
  resultBoxInner: {
    paddingVertical: 22,
    paddingHorizontal: 28,
    alignItems: 'center',
    justifyContent: 'center',
  },
  resultText: { fontSize: 32, fontWeight: '800' },
  copyText: { fontSize: 12, marginTop: 5 },
  errorBanner: { position: 'absolute', bottom: 30, alignSelf: 'center', padding: 15, borderRadius: 30, flexDirection: 'row', alignItems: 'center', gap: 10 },
  errorText: { fontWeight: '600' }
});