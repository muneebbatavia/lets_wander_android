# Let's Wander

An Android travel-guide app that pins historical landmarks on a map and automatically narrates each location's history when the user physically enters its geofence — turning exploration into a hands-free, audio-guided experience.

**Platform:** Android (Java) · **Maps:** Google Maps API · **Status:** Completed

## 📱 Overview

Let's Wander lets users discover landmarks through an interactive map. As the user enters the geofenced radius of a pinned location, the app automatically triggers a spoken description of that landmark's history — no manual interaction required while exploring.

> A companion admin app, [lets_wander_admin_android](https://github.com/muneebbatavia/lets_wander_admin_android), is used to manage and publish landmark data to the main app.

## ✨ Key Features

- **Interactive map with multiple pinned landmarks** using the Google Maps API
- **Geofence-triggered narration** — automatically detects when a user enters a landmark's radius and plays an audio description via text-to-speech
- **Manual pin details on tap** — tapping a landmark pin directly shows its name, location, and description on demand
- **Location accuracy tuning** — geofence logic refined to improve trigger accuracy
- **Admin-managed content** — landmark data (location, description, audio) is managed through a companion admin app rather than hardcoded

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Platform | Android (Java) |
| Maps & Location | Google Maps API, Geofencing API |
| Audio | Android Text-to-Speech (TTS) |
| Data | Firebase |

## ⚙️ How It Works

1. Landmarks (with GPS coordinates, description, and audio content) are published via the admin app.
2. The main app renders each landmark as a pin on Google Maps.
3. A geofence is registered around each landmark's coordinates.
4. When the user's device enters a geofence radius, the app triggers a broadcast that plays the landmark's narrated description via TTS.

## 🚀 Getting Started

```bash
git clone https://github.com/muneebbatavia/lets_wander_android.git
```

<!-- Add: Android Studio version, minSdk/targetSdk, Google Maps API key setup, Firebase config (google-services.json) -->

## 👤 Role

Designed and built the core Android app — map integration, geofence logic, and Firebase-backed landmark sync with the admin app.
