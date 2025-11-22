# 💱 VeCurrency App

A modern, feature-rich mobile currency converter built with **React Native & Expo** that provides real-time exchange rates between **USD**, **USDT**, and **Venezuelan Bolivars (VES)**. Features a beautiful glassmorphism design with dark/light mode support and multiple rate sources.

---

## ✨ Features

### 🔄 Real-time Currency Conversion
- Convert between **USD ↔ VES** and **VES ↔ USD** with a single tap
- Support for multiple rate sources:
  - 📊 **Official BCV Rate** (Government rate)
  - 💱 **Digital Dollar Rate** (Binance P2P)
  - ⚖️ **Average Rate** (BCV + Digital average)
- Automatic calculations as you type

### 🎨 Modern UI/UX
- **Glassmorphism Design** with blurred glass cards
- **Dark/Light Mode** toggle with smooth transitions
- **Animated Interactions** for copy feedback and state changes
- **Gradient Backgrounds** for visual appeal
- **Responsive Layout** optimized for mobile

### 🔧 Smart Functionality
- **One-tap Copy** results to clipboard with visual feedback
- **Smart Rate Switching** between BCV, Digital, and Average rates
- **Network Error Handling** with user-friendly messages
- **Auto-refresh** capability for latest rates
- **Input Validation** for numeric values

### 🌐 Multi-source Rate Fetching
- **BCV API** for official government rates
- **Binance P2P API** for digital dollar rates
- **Automatic Fallbacks** if one API fails
- **Network State Detection** with timeout protection

---## 🎨 Theming System

### Dark Theme
- Deep gradient backgrounds
- High contrast text
- Vibrant accent colors

### Light Theme  
- Light gradient backgrounds
- Dark text for readability
- Softer accent colors

---

## 📸 Screenshots

| Dark Mode | Light Mode |
|:---:|:---:|
| <img width="540" height="1071" alt="Dark_Mode" src="https://github.com/user-attachments/assets/bdbd0feb-77af-4d1d-956e-8981a222699f" /> | <img width="540" height="1071" alt="Light_Mode" src="https://github.com/user-attachments/assets/17ac5f64-a192-40a5-abbd-864dde4f03ad" /> |

---

## 🚀 Getting Started

### Prerequisites
- Node.js (v14 or newer)
- Expo CLI
- iOS Simulator or Android Emulator, or physical device with Expo Go

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/MichaelX17/vecurrency-app.git
   cd vecurrency-app
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Start the development server**
   ```bash
   npx expo start
   ```

4. **Run on your device**
   - Scan the QR code with Expo Go (Android) or Camera (iOS)
   - Or press `a` for Android emulator / `i` for iOS simulator

---

## 🛠️ Technical Stack

### Core Technologies
- **React Native** with **Expo** SDK
- **TypeScript** for type safety
- **Expo Blur** for glassmorphism effects
- **Linear Gradient** for beautiful backgrounds
- **React Navigation** for navigation (if multi-screen)

### State & Data Management
- **Custom Hooks** (`useRates`) for rate management
- **React Context API** for theme management
- **Async Storage** for persistence (if implemented)

### APIs & Services
- **BCV Rates API** (`bcv-api.rafnixg.dev`)
- **Binance P2P API** for digital rates
- **Expo Clipboard** for copy functionality
- **Expo Network** for connectivity detection

### UI Components
- **Custom GlassCard** component for glassmorphism
- **PriceItem** for rate displays
- **Animated API** for smooth transitions
- **Ionicons** for consistent iconography

---

## 📱 Usage Guide

### Converting Currency
1. **Enter Amount** - Type in the input field (automatically calculates)
2. **Select Rate Type** - Tap the left pill to cycle through BCV → Digital → Average
3. **Swap Direction** - Tap the swap icon to change between USD→VES or VES→USD
4. **Copy Result** - Tap the result card to copy to clipboard

### Managing Rates
- **Refresh Rates** - Tap the refresh icon in header
- **View All Rates** - See BCV, Average, and Digital rates at a glance
- **Copy Rates** - Tap any rate display to copy its value

### Customization
- **Toggle Theme** - Use the sun/moon icon to switch between dark/light mode
- **Auto-refresh** - Pull down or use refresh button for latest rates

---



## 🔌 API Integration

### Rate Sources
- **BCV Official Rate**: `https://bcv-api.rafnixg.dev/rates/`
- **Binance P2P Rate**: Binance P2P API for USDT/VES
- **Average Rate**: Calculated from (BCV + Digital) / 2

### Error Handling
- **Network Timeouts**: 10-second request timeout
- **API Fallbacks**: Graceful degradation if one API fails
- **Connectivity Checks**: Network state detection before requests

---


## 🔮 Future Enhancements

- ⏰ **Scheduled Updates** - Automatic rate refresh every 5 minutes
- 📈 **Rate History** - Historical charts and trends
- 🔔 **Rate Alerts** - Push notifications for target rates
- 💾 **Local Storage** - Cache rates for offline use
- 🌍 **Multi-currency** - Support for other Latin American currencies
- 📊 **Advanced Charts** - Interactive rate visualization

---

## 🐛 Troubleshooting

### Common Issues
- **Rates not loading**: Check internet connection and API status
- **Build errors**: Clear Expo cache with `npx expo start -c`
- **Clipboard not working**: Ensure app has clipboard permissions

### Performance Tips
- Uses React Native Animated for 60fps animations
- Memoized calculations prevent unnecessary re-renders
- Optimized API calls with timeout protection

---

## 👨‍💻 Development

### Contributing
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- TypeScript for type safety
- Functional components with hooks
- Consistent naming conventions
- Modular component structure

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 👤 Developed By

**Miguel Farfan**
- Computer Science Engineer | Frontend/Backend/Mobile/Desktop Developer
- 📧 mfpersonal777@gmail.com  
- 🌐 [GitHub](https://github.com/MichaelX17)
- 💼 [LinkedIn](https://linkedin.com/in/miguelfarfan) 
- 💼 [Workana](https://www.workana.com/freelancer/9da9f40c57fe3491650d3ddfdc37af91) 

---
