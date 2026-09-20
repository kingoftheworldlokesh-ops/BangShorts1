# BangShorts

BangShorts is a simple Android prototype designed to stop the YouTube Shorts feed from appearing in the active YouTube app screen.

What it does
- Provides a small dashboard with an enable switch.
- Opens Android Accessibility settings to let the app monitor the active screen.
- Uses an Accessibility Service to detect text containing "Shorts" and immediately sends the user back.

Important notes
- This is a prototype for learning and personal use.
- Android accessibility controls are restricted by OS behavior and app policies.
- YouTube can change its layout and text or block some automated behavior over time.

Project setup
1. Open in Android Studio.
2. Let Gradle sync.
3. Connect a device or emulator.
4. Enable BangShorts in Settings > Accessibility.

This app is for educational use and should be treated as a prototype rather than a production guarantee against all YouTube Shorts behavior.
