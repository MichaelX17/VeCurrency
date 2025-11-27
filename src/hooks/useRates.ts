// src/hooks/useRates.ts
import { useState, useEffect, useCallback } from 'react';
import * as Network from 'expo-network';

const FETCH_TIMEOUT = 10000; // 10 seconds timeout

interface RequestInitWithTimeout extends RequestInit {
  timeout?: number;
}

async function fetchWithTimeout(url: string, options: RequestInitWithTimeout = {}): Promise<Response> {
  const { timeout = FETCH_TIMEOUT, ...rest } = options;

  const controller = new AbortController();
  const id = setTimeout(() => controller.abort(), timeout);

  const response = await fetch(url, {
    ...rest,
    signal: controller.signal
  });
  clearTimeout(id);

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  return response;
}

export const useRates = () => {
  const [rates, setRates] = useState({ bcv: 0, digital: 0, average: 0 });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // --- Corrección: obtener 10 precios y promediar ---
  const fetchBinancePrice = async (): Promise<number> => {
    try {
      const response = await fetchWithTimeout(
        'https://p2p.binance.com/bapi/c2c/v2/friendly/c2c/adv/search',
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            page: 1,
            rows: 20,
            payTypes: [],
            asset: 'USDT',
            tradeType: 'SELL',
            fiat: 'VES',
            transAmount: ''
          })
        }
      );

      const data = await response.json();

      if (!data.data || data.data.length === 0) {
        throw new Error('BINANCE_DATA_ERROR');
      }

      const prices = data.data
        .map((item: any) =>
          item?.adv?.price ? parseFloat(item.adv.price) : null
        )
        .filter((p: number | null) => typeof p === "number" && !isNaN(p));

      if (prices.length === 0) {
        throw new Error('BINANCE_NO_VALID_PRICES');
      }

      const avg =
        prices.reduce((acc: number, val: number) => acc + val, 0) /
        prices.length;

      return avg;

    } catch (e) {
      if (e instanceof Error && e.name === 'AbortError') {
        throw new Error('BINANCE_TIMEOUT');
      }
      throw e;
    }
  };


  const getRates = useCallback(async () => {
    setLoading(true);
    setError(null);
    let bcvRate = 0;
    let binancePrice = 0;

    try {
      const net = await Network.getNetworkStateAsync();
      if (!net.isConnected) {
        setError('networkError');
        return;
      }

      // Fetch BCV Rate
      try {
        const resBCV = await fetchWithTimeout('https://bcv-api.rafnixg.dev/rates/');
        const dataBCV = await resBCV.json();
        if (dataBCV && typeof dataBCV.dollar === 'number') {
          bcvRate = dataBCV.dollar;
        } else {
          throw new Error('BCV_DATA_ERROR');
        }
      } catch (e) {
        if (e instanceof Error && e.name === 'AbortError') console.warn("BCV fetch timed out");
        console.warn("Failed to fetch BCV rates:", e);
        // Do not set global error yet, try other fetches
      }

      // Fetch Binance Price
      try {
        binancePrice = await fetchBinancePrice();
      } catch (e) {
        if (e instanceof Error && e.name === 'AbortError') console.warn("Binance fetch timed out");
        console.warn("Failed to fetch Binance price:", e);
        // Do not set global error yet, try other fetches
      }

      if (bcvRate === 0 && binancePrice === 0) {
        setError('serverError'); // Both failed
      } else if (bcvRate === 0) {
        setError('bcvApiError'); // BCV failed
      } else if (binancePrice === 0) {
        setError('binanceApiError'); // Binance failed
      }


      setRates({
        bcv: bcvRate,
        digital: binancePrice,
        average: (bcvRate + binancePrice) / 2
      });

    } catch (e) {
      if (e instanceof Error && e.message === 'NETWORK_ERROR') {
        setError('networkError');
      } else if (e instanceof Error && e.name === 'AbortError') {
        setError('requestTimeout');
      } else {
        setError('serverError');
        console.error("An unexpected error occurred:", e);
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { getRates(); }, [getRates]);

  return { rates, loading, error, refresh: getRates, clearError: () => setError(null) };
};