# Videri Android App Development Documentation

This repository contains developer documentation for building Android apps for Videri signage devices.

## Platform Overview

- Videri signage devices run **Android 6.0 and newer**
- The Android WebView on the Videri OS is backed by **Chromium 101 or newer**
- App content can be scheduled by a hosted URL through the Videri CMS: [https://go.videri.com](https://go.videri.com)
- In addition to Android application content, the platform supports scheduling:
  - images
  - videos
  - web sites

## Device and Resolution Notes

Videri operates multiple device types with different display resolutions. The majority of deployed devices are using 1080p or
2160p in either portrait or landscape.  Typically the devices use a pixel density of 1.5 (240dpi) for HD displays and 3 (480dpi) for UHD.

Below is a partial list of supported Videri hardware:

| Model  | Resolution | Device Pixel Ratio |
|--------|------------|--------------------|
| Spark5 | 2160x3840  | 3                  |
| Spark4 | 2160x3840  | 3                  |
| Spark3 | 1080x1920  | 1.5                |
| Spark2 | 1080x1920  | 1.5                |
| SparkQ+ | 1920x1920  | 1.5                |
| Spark Bridge + | various (HDMI output) | 1.5                |

## Development Guidelines

- **Background resource usage**: Applications must not consume excessive CPU or other resources while in the background. The Videri platform runs multiple scheduled apps and the device must remain responsive at all times.
- **Storage**: Applications must not consume excessive device storage. Clean up temporary files and cached data that are no longer needed.
- **Permissions**: Declare only the permissions required for your application's functionality. Applications may be flagged for review based on their declared permissions.
- **Runtime permissions**: The Videri OS is a custom version of Android that removes system-level permission prompts. All runtime permissions are automatically granted at install time — no in-app permission request dialogs are needed or will appear.
- **Signing**: Sign your application with a production certificate before submission. All subsequent updates must be signed with the same certificate — mismatched signing certificates will cause updates to be rejected and installation to fail.
- **Version codes**: Every update to your application must increment the `versionCode` in your `build.gradle`. Uploads with a version code equal to or lower than the currently installed version will be ignored.

## Examples

| Example | Description |
|---------|-------------|
| [appvar-example](examples/appvar-example/README.md) | Demonstrates how to read per-schedule key/value variables passed via launch intents and handle app-level foreground/background lifecycle transitions using `ProcessLifecycleOwner`. |
| [icanvas-interface-example](examples/icanvas-interface-example/README.md) | Demonstrates how to communicate with the icanvasplayer application via the `VServiceConnection` AIDL interface — sending and receiving commands from the Demo API — and how to register custom private commands using the command registry. See also: [iCanvasPlayer Interface SDK — API Reference](docs/ICANVAS-INTERFACE-API.md) |