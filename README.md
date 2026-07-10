# 🛰️ Orbital-X (SpaceX Tracker)

**A high-performance Android engine for tracking the future of aerospace.**

Orbital-X is the ultimate tool for space enthusiasts, engineered to provide real-time telemetry and mission data for SpaceX launches and Starlink satellite constellations. Built with a focus on speed and data accuracy, it transforms raw orbital parameters into a stunning mobile experience.

---

## 🌌 Core Capabilities

- **Mission Telemetry**: Real-time launch countdowns, payload details, and landing target tracking using the **SpaceX (r-spacex) API**.
- **Starlink Live Map**: A reactive, canvas-drawn visualization of the Starlink constellation with live orbital positioning.
- **Launch History**: A comprehensive, searchable database of every SpaceX mission from Falcon 1 to Starship.
- **Notification Engine**: High-priority alerts for mission status changes, launch windows, and static fire tests.

---

## 🛠️ Engineering Details

- **API Layer**: Native integration with the **SpaceX REST API** and **NORAD TLE data** for satellite tracking.
- **Rendering**: Custom Canvas drawing for orbital path projections.
- **State Management**: Kotlin Coroutines and StateFlow for real-time data streaming without UI lag.

---

## 🚀 Installation

1.  **Clone**: `git clone https://github.com/TheGitCommitMan/Orbital-X-SpaceXTracker.git`
2.  **Import**: Open in Android Studio.
3.  **Sync**: Allow Gradle to resolve dependencies for NASA/SpaceX data bridges.
4.  **Run**: Deploy to an Android device with Play Services enabled.
