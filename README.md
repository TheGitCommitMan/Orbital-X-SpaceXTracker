# Orbital-X SpaceX Tracker

Orbital-X is a high-performance Android application engineered for space enthusiasts and researchers to track SpaceX missions and Starlink satellite constellations. Utilizing real-time orbital data, the app provides a comprehensive overview of launch schedules, mission histories, and live satellite positioning.

The project emphasizes a clean, reactive UI and seamless integration with celestial data streams to deliver a premium tracking experience.

## 🛰️ Development Setup

Ensure your local environment is configured to build and deploy the Orbital-X application.

### Prerequisites

- [Android Studio](https://developer.android.com/studio)
- Active internet connection for satellite data synchronization

### Local Execution

1. **Project Import**
   Open Android Studio and choose **Open** to import the Orbital-X source code.

2. **Dependency Resolution**
   During the initial import, Android Studio will automatically resolve Gradle dependencies. Ensure this process completes without errors to maintain build integrity.

3. **Security & API Configuration**
   The tracking engine utilizes AI-enhanced data parsing.
   - Create a `.env` file in the project root.
   - Set the following variable: `GEMINI_API_KEY=your_api_key`
   - Use `.env.example` as a template for your configuration.

4. **Gradle Adjustments**
   For local testing, verify your `app/build.gradle.kts` does not require external signing certificates. You may need to comment out or remove specific `signingConfig` lines to use the default debug key.

5. **Run the Application**
   Deploy the build to an Android Emulator or a physical device via the **Run** command in Android Studio.
