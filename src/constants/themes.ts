// src/constants/theme.ts
export const theme = {
  light: {
    background: ['#E0C3FC', '#8EC5FC'] as [string, string], // Gradiente Pastel Morado -> Azul
    text: '#2D3436',
    primary: '#6C5CE7',
    primarySoft: 'rgba(108, 92, 231, 0.2)',
    iconColor: '#6C5CE7',
    placeholder: '#636e72',
    softHighlight: 'rgba(0,0,0,0.05)',
    secondaryText: '#888',
    dividerColor: 'rgba(150,150,150,0.3)',
    gradientEnd: '#a29bfe',
    copyText: 'rgba(0,0,0,0.6)',
    errorBackground: '#e74c3c',
    white: '#FFFFFF',
    glassBorder: 'rgba(0,0,0,0.1)',
    resultGradientColors: ['#a29bfe', '#aeb0cfff', '#a29bfe'] as [string, string, string],
  },
  dark: {
    background: ['#2d1b4e', '#1e272e'] as [string, string], // Gradiente Oscuro Profundo
    text: '#DFE6E9',
    primary: '#A29BFE',
    primarySoft: 'rgba(162, 155, 254, 0.2)',
    iconColor: '#A29BFE',
    placeholder: '#b2bec3',
    softHighlight: 'rgba(255,255,255,0.1)',
    secondaryText: '#BBBBBB',
    dividerColor: 'rgba(150,150,150,0.3)',
    gradientEnd: '#a29bfe',
    copyText: 'rgba(255,255,255,0.8)',
    errorBackground: '#e74c3c',
    white: '#FFFFFF',
    glassBorder: 'rgba(255,255,255, 0.2)',
    resultGradientColors: ['#a29bfe', '#be9bfeff', '#a29bfe'] as [string, string, string],
  }
};