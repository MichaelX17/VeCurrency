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

type RateType = 'bcv' | 'digital' | 'euro';

const VeCurrency = () => {
  // Estados principales
  const [isDollarToBs, setIsDollarToBs] = useState(true);
  const [type, setType] = useState<RateType>('bcv');
  const [input, setInput] = useState('');
  const [rates, setRates] = useState({
    bcv: 0,
    digital: 0,
    euro: 0 
  });
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

    const response = await fetch(url, {
      ...options,
      signal: controller.signal
    });

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
            NavigationBar.setBackgroundColorAsync('#000000'),
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

  // Función para obtener el precio de Binance
  const fetchBinancePrice = async () => {
    const response = await fetch('https://p2p.binance.com/bapi/c2c/v2/friendly/c2c/adv/search', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        "page": 1,
        "rows": 1,
        "payTypes": [],
        "asset": "USDT",
        "tradeType": "SELL",
        "fiat": "VES",
        "transAmount": ""
      })
    });

    if (!response.ok) {
      throw new Error('Error al obtener precio de Binance');
    }

    const data = await response.json();
    if (!data.success || !data.data?.[0]?.adv?.price) {
      throw new Error('Datos de Binance no disponibles');
    }

    return parseFloat(data.data[0].adv.price);
  };

  // Obtener tasas de cambio (modificada)
  const fetchRates = useCallback(async () => {
    try {
      setIsRefreshing(true);

      // Verificar conexión antes de hacer la petición
      const networkState = await Network.getNetworkStateAsync();
      if (!networkState.isConnected || !networkState.isInternetReachable) {
        throw new Error('No hay conexión a internet');
      }

      // Hacer las peticiones
      const [resBCV, binancePrice] = await Promise.all([
        fetch('https://pydolarve.org/api/v1/dollar?page=bcv'),
        fetchBinancePrice()
      ]);

      if (!resBCV.ok) {
        throw new Error('Error en la respuesta del servidor BCV');
      }

      const dataBCV = await resBCV.json();

      setRates({
        bcv: dataBCV.monitors?.usd?.price || 0,
        digital: binancePrice || 0,  // Precio de Binance
        euro: dataBCV.monitors?.eur?.price || 0  // Precio del Euro desde BCV
      });
    } catch (error) {
      // Manejo de errores (igual que antes)
      if (error instanceof Error) {
        setErrorMessage(
          error.message.includes('internet')
            ? 'No hay conexión a internet. Conéctese para obtener tasas actualizadas.'
            : 'Los servidores no están respondiendo. Intente nuevamente más tarde.'
        );
      } else {
        setErrorMessage('Ocurrió un error desconocido');
      }
      setIsErrorModalVisible(true);
    } finally {
      setIsRefreshing(false);
    }
  }, []);

  // Cargar tasas al montar el componente
  useEffect(() => {
    fetchRates();
  }, [fetchRates]);

  // Animación para copiar al portapapeles
  const animateCopy = useCallback(() => {
    Animated.sequence([
      Animated.timing(fadeAnim, {
        toValue: 0.4,
        duration: 100,
        useNativeDriver: true,
      }),
      Animated.timing(fadeAnim, {
        toValue: 1,
        duration: 300,
        useNativeDriver: true,
      })
    ]).start();
  }, [fadeAnim]);

  // Copiar al portapapeles
  const handleCopyToClipboard = useCallback(async (text: string) => {
    if (text) {
      await ClipboardAPI.setStringAsync(text);
      animateCopy();
    }
  }, [animateCopy]);

  // Cálculo del resultado de conversión (modificado para usar 'digital' y 'euro')
  const result = useMemo(() => {
    const value = parseFloat(input.replace(',', '.'));
    if (isNaN(value) || !input) return '';

    let rate = 0;
    switch (type) {
      case 'bcv': rate = rates.bcv; break;
      case 'digital': rate = rates.digital; break;
      case 'euro': rate = rates.euro; break;
    }

    const resultValue = isDollarToBs ? value * rate : value / rate;
    return resultValue.toFixed(2);
  }, [input, isDollarToBs, type, rates]);

  // Animación del resultado
  useEffect(() => {
    if (result) {
      fadeAnim.setValue(0);
      Animated.timing(fadeAnim, {
        toValue: 1,
        duration: 500,
        useNativeDriver: true,
      }).start();
    }
  }, [result, fadeAnim]);

  // Handlers
  const handleSwap = () => setIsDollarToBs(prev => !prev);

  // En el render, cambiar los textos para reflejar los nuevos tipos
  const handleTypeToggle = useCallback(() => {
    setType(prev => {
      if (prev === 'bcv') return 'digital';
      if (prev === 'digital') return 'euro';
      return 'bcv';
    });
  }, []);

  const handleInputChange = useCallback((text: string) => {
    const cleaned = text.replace(/[^0-9.]/g, '');
    const valid = cleaned.split('.').length <= 2 ? cleaned : input;
    setInput(valid);
  }, [input]);

  const handleBlur = useCallback(() => Keyboard.dismiss(), []);

  const openExternalLink = useCallback(async (url: string) => {
    try {
      if (!/^https?:\/\//i.test(url)) {
        url = `https://${url}`;
      }
      await Linking.openURL(url);
    } catch (error) {
      console.error('Error al abrir el enlace:', error);
      setErrorMessage('No se pudo abrir el enlace');
      setIsErrorModalVisible(true);
    }
  }, []);

  // Valores
  const rightLabel =
    (type === 'euro' && !isDollarToBs) ? 'EUR' :   // Cuando convertimos Bs → €
      (type === 'euro' && isDollarToBs) ? 'VES' :    // Cuando convertimos € → Bs
        isDollarToBs ? 'VES' : 'USD';                  // Casos normales ($ ↔ Bs)

  const placeholderText =
    isDollarToBs
      ? (type === 'euro' ? 'Monto en €' : 'Monto en $')  // Cuando convertimos €/$ → Bs
      : 'Monto en Bs';                                   // Cuando convertimos Bs → €/$

  // Componente para mostrar las tasas
  const RateBox = React.memo(({
    icon,
    value,
    label
  }: {
    icon: any;
    value: number | string;
    label?: string
  }) => (
    <TouchableOpacity
      style={styles.iconBox}
      onPress={() => handleCopyToClipboard(String(value))}
    >
      <Image source={icon} style={styles.iconImage} resizeMode="contain" />
      <Text style={styles.priceText}>{value}</Text>
      {label && <Text style={styles.priceLabel}>{label}</Text>}
    </TouchableOpacity>
  ));

  return (
    <View style={styles.container}>
      <StatusBar style="light" backgroundColor="transparent" translucent />

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity
          onPress={() => openExternalLink('https://www.github.com/MichaelX17')}
          activeOpacity={0.7}
          style={styles.logoWrapper}
        >
          <Image
            source={require('./assets/logo.png')}
            style={styles.logo}
            resizeMode="contain"
          />
        </TouchableOpacity>

        <TouchableOpacity
          onPress={fetchRates}
          activeOpacity={0.7}
          style={styles.logoWrapper}
          disabled={isRefreshing}
        >
          {isRefreshing ? (
            <ActivityIndicator color="#9b59b6" size="small" />
          ) : (
            <Image
              source={require('./assets/refresh.png')}
              style={[styles.refresh, { tintColor: '#9b59b6' }]}
              resizeMode="contain"
            />
          )}
        </TouchableOpacity>
      </View>

      {/* Tasas de cambio */}
      <View style={styles.fastPrices}>
        <View style={styles.box}>
          <RateBox
            icon={require('./assets/bcv-icon.png')}
            value={rates.bcv || '...'}
            label="BCV"
          />
          <RateBox
            icon={require('./assets/euro.png')}  // Necesitarás un icono para el euro
            value={rates.euro || '...'}
            label="EURO"
          />
          <RateBox
            icon={require('./assets/dollar.png')}  // Necesitarás un icono para digital
            value={rates.digital || '...'}
            label="DIGITAL"
          />
        </View>
      </View>

      {/* Selector de tipo de cambio */}
      <View style={styles.row}>
        {isDollarToBs ? (
          <>
            <TouchableOpacity style={styles.typeButton} onPress={handleTypeToggle}>
              <Text style={styles.typeButtonText}>
                {type === 'bcv' ? 'BCV' : type === 'digital' ? 'DIGI' : 'EURO'}
              </Text>
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
              <Text style={styles.typeButtonText}>
                {type === 'bcv' ? 'BCV' : type === 'digital' ? 'DIGI' : 'EURO'}
              </Text>
            </TouchableOpacity>
          </>
        )}
      </View>

      {/* Input */}
      {/* Input */}
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
        />
        <Text style={styles.currencySymbol}>
          {!isDollarToBs ? 'Bs' : (type === 'euro' ? '€' : '$')}
        </Text>
      </View>

      {/* Resultado */}
      <TouchableOpacity
        onPress={() => handleCopyToClipboard(result)}
        activeOpacity={0.8}
        style={styles.resultBoxContainer}
        disabled={!result}
      >
        <Animated.View style={[styles.resultBox, { opacity: fadeAnim }]}>
          <LinearGradient
            colors={['#9b59b6', '#835cf9', '#5c64fa']}
            start={{ x: 0, y: 0 }}
            end={{ x: 1, y: 1 }}
            style={styles.resultBoxInner}
          >
            <Text style={styles.resultText}>
              {result ? `${result} ${rightLabel}` : '---'}
            </Text>
            {result !== '' && <Text style={styles.copyNote}>Toca para copiar</Text>}
          </LinearGradient>
        </Animated.View>
      </TouchableOpacity>

      {/* Modal de Error */}
      <Modal
        isVisible={isErrorModalVisible}
        onBackdropPress={() => setIsErrorModalVisible(false)}
        backdropColor="#000"
        backdropOpacity={0.8}
        animationIn="zoomIn"
        animationOut="zoomOut"
        animationInTiming={300}
        animationOutTiming={300}
        style={styles.modal}
      >
        <View style={styles.modalContent}>
          <Image
            source={require('./assets/logo.png')}
            style={styles.modalIcon}
            resizeMode="contain"
          />
          <Text style={styles.modalTitle}>Error</Text>
          <Text style={styles.modalText}>{errorMessage}</Text>

          <View style={styles.modalButtons}>
            <TouchableOpacity
              style={[styles.modalButton, styles.retryButton]}
              onPress={() => {
                setIsErrorModalVisible(false);
                fetchRates();
              }}
            >
              <Text style={styles.modalButtonText}>Reintentar</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.modalButton, styles.closeButton]}
              onPress={() => setIsErrorModalVisible(false)}
            >
              <Text style={styles.modalButtonText}>Cerrar</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </View>
  );
};

// Estilos
const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0b0c1e',
    alignItems: 'center',
    paddingTop: 60,
    paddingHorizontal: 20,
  },
  header: {
    width: '100%',
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  logoWrapper: {
    width: 50,
    height: 50,
    alignItems: 'center',
    justifyContent: 'center',
  },
  logo: {
    width: 40,
    height: 40,
  },
  refresh: {
    width: 35,
    height: 35,
  },
  fastPrices: {
    width: '80%',
    alignItems: 'center',
    marginTop: 20,
    marginBottom: 20,
  },
  box: {
    flexDirection: 'row',
    backgroundColor: '#1a1b2f',
    borderRadius: 20,
    padding: 10,
    justifyContent: 'space-around',
    width: '100%',
    shadowColor: '#9b59b6',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.3,
    shadowRadius: 30,
    elevation: 15,
  },
  iconBox: {
    alignItems: 'center',
    flex: 1,
  },
  iconImage: {
    width: 35,
    height: 35,
    marginBottom: 10,
    tintColor: '#8e44ad',
  },
  priceText: {
    color: 'white',
    fontWeight: 'bold',
    fontSize: 18,
  },
  priceLabel: {
    color: '#aaa',
    fontSize: 12,
    marginTop: 4,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 25,
    width: '80%',
    justifyContent: 'space-between',
  },
  typeButton: {
    backgroundColor: '#000',
    padding: 10,
    borderRadius: 8,
    width: 95,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 2,
    borderColor: '#9b59b6',
  },
  typeButtonText: {
    color: '#9b59b6',
    fontWeight: '600',
    fontSize: 14,
  },
  currencyBox: {
    backgroundColor: '#000',
    padding: 10,
    borderRadius: 8,
    width: 95,
    alignItems: 'center',
    borderWidth: 2,
    borderColor: '#9b59b6',
  },
  currencyText: {
    color: '#9b59b6',
    fontWeight: '600',
  },
  swapBtn: {
    padding: 10,
  },
  swapIcon: {
    fontSize: 30,
    color: '#fff',
  },
  inputContainer: {
    width: '80%',
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 2,
    borderColor: '#9b59b6',
    borderRadius: 10,
    backgroundColor: '#000',
    marginBottom: 20,
    paddingLeft: 20,
  },
  input: {
    flex: 1,
    height: 60,
    color: '#9b59b6',
    fontSize: 20,
    textAlign: 'center',
    includeFontPadding: false,
    letterSpacing: 0.5,
  },
  currencySymbol: {
    width: 40,
    color: '#9b59b6',
    fontSize: 20,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  resultBoxContainer: {
    width: '80%',
    borderRadius: 12,
    overflow: 'hidden',
  },
  resultBox: {
    borderRadius: 12,
    shadowColor: '#9b59b6',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.3,
    shadowRadius: 30,
    elevation: 15,
  },
  resultBoxInner: {
    paddingVertical: 20,
    paddingHorizontal: 30,
    alignItems: 'center',
    justifyContent: 'center',
  },
  resultText: {
    color: '#fff',
    fontSize: 22,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  copyNote: {
    marginTop: 5,
    fontSize: 12,
    color: '#fff',
    opacity: 0.7,
  },
  modal: {
    justifyContent: 'center',
    alignItems: 'center',
    margin: 0,
  },
  modalContent: {
    backgroundColor: '#1a1b2f',
    width: '80%',
    borderRadius: 15,
    padding: 25,
    alignItems: 'center',
    borderWidth: 2,
    borderColor: '#FD706D',
  },
  modalIcon: {
    width: 60,
    height: 60,
    marginBottom: 15,
    tintColor: '#FD706D',
  },
  modalTitle: {
    color: '#FD706D',
    fontSize: 22,
    fontWeight: 'bold',
    marginBottom: 10,
    textAlign: 'center',
  },
  modalText: {
    color: '#fff',
    fontSize: 16,
    marginBottom: 20,
    textAlign: 'center',
    lineHeight: 22,
  },
  modalButtons: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    width: '100%',
    marginTop: 10,
  },
  modalButton: {
    flex: 1,
    paddingVertical: 12,
    borderRadius: 8,
    alignItems: 'center',
    marginHorizontal: 5,
  },
  retryButton: {
    backgroundColor: '#9b59b6',
  },
  closeButton: {
    backgroundColor: '#333',
    borderWidth: 1,
    borderColor: '#9b59b6',
  },
  modalButtonText: {
    color: 'white',
    fontWeight: '600',
    fontSize: 16,
  },
});

export default VeCurrency;