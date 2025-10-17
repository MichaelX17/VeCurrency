import React, { useState, useEffect, useRef, useCallback, useMemo } from 'react';
import {
  View, Text, StyleSheet, TextInput, TouchableOpacity, Platform, Image, Keyboard, Animated,
  Linking, ActivityIndicator
} from 'react-native';
import Modal from 'react-native-modal';
import * as ClipboardAPI from 'expo-clipboard';
import { LinearGradient } from 'expo-linear-gradient';
import { StatusBar } from 'expo-status-bar';
import * as NavigationBar from 'expo-navigation-bar';
import * as Network from 'expo-network';

type RateType = 'bcv' | 'digital' | 'average';

const VeCurrency = () => {
  // Estados principales
  const [isDollarToBs, setIsDollarToBs] = useState(true);
  const [type, setType] = useState<RateType>('bcv');
  const [input, setInput] = useState('');
  const [rates, setRates] = useState({ bcv: 0, digital: 0, average: 0 });
  const [isRefreshing, setIsRefreshing] = useState(false);
  const fadeAnim = useRef(new Animated.Value(0)).current;

  // Estados para el modal de error
  const [isErrorModalVisible, setIsErrorModalVisible] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [isConnected, setIsConnected] = useState<boolean | null>(null);

  // Verificar conexión a internet
  useEffect(() => {
    const checkNetwork = async () => {
      const networkState = await Network.getNetworkStateAsync();
      setIsConnected(
        networkState.isConnected && networkState.isInternetReachable
          ? true
          : networkState.isConnected === false || networkState.isInternetReachable === false
            ? false
            : null
      );
    };

    checkNetwork();
    const interval = setInterval(checkNetwork, 10000);
    return () => clearInterval(interval);
  }, []);

  const fetchWithTimeout = async (url: string, options: any = {}, timeout = 5000) => {
    const controller = new AbortController();
    const id = setTimeout(() => controller.abort(), timeout);
    const response = await fetch(url, { ...options, signal: controller.signal });
    clearTimeout(id);
    return response;
  };

  // Configuración de la barra de navegación (Android)
  useEffect(() => {
    const configureAndroidNavigation = async () => {
      if (Platform.OS === 'android') {
        try {
          await Promise.all([
            NavigationBar.setPositionAsync('absolute'),
            NavigationBar.setBackgroundColorAsync('#0d0f20'),
            NavigationBar.setButtonStyleAsync('light'),
            NavigationBar.setVisibilityAsync('visible')
          ]);
        } catch (error) {
          console.warn('Error configuring navigation bar:', error);
        }
      }
    };
    configureAndroidNavigation();
  }, []);

  const fetchBinancePrice = async () => {
    const response = await fetch('https://p2p.binance.com/bapi/c2c/v2/friendly/c2c/adv/search', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        page: 1,
        rows: 1,
        payTypes: [],
        asset: 'USDT',
        tradeType: 'SELL',
        fiat: 'VES',
        transAmount: ''
      })
    });
    if (!response.ok) throw new Error('Error al obtener precio de Binance');

    const data = await response.json();
    if (!data.success || !data.data?.[0]?.adv?.price) throw new Error('Datos de Binance no disponibles');
    return parseFloat(data.data[0].adv.price);
  };

  // Obtener tasas de cambio
  const fetchRates = useCallback(async () => {
    try {
      setIsRefreshing(true);
      const networkState = await Network.getNetworkStateAsync();
      if (!networkState.isConnected || !networkState.isInternetReachable) throw new Error('No hay conexión a internet');

      const [resBCV, binancePrice] = await Promise.all([
        fetchWithTimeout('https://bcv-api.rafnixg.dev/rates/'),
        fetchBinancePrice()
      ]);

      if (!resBCV.ok) throw new Error('Error en la respuesta del servidor BCV');
      const dataBCV = await resBCV.json();
      const bcvRate = dataBCV.dollar;
      const averageRate = (bcvRate + binancePrice) / 2;

      setRates({ bcv: bcvRate, digital: binancePrice, average: averageRate });
    } catch (error) {
      setErrorMessage(
        error instanceof Error && error.message.includes('internet')
          ? 'No hay conexión a internet. Conéctese para obtener tasas actualizadas.'
          : 'Los servidores no están respondiendo. Intente nuevamente más tarde.'
      );
      setIsErrorModalVisible(true);
    } finally {
      setIsRefreshing(false);
    }
  }, []);

  useEffect(() => { fetchRates(); }, [fetchRates]);

  const animateCopy = useCallback(() => {
    Animated.sequence([
      Animated.timing(fadeAnim, { toValue: 0.4, duration: 120, useNativeDriver: true }),
      Animated.timing(fadeAnim, { toValue: 1, duration: 350, useNativeDriver: true })
    ]).start();
  }, [fadeAnim]);

  const handleCopyToClipboard = useCallback(async (text: string) => {
    if (text) {
      await ClipboardAPI.setStringAsync(text);
      animateCopy();
    }
  }, [animateCopy]);

  const result = useMemo(() => {
    const value = parseFloat(input.replace(',', '.'));
    if (isNaN(value) || !input) return '';
    const rate = type === 'bcv' ? rates.bcv : type === 'digital' ? rates.digital : rates.average;
    const resultValue = isDollarToBs ? value * rate : value / rate;
    return resultValue.toFixed(2);
  }, [input, isDollarToBs, type, rates]);

  useEffect(() => {
    if (result) {
      fadeAnim.setValue(0);
      Animated.timing(fadeAnim, { toValue: 1, duration: 500, useNativeDriver: true }).start();
    }
  }, [result, fadeAnim]);

  const handleSwap = () => setIsDollarToBs(prev => !prev);
  const handleTypeToggle = useCallback(() => {
    setType(prev => prev === 'bcv' ? 'digital' : prev === 'digital' ? 'average' : 'bcv');
  }, []);
  const handleInputChange = useCallback((text: string) => {
    const cleaned = text.replace(/[^0-9.]/g, '');
    const valid = cleaned.split('.').length <= 2 ? cleaned : input;
    setInput(valid);
  }, [input]);
  const handleBlur = useCallback(() => Keyboard.dismiss(), []);

  const openExternalLink = useCallback(async (url: string) => {
    try {
      if (!/^https?:\/\//i.test(url)) url = `https://${url}`;
      await Linking.openURL(url);
    } catch (error) {
      setErrorMessage('No se pudo abrir el enlace');
      setIsErrorModalVisible(true);
    }
  }, []);

  const rightLabel = isDollarToBs ? 'VES' : 'USD';
  const placeholderText = isDollarToBs ? 'Monto en $' : 'Monto en Bs';

  const RateBox = React.memo(({ icon, value, label }: { icon: any; value: number | string; label?: string }) => (
    <TouchableOpacity style={styles.iconBox} onPress={() => handleCopyToClipboard(String(value))}>
      <Image source={icon} style={styles.iconImage} resizeMode="contain" />
      <Text style={styles.priceText}>{value ? value.toLocaleString() : '...'}</Text>
      {label && <Text style={styles.priceLabel}>{label}</Text>}
    </TouchableOpacity>
  ));

  return (
    <View style={styles.container}>
      <StatusBar style="light" backgroundColor="transparent" translucent />

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => openExternalLink('https://www.github.com/MichaelX17')} activeOpacity={0.8} style={styles.logoWrapper}>
          <Image source={require('./assets/in-app-icons/logo.png')} style={styles.logo} resizeMode="contain" />
        </TouchableOpacity>

        <TouchableOpacity onPress={fetchRates} activeOpacity={0.8} style={styles.logoWrapper} disabled={isRefreshing}>
          {isRefreshing ? (
            <ActivityIndicator color="#9b59b6" size="small" />
          ) : (
            <Image source={require('./assets/in-app-icons/refresh.png')} style={[styles.refresh, { tintColor: '#9b59b6' }]} resizeMode="contain" />
          )}
        </TouchableOpacity>
      </View>

      {/* Tasas */}
      <View style={styles.fastPrices}>
        <View style={styles.box}>
          <RateBox icon={require('./assets/in-app-icons/bcv-icon.png')} value={rates.bcv || '...'} label="BCV" />
          <RateBox icon={require('./assets/in-app-icons/average.png')} value={rates.average || '...'} label="PROMEDIO" />
          <RateBox icon={require('./assets/in-app-icons/dollar.png')} value={rates.digital || '...'} label="DIGITAL" />
        </View>
      </View>

      {/* Selector */}
      <View style={styles.row}>
        {isDollarToBs ? (
          <>
            <TouchableOpacity style={styles.typeButton} onPress={handleTypeToggle}>
              <Text style={styles.typeButtonText}>{type === 'bcv' ? 'BCV' : type === 'digital' ? 'DIGITAL' : 'PROMEDIO'}</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.swapBtn} onPress={handleSwap}>
              <Text style={styles.swapIcon}>⇆</Text>
            </TouchableOpacity>
            <View style={styles.currencyBox}><Text style={styles.currencyText}>Bs</Text></View>
          </>
        ) : (
          <>
            <View style={styles.currencyBox}><Text style={styles.currencyText}>Bs</Text></View>
            <TouchableOpacity style={styles.swapBtn} onPress={handleSwap}>
              <Text style={styles.swapIcon}>⇆</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.typeButton} onPress={handleTypeToggle}>
              <Text style={styles.typeButtonText}>{type === 'bcv' ? 'BCV' : type === 'digital' ? 'DIGITAL' : 'PROMEDIO'}</Text>
            </TouchableOpacity>
          </>
        )}
      </View>

      {/* Input */}
      <View style={styles.inputContainer}>
        <TextInput
          placeholder={placeholderText}
          placeholderTextColor="#888"
          keyboardType="decimal-pad"
          value={input}
          onChangeText={handleInputChange}
          style={styles.input}
          textAlign='center'
          cursorColor="#9b59b6"
          onBlur={handleBlur}
        />
        <Text style={styles.currencySymbol}>{isDollarToBs ? '$' : 'Bs'}</Text>
      </View>

      {/* Resultado */}
      <TouchableOpacity onPress={() => handleCopyToClipboard(result)} activeOpacity={0.9} style={styles.resultBoxContainer} disabled={!result}>
        <Animated.View style={[styles.resultBox, { opacity: fadeAnim }]}>
          <LinearGradient colors={['#9b59b6', '#835cf9', '#5c64fa']} start={{ x: 0, y: 0 }} end={{ x: 1, y: 1 }} style={styles.resultBoxInner}>
            <Text style={styles.resultText}>{result ? `${result} ${rightLabel}` : '---'}</Text>
            {result !== '' && <Text style={styles.copyNote}>Toca para copiar</Text>}
          </LinearGradient>
        </Animated.View>
      </TouchableOpacity>

      {/* Modal */}
      <Modal isVisible={isErrorModalVisible} onBackdropPress={() => setIsErrorModalVisible(false)} backdropColor="#000" backdropOpacity={0.7} animationIn="zoomIn" animationOut="zoomOut" animationInTiming={300} animationOutTiming={250} style={styles.modal}>
        <View style={styles.modalContent}>
          <Image source={require('./assets/in-app-icons/logo.png')} style={styles.modalIcon} resizeMode="contain" />
          <Text style={styles.modalTitle}>Error</Text>
          <Text style={styles.modalText}>{errorMessage}</Text>

          <View style={styles.modalButtons}>
            <TouchableOpacity style={[styles.modalButton, styles.retryButton]} onPress={() => { setIsErrorModalVisible(false); fetchRates(); }}>
              <Text style={styles.modalButtonText}>Reintentar</Text>
            </TouchableOpacity>
            <TouchableOpacity style={[styles.modalButton, styles.closeButton]} onPress={() => setIsErrorModalVisible(false)}>
              <Text style={styles.modalButtonText}>Cerrar</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0d0f20',
    alignItems: 'center',
    paddingTop: 60,
    paddingHorizontal: 20,
  },
  header: {
    width: '100%',
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: 10,
    marginBottom: 0,
  },
  logoWrapper: {
    width: 52,
    height: 52,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 16,
    backgroundColor: 'rgba(255,255,255,0.05)'
  },
  logo: { width: 40, height: 40 },
  refresh: { width: 30, height: 30 },
  fastPrices: { width: '90%', alignItems: 'center', marginVertical: 25 },
  box: {
    flexDirection: 'row',
    backgroundColor: '#1a1b2f',
    borderRadius: 24,
    padding: 14,
    justifyContent: 'space-around',
    width: '100%',
    shadowColor: '#9b59b6',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.25,
    shadowRadius: 20,
    elevation: 12,
  },
  iconBox: { alignItems: 'center', flex: 1 },
  iconImage: { width: 34, height: 34, marginBottom: 8, tintColor: '#9b59b6' },
  priceText: { color: 'white', fontWeight: '600', fontSize: 18 },
  priceLabel: { color: '#aaa', fontSize: 13, marginTop: 4 },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 25,
    width: '90%',
    justifyContent: 'space-between',
  },
  typeButton: {
    backgroundColor: '#15162b',
    paddingVertical: 12,
    borderRadius: 14,
    width: 95,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1.8,
    borderColor: '#9b59b6',
  },
  typeButtonText: { color: '#9b59b6', fontWeight: '700', fontSize: 14 },
  currencyBox: {
    backgroundColor: '#15162b',
    paddingVertical: 12,
    borderRadius: 14,
    width: 95,
    alignItems: 'center',
    borderWidth: 1.8,
    borderColor: '#9b59b6',
  },
  currencyText: { color: '#9b59b6', fontWeight: '700', fontSize: 14 },
  swapBtn: { padding: 10 },
  swapIcon: { fontSize: 40, color: '#fff', margin: -10 },
  inputContainer: {
    width: '90%',
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1.8,
    borderColor: '#9b59b6',
    borderRadius: 14,
    backgroundColor: '#15162b',
    marginBottom: 25,
    paddingLeft: 16,
  },
  input: {
    flex: 1,
    height: 60,
    color: '#fff',
    fontSize: 20,
    textAlign: 'center',
    letterSpacing: 0.6,
  },
  currencySymbol: {
    width: 40,
    color: '#9b59b6',
    fontSize: 20,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  resultBoxContainer: {
    width: '90%',
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
  resultText: { color: '#fff', fontSize: 24, fontWeight: '700', textAlign: 'center' },
  copyNote: { marginTop: 6, fontSize: 12, color: '#fff', opacity: 0.65 },
  modal: { justifyContent: 'center', alignItems: 'center', margin: 0 },
  modalContent: {
    backgroundColor: '#1a1b2f',
    width: '85%',
    borderRadius: 20,
    padding: 28,
    alignItems: 'center',
    borderWidth: 1.8,
    borderColor: '#FD706D',
  },
  modalIcon: { width: 60, height: 60, marginBottom: 15, tintColor: '#FD706D' },
  modalTitle: { color: '#FD706D', fontSize: 22, fontWeight: '700', marginBottom: 10, textAlign: 'center' },
  modalText: { color: '#fff', fontSize: 16, marginBottom: 20, textAlign: 'center', lineHeight: 22 },
  modalButtons: { flexDirection: 'row', justifyContent: 'space-between', width: '100%', marginTop: 10 },
  modalButton: { flex: 1, paddingVertical: 14, borderRadius: 12, alignItems: 'center', marginHorizontal: 6 },
  retryButton: { backgroundColor: '#9b59b6' },
  closeButton: { backgroundColor: '#2b2d42', borderWidth: 1, borderColor: '#9b59b6' },
  modalButtonText: { color: 'white', fontWeight: '600', fontSize: 16 },
});

export default VeCurrency;