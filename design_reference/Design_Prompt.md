# VeCurrency — UI/UX Redesign Implementation Prompt

## Role

You are a Senior Android UI/UX Engineer and Kotlin Jetpack Compose specialist. Your task is to redesign the existing **VeCurrency** Android application using the provided visual reference.

You must work directly on the existing project, modifying its actual screens and components. Do not create a separate demo application or static prototype.

## 1. Design Reference

The visual reference is located at:

`./design_reference/interface.png`

**IMPORTANT:** Before making any changes, open and carefully analyze this image.

This image is the primary visual source of truth for the redesign.

Your objective is to reproduce its visual language, layout philosophy, spacing, proportions, colors, typography, card treatments, navigation, and overall aesthetic while adapting the design to VeCurrency's actual functionality.

Do not introduce a completely different design system.

### Visual direction

The application must follow these characteristics:

- Premium dark-mode interface.
- Deep charcoal and almost-black backgrounds.
- Subtle gradients and layered dark surfaces.
- Large rounded cards and containers.
- Soft shadows and restrained borders.
- Minimalist white and light-gray typography.
- Carefully placed colored accents.
- Spacious layouts with consistent padding.
- Minimalist icons.
- Smooth, modern visual hierarchy.
- Professional financial application aesthetic.

Avoid:
- Excessive colors.
- Bright backgrounds.
- Unnecessary decorative elements.
- Excessive borders.
- Generic Material Design default-looking screens.
- Oversized gradients.
- Unnecessary animations.
- Visual clutter.

The result should feel like a premium financial application, not a generic cryptocurrency dashboard.

---

## 2. Main Application Screens

The reference presents three mobile screens. Reproduce their visual structure and adapt them to the following VeCurrency functionality.

### Screen 1 — Home / Exchange Rates

This is the primary screen.

Reproduce the structure of the first phone in the reference.

Required elements:

**Header**
- VeCurrency application name.
- Subtitle: Venezuela · Tipos de cambio.
- Settings icon.

**Exchange rate cards**

Create three prominent cards:

1. USD — BCV
2. EUR — BCV
3. USDT — Binance

Each card must contain:
- Currency icon or recognizable currency symbol.
- Currency name.
- Source name.
- Current price in VES.
- Optional percentage variation if historical data exists.
- Last successful update time.
- Copy-to-clipboard action.

Use the reference's visual approach:
- USD: subtle blue accent.
- EUR: subtle purple accent.
- USDT: subtle yellow accent.

Each card should have a dark surface, rounded corners, and a restrained colored accent along its left edge or border.

**Refresh information**

Below the exchange cards, create two separate information panels:

BCV:
- Next scheduled refresh.
- Venezuelan local time.
- Calendar icon.
- Yellow accent for upcoming refresh information.

USDT:
- Next eligible refresh.
- Four-hour refresh interval.
- Countdown based on the last successful update.
- Binance icon or recognizable identifier.

**Bottom navigation**

Follow the reference's floating dark navigation bar.

Required destinations:
- Inicio
- Calculadora
- Historial

The active destination must have a subtle blue or purple indicator.

---

### Screen 2 — Currency Calculator

Reproduce the structure of the second phone in the reference.

This screen must prioritize simplicity and usability.

Required elements:

**Header**
- Back navigation or appropriate navigation behavior.
- Title: Calculadora.

**Currency selection**
- USD
- EUR
- USDT
- VES

Allow users to select the source and destination currencies.

**Source amount card**

Include:
- Currency icon.
- Currency code.
- Dropdown selector.
- Editable numeric amount.
- Clear input action.

**Swap button**

Place a circular swap button between the source and destination cards.

It must exchange the selected currencies.

**Destination amount card**

Include:
- Destination currency icon.
- Currency code.
- Converted amount.

**Equivalencias section**

Below the calculator, display an equivalence breakdown showing how the entered amount compares across the available currencies.

**Actions**
- Copy calculated result.
- Clear calculator.
- Paste amount using normal Android clipboard behavior.

Maintain the same dark surfaces, rounded cards, typography, spacing, and hierarchy as the reference.

---

### Screen 3 — History / Insights

Reproduce the structure of the third phone in the reference.

This screen should present historical information and data-source transparency.

**Header**
- Title: Detalles e Insights.

**Rate overview**

Create a circular visual indicator showing the percentage variation of a selected rate, if sufficient historical data exists.

Use positive, neutral, and negative states.

Do not invent historical percentages or data.

If historical data is unavailable, show an appropriate empty state.

**Historical chart**

Display historical exchange-rate evolution.

Include:
- USD (BCV).
- EUR (BCV).
- USDT (Binance).

Use different restrained accent colors:
- Blue for USD.
- Purple for EUR.
- Yellow for USDT.

Provide time filters:
- 1D
- 1S
- 1M
- 3M
- 1A

Only display periods supported by locally available historical data.

**Source information**

Create a dedicated section showing:

BCV:
- Source identifier.
- USD/EUR availability.
- Last successful update.
- Update status.

Binance:
- Source identifier.
- USDT availability.
- Last successful update.
- Update status.

**Next refresh information**

Display the next BCV scheduled update and its Venezuelan local time.

---

## 3. Design System

Create reusable Compose components and a centralized theme.

Suggested palette:

| Element | Color |
|---|---|
| Main background | #101116 |
| Secondary background | #171A22 |
| Card background | #1B1F29 |
| Elevated surface | #222631 |
| Primary text | #F5F6FA |
| Secondary text | #A5A9B5 |
| USD accent | #45B5FF |
| EUR accent | #B46AFF |
| USDT accent | #F4BD24 |
| Success | #35D98A |
| Warning | #F4BD24 |
| Error | #FF626F |

These colors are starting points. Adjust them to match the reference image more closely.

### Typography

Use a modern sans-serif typography system.

Suggested hierarchy:
- Screen titles: 22–26sp.
- Currency codes: 20–24sp.
- Exchange values: 24–28sp, semibold or bold.
- Section titles: 17–20sp.
- Supporting text: 13–15sp.
- Timestamps: 12–13sp.

Keep typography consistent across all screens.

### Components

Build reusable components such as:

- VeCurrencyTopBar
- ExchangeRateCard
- RefreshScheduleCard
- CalculatorCurrencyCard
- CurrencySelector
- SwapCurrencyButton
- EquivalenceCard
- HistoricalChartCard
- SourceInformationCard
- BottomNavigationBar

Avoid duplicating UI implementations.

---

## 4. Technical Requirements

The redesign must preserve the existing application's architecture and functionality.

Before changing code:

1. Inspect the project structure.
2. Identify the existing navigation implementation.
3. Identify ViewModels and UI state models.
4. Identify existing repositories and data sources.
5. Identify existing theme and reusable components.
6. Understand how the application currently retrieves and stores exchange rates.

Use Jetpack Compose and Material 3 if that is what the existing project uses.

Do not replace the existing architecture just to implement the redesign.

### Strict functional requirements

The following behavior must remain intact:

- BCV USD/EUR scraping.
- Binance USDT data retrieval.
- BCV refresh schedule: 09:30, 15:00 and 21:30 Venezuelan time.
- USDT refresh every four hours after the last successful fetch.
- No background fetching.
- Network fetching only while the application is foregrounded.
- Local persistence of successful exchange rates.
- Offline access to cached rates.
- Individual timestamps for each source.
- Accurate stale, loading, error and unavailable states.
- BigDecimal-based currency calculations.
- Copy and paste functionality.
- Dashboard image export/share functionality.

The UI must observe real application state.

Never hardcode exchange rates, timestamps, percentage variations, countdowns or historical chart values.

---

## 5. Interaction and Animation

Add subtle interactions consistent with the reference:

- Smooth screen transitions.
- Subtle card press feedback.
- Animated calculator currency swapping.
- Smooth chart transitions.
- Refresh loading indicators.
- Copy confirmation feedback.
- Animated refresh countdown updates.

Animations should be lightweight and must not interfere with refresh operations or application lifecycle behavior.

---

## 6. Responsive Design and Accessibility

Ensure the redesigned application works on:

- Small Android phones.
- Standard Android phones.
- Large-screen devices.

Consider:
- Dynamic font scaling.
- TalkBack accessibility.
- Appropriate contrast.
- Minimum touch-target sizes.
- Keyboard behavior.
- Scrollable content.
- Edge-to-edge system bars.

Do not sacrifice usability to reproduce the reference's proportions.

---

## 7. Implementation Workflow

Work incrementally.

### Phase 1 — Analyze
- Inspect the reference image.
- Inspect the existing project.
- Identify the current UI implementation.
- Create a list of screens and components that require redesign.

### Phase 2 — Design foundation
- Implement the new color palette.
- Implement typography.
- Implement reusable surfaces and cards.
- Implement navigation styling.

### Phase 3 — Redesign screens
Implement in this order:

1. Home / Exchange Rates.
2. Currency Calculator.
3. History / Insights.
4. Supporting screens and settings.

### Phase 4 — Functional integration
- Connect every screen to existing ViewModels.
- Preserve repository and database behavior.
- Verify refresh states.
- Verify offline behavior.
- Verify calculator functionality.
- Verify clipboard actions.

### Phase 5 — Validation
- Build the application.
- Run existing tests.
- Fix compilation errors.
- Fix visual inconsistencies.
- Check different screen sizes.
- Compare screenshots against `./design_reference/interface.png`.

---

## 8. Important Rules

1. Do not modify business logic unless absolutely necessary.
2. Do not replace real data with mock data.
3. Do not remove existing features.
4. Do not create a static UI prototype.
5. Do not introduce new backend dependencies.
6. Do not introduce background fetching.
7. Do not break existing navigation.
8. Do not leave placeholder buttons or nonfunctional controls.
9. Reuse existing dependencies whenever possible.
10. Keep the implementation modular and maintainable.

**Most importantly: the reference image defines the visual direction, but the existing application defines the functionality. Both must be respected.**

---

## 9. Definition of Done

The redesign is complete when:

- All primary screens visually follow the reference.
- The application has a consistent premium dark theme.
- All exchange-rate information comes from actual application data.
- Calculator functionality remains operational.
- Historical information is based on real stored data.
- All refresh schedules remain unchanged.
- Offline functionality remains operational.
- Copy, paste and image export continue working.
- No existing feature has been accidentally removed.
- The project builds successfully.
- Existing tests pass.

## Final Deliverable

Modify the actual Android project.

When finished, provide:

1. Summary of redesigned screens.
2. List of modified and created files.
3. Description of reusable components.
4. Build and test results.
5. Any remaining visual or functional limitations.

Update `Progress.md` if this project is using the VeCurrency task-tracking workflow.

Start by inspecting `./design_reference/interface.png` and the existing project. Then implement the redesign.
