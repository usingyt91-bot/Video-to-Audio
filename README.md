# VideoToAudio

A lightweight, offline Android app that converts video files to MP3 audio using native Android APIs.

## Features

- 🚀 **Fast & Lightweight**: Minimal dependencies, quick conversion
- 📴 **Completely Offline**: No internet permission required
- 🎬 **Video Format Support**: Converts any video format supported by Android
- 🎵 **MP3 Output**: Saves audio as `converted_audio.mp3` to Downloads folder
- 📊 **Progress Tracking**: Visual progress bar during conversion
- 🛡️ **Clean UI**: Minimal, intuitive interface with one main action button

## Requirements

- Android 7.0+ (API 24)
- Target SDK 34
- Java 8+
- Gradle 8.2

## Building the APK

### Prerequisites

1. Install Android Studio (Hedgehog or later)
2. Ensure SDK 34 is installed

### Build Steps

#### Via Android Studio

1. Open Android Studio
2. Select `File` → `Open` and navigate to the project folder
3. Wait for Gradle sync to complete
4. Click `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
5. The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

#### Via Command Line

```bash
git clone https://github.com/usingyt91-bot/Video-to-Audio.git
cd Video-to-Audio
git checkout android-app-dev
./gradlew assembleDebug
```

The generated APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`

## How to Use

1. Install the APK on your Android device
2. Open the VideoToAudio app
3. Tap the **SELECT VIDEO** button
4. Choose a video file from your device
5. Wait for conversion to complete
6. The MP3 file will be saved to your Downloads folder as `converted_audio.mp3`
7. A toast notification will confirm completion

## Technical Details

### Audio Extraction

- Uses Android's native `MediaExtractor` and `MediaMuxer` APIs
- No external codec libraries required
- Automatically detects audio track in video file
- Handles videos with multiple audio tracks
- Gracefully handles videos with no audio track

### Permissions

- `READ_EXTERNAL_STORAGE`: Read video files
- `WRITE_EXTERNAL_STORAGE`: Save MP3 to Downloads
- `MANAGE_EXTERNAL_STORAGE`: (Android 11+) Full file system access

### Threading

- Conversion runs on background thread to prevent UI freezing
- UI updates are properly dispatched to main thread

### File Output

- **Location**: `Environment.DIRECTORY_DOWNLOADS`
- **Filename**: `converted_audio.mp3`
- **Format**: MPEG-4 audio container

## Dependencies

- AndroidX Core: `androidx.core:core-ktx:1.12.0`
- AndroidX AppCompat: `androidx.appcompat:appcompat:1.6.1`
- Material Components: `com.google.android.material:material:1.11.0`
- ConstraintLayout: `androidx.constraintlayout:constraintlayout:2.1.4`

## CI/CD

GitHub Actions workflow automatically builds the APK on every push and pull request to the `android-app-dev` and `main` branches. The APK is uploaded as an artifact for easy download.

## Project Structure

```
VideoToAudio/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/videotoaudio/
│   │   │   └── MainActivity.kt
│   │   ├── res/
│   │   │   ├── layout/activity_main.xml
│   │   │   ├── values/strings.xml
│   │   │   ├── values/colors.xml
│   │   │   ├── values/themes.xml
│   │   │   └── xml/
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── gradle/wrapper/
├── .github/workflows/
├── build.gradle
├── settings.gradle
├── gradle.properties
└── README.md
```

## Performance

- **Lightweight**: ~2 MB APK size
- **Memory Efficient**: Streams audio data
- **Fast**: Speed depends on video file size and codec
- **Battery Efficient**: No background services

## License

MIT License

## Troubleshooting

### Build Fails

1. Ensure JDK 17 is installed
2. Run `./gradlew clean` then rebuild
3. Check that Android SDK 34 is installed

### Conversion Fails

1. Ensure video file is not corrupted
2. Check that device has sufficient storage space
3. Verify file permissions are granted
4. Some video formats may not be supported by the device

### Permissions Not Granted (Android 11+)

1. Go to Settings → Apps → VideoToAudio → Permissions
2. Grant "All files access" permission
3. Restart the app
