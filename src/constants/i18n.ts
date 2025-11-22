// src/constants/i18n.ts
import { getLocales } from 'expo-localization';
import { I18n } from 'i18n-js';

const i18n = new I18n({
  en: {
    bcv: 'BCV',
    digital: 'DIGITAL',
    average: 'AVERAGE',
    amountInUsd: 'Amount in $',
    amountInBs: 'Amount in Bs',
    tapToCopy: 'Tap to copy',
    error: 'Error',
    retry: 'Retry',
    close: 'Close',
    noInternet: 'No internet connection.',
    serverError: 'Servers are not responding.',
    bcvApiError: 'Failed to fetch BCV rates.',
    binanceApiError: 'Failed to fetch Binance rates.',
    requestTimeout: 'Request timed out. Please try again.',
    refreshRates: 'Refresh rates',
    toggleTheme: 'Toggle theme',
    changeRateType: 'Change rate type',
    swapCurrencies: 'Swap currencies',
    copyResult: 'Copy result',
    dismissError: 'Dismiss error'
  },
  es: {
    bcv: 'BCV',
    digital: 'DIGITAL',
    average: 'PROMEDIO',
    amountInUsd: 'Monto en $',
    amountInBs: 'Monto en Bs',
    tapToCopy: 'Toca para copiar',
    error: 'Error',
    retry: 'Reintentar',
    close: 'Cerrar',
    noInternet: 'No hay conexión a internet.',
    serverError: 'Los servidores no responden.',
    bcvApiError: 'No se pudieron obtener las tasas del BCV.',
    binanceApiError: 'No se pudieron obtener las tasas de Binance.',
    requestTimeout: 'La solicitud ha excedido el tiempo límite. Inténtalo de nuevo.',
    refreshRates: 'Actualizar tasas',
    toggleTheme: 'Cambiar tema',
    changeRateType: 'Cambiar tipo de tasa',
    swapCurrencies: 'Intercambiar monedas',
    copyResult: 'Copiar resultado',
    dismissError: 'Descartar error'
  }
});

// Configura el idioma basado en el dispositivo
const deviceLanguage = getLocales()[0].languageCode;
i18n.locale = deviceLanguage === 'es' ? 'es' : 'en';
i18n.enableFallback = true;

export default i18n;