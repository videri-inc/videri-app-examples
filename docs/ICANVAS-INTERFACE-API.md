# iCanvasPlayer Interface SDK — API Reference

**Package:** `com.videri.libs:icanvasplayer`  
**Latest release:** can be found in the <a href=https://gems.videri.com/artifactory/videri-core/com/videri/libs/icanvasplayer>Videri Artifact Repository</a>  
**Permission required:** `com.videri.icanvasplayer.vservice.VSERVICE_PERMISSION`

---

## Table of Contents

1. [Setup](#setup)
2. [VServiceConnection](#vserviceconnection)
3. [VServiceConnection.ConnectionListener](#vserviceconnectionconnectionlistener)
4. [VServiceConnection.CmdStatus](#vserviceconnectioncmdstatus)
5. [VServiceBroadcastDefinitions](#vservicebroadcastdefinitions)
6. [ConnectionState](#connectionstate)
7. [Setting](#setting)
8. [RenderingMode](#renderingmode)
9. [ScaleType](#scaletype)
10. [ErrorCodes](#errorcodes)
11. [BaseVideriApi](#basevideriapi)
12. [IVService (AIDL)](#ivservice-aidl)

---

## Setup

### Gradle dependency

Add the Videri Maven repository and the library dependency to your `build.gradle`:

```groovy
repositories {
    maven {
        url "https://gems.videri.com/artifactory/videri-core"
    }
}

dependencies {
    implementation 'com.videri.libs:icanvasplayer:6.3.0'
}
```

### Manifest permission

The SDK requires the VService permission to be declared in `AndroidManifest.xml`:

```xml
<uses-permission android:name="com.videri.icanvasplayer.vservice.VSERVICE_PERMISSION" />
```

---

## VServiceConnection

```
public class VServiceConnection
```

**Package:** `com.videri.lib.icanvasplayer`

Wrapper class around an AIDL interface to the `VService` background service from the iCanvasPlayer APK. This is the primary entry point for most integrations.

### Nested types

| Type | Description |
|------|-------------|
| `VServiceConnection.CmdStatus` | Integer constants representing possible return values from `sendCommand()` |
| `VServiceConnection.ConnectionListener` | Callback interface for binder connection state changes |

### Methods

---

#### `setConnectionListener`

```java
void setConnectionListener(@Nullable VServiceConnection.ConnectionListener listener)
```

Registers a listener to be notified when the connection to VService changes state. Pass `null` to remove the listener.

| Parameter | Type | Description |
|-----------|------|-------------|
| `listener` | `VServiceConnection.ConnectionListener` | Listener to use, or `null` to unregister |

---

#### `connect`

```java
boolean connect(@NonNull Context context)
```

Binds to the VService from the iCanvasPlayer APK and sets up the required broadcast receivers. Call this from `Activity.onStart()` or `Service.onCreate()`.

| Parameter | Type | Description |
|-----------|------|-------------|
| `context` | `Context` | Context to bind with |

**Returns:** `true` if the bind was successfully initiated.

---

#### `disconnect`

```java
void disconnect()
```

Unbinds from the VService and unregisters broadcast receivers. Call this from `Activity.onStop()` or `Service.onDestroy()` to avoid leaks.

---

#### `isBound`

```java
boolean isBound()
```

**Returns:** `true` if currently bound to the VService.

---

#### `getVersion`

```java
@Nullable String getVersion()
```

**Returns:** The version string of the running iCanvasPlayer APK, or `null` if not bound.

---

#### `getUid`

```java
@Nullable String getUid()
```

**Returns:** The unique identifier that iCanvasPlayer associates with this device on the VLE backend (typically the device serial number), or `null` if not bound.

---

#### `sendCommand`

```java
int sendCommand(@NonNull Context context, @NonNull String command, @NonNull String msgUuid)
```

Sends a command to iCanvasPlayer via the demo command API. The response is delivered asynchronously as a targeted `Intent` with action `VServiceBroadcastDefinitions.ACTION_DEMO_COMMAND_RESPONSE`.

| Parameter | Type | Description |
|-----------|------|-------------|
| `context` | `Context` | Context of the calling application |
| `command` | `String` | Command name — see `getAllowedCommands()` |
| `msgUuid` | `String` | Unique message identifier used to correlate the response |

**Returns:** A `VServiceConnection.CmdStatus` integer constant.

---

#### `getAllowedCommands`

```java
@Nullable List<String> getAllowedCommands(@NonNull Context context)
```

Returns the list of commands that this package is permitted to send through `sendCommand()`.

| Parameter | Type | Description |
|-----------|------|-------------|
| `context` | `Context` | App context |

**Returns:** List of allowed command name strings, or `null` if not bound.

---

## VServiceConnection.ConnectionListener

```
public interface VServiceConnection.ConnectionListener
```

Callback interface for monitoring VService binder connection state.

### Methods

| Method | Description |
|--------|-------------|
| `abstract void onConnected()` | Called when a valid connection to VService is established |
| `abstract void onDisconnected()` | Called when the connection to VService is lost |

### Example

```kotlin
val connection = VServiceConnection()

connection.setConnectionListener(object : VServiceConnection.ConnectionListener {
    override fun onConnected() {
        // Safe to call getVersion(), getUid(), sendCommand(), etc.
    }
    override fun onDisconnected() {
        // Handle loss of connection; do not call service methods
    }
})

override fun onStart() {
    super.onStart()
    connection.connect(this)
}

override fun onStop() {
    super.onStop()
    connection.disconnect()
}
```

---

## VServiceConnection.CmdStatus

```
public abstract class VServiceConnection.CmdStatus
```

Integer constants returned by `VServiceConnection.sendCommand()`.

| Constant | Type | Description |
|----------|------|-------------|
| `SUCCESSFULLY_SENT` | `int` | Command was accepted and dispatched to iCanvasPlayer |
| `NOT_BOUND` | `int` | `VServiceConnection` is not currently bound — call `connect()` first |
| `NOT_SUPPORTED` | `int` | The command is not in the list of allowed commands for this package |
| `NOT_AUTHORIZED` | `int` | The calling package does not have the required permission |
| `INTERNAL_ERROR` | `int` | An unexpected error occurred inside iCanvasPlayer |

---

## VServiceBroadcastDefinitions

```
public class VServiceBroadcastDefinitions
```

**Package:** `com.videri.lib.icanvasplayer`

String constants for Intent actions and extras used in broadcasts between iCanvasPlayer and third-party apps.

### Permissions

| Constant | Description |
|----------|-------------|
| `VSERVICE_PERMISSION` | Required permission for sending and receiving VService intents |
| `VSERVICE_PERMISSION_PRIVILEGED` | Elevated permission for privileged operations (not for 3rd party use) |

### Service actions

| Constant | Description |
|----------|-------------|
| `PACKAGE_NAME` | iCanvasPlayer package name |
| `MAIN_SERVICE_NAME` | Fully-qualified name of the main VService |
| `ACTION_START_SERVICE` | Intent action to start the VService |
| `BROADCAST_ACTION_MAIN_SERVICE_STARTED` | Broadcast when the main service has started |
| `ACTION_CONNECTION_STATE_CHANGED` | Broadcast when the cloud connection state changes |
| `ACTION_SHOW_APP` | Sent by iCanvasPlayer to bring a scheduled app to the foreground |
| `ACTION_LOGO_UPDATED` | Broadcast when the device logo has been updated |
| `ACTION_MY_PACKAGE_ADDED` | Broadcast when this package is first installed or updated |

### Settings

| Constant | Description |
|----------|-------------|
| `BROADCAST_ACTION_SETTING_CHANGED` | Broadcast when a setting value changes |
| `COMMAND_METADATA_KEY` | Metadata key for declaring registered commands in the manifest |
| `PRIVATE_COMMAND_METADATA_KEY` | Metadata key for private (unlogged) commands |

### Demo command actions and extras

| Constant | Description |
|----------|-------------|
| `ACTION_DEMO_COMMAND` | Action for sending a demo command to iCanvasPlayer |
| `ACTION_DEMO_COMMAND_RESPONSE` | Action for receiving the result of a demo command |
| `EXTRA_COMMAND` | Intent extra: command name string |
| `EXTRA_MESSAGE` | Intent extra: command response message |
| `EXTRA_UUID` | Intent extra: unique message identifier |
| `EXTRA_SUCCESS` | Intent extra: boolean success flag |
| `EXTRA_PACKAGE_NAME` | Intent extra: calling package name |

### XMPP message extras

| Constant | Description |
|----------|-------------|
| `BROADCAST_ACTION_RECIEVE_MESSAGE` | Broadcast when an XMPP message is received |
| `BROADCAST_ARG_MSG` | Extra: raw message body |
| `BROADCAST_EXTRA_MSG_COMMAND` | Extra: parsed command name |
| `BROADCAST_EXTRA_MSG_PARAMS` | Extra: command parameters |
| `BROADCAST_EXTRA_MSG_JSON` | Extra: full message as JSON |
| `BROADCAST_EXTRA_MSG_FROM` | Extra: sender JID |
| `BROADCAST_EXTRA_MSG_UUID` | Extra: message UUID |

### Asset download actions and extras

| Constant | Description |
|----------|-------------|
| `ACTION_ASSET_DOWNLOAD_STARTED` | Broadcast when an asset download begins |
| `ACTION_ASSET_DOWNLOAD_COMPLETED` | Broadcast when an asset download finishes successfully |
| `ACTION_ASSET_DOWNLOAD_FAILED` | Broadcast when an asset download fails |
| `BROADCAST_EXTRA_DOWNLOAD_ID` | Extra: download job identifier |
| `BROADCAST_EXTRA_DOWNLOAD_ASSET_ID` | Extra: asset identifier |
| `BROADCAST_EXTRA_DOWNLOAD_URL` | Extra: source URL |
| `BROADCAST_EXTRA_DOWNLOAD_FILE_PATH` | Extra: local destination file path |
| `BROADCAST_EXTRA_DOWNLOAD_TOTAL_BYTES_TO_TRANSFER` | Extra: total expected bytes |
| `BROADCAST_EXTRA_DOWNLOAD_BYTES_TRANSFERRED` | Extra: bytes transferred so far |
| `BROADCAST_EXTRA_DOWNLOAD_ERROR_MESSAGE` | Extra: human-readable error description |
| `BROADCAST_EXTRA_DOWNLOAD_FROM_ID` | Extra: sender identifier |
| `BROADCAST_EXTRA_DOWNLOAD_MSG_ID` | Extra: originating message identifier |

### Connection state extras

| Constant | Description |
|----------|-------------|
| `EXTRA_NEW_STATE` | Extra in `ACTION_CONNECTION_STATE_CHANGED`: new `ConnectionState` name |
| `EXTRA_CONNECTION_ERROR` | Extra in `ACTION_CONNECTION_STATE_CHANGED`: error string if applicable |
| `BROADCAST_ARG_ORIENTATION_CHANGE` | Extra: orientation change event |
| `BROADCAST_ACTION_CONFIG_CHANGE` | Broadcast when configuration changes |

### Helper methods

```java
static Intent getServiceIntent()
```
Returns an `Intent` configured to start the iCanvasPlayer VService.

```java
static Intent getDemoCommandIntent(Context context, String command, String uuid)
```
Builds a correctly formed demo command `Intent` ready to send to iCanvasPlayer.

```java
static Intent getDemoResponseIntent(String message, String respondToId, String uuid, boolean success)
static Intent getDemoResponseIntent(Intent receivedIntent, String message, boolean success)
```
Builds a demo command response `Intent`. Requires `VSERVICE_PERMISSION`. The second overload extracts the `from` and `uuid` values automatically from the original received command `Intent`.

---

## ConnectionState

```
public enum ConnectionState
```

**Package:** `com.videri.lib.icanvasplayer`

Represents the cloud connectivity state of the device.

| Constant | Description |
|----------|-------------|
| `DISCONNECTED` | No connection to the Videri cloud |
| `CONNECTING` | Connection attempt in progress |
| `CONNECTED` | Active connection established |
| `RECONNECTING` | Lost connection; attempting to reconnect |

The current state name is delivered in broadcast `ACTION_CONNECTION_STATE_CHANGED` via extra `EXTRA_NEW_STATE`.

---

## ErrorCodes

```
public class ErrorCodes
```

**Package:** `com.videri.lib.icanvasplayer`

Integer error code constants.

| Constant | Value | Description |
|----------|-------|-------------|
| `PARSING_ERROR` | `int` | Failed to parse a response or payload |
| `UI_THREAD_ERROR` | `int` | An operation was attempted on the wrong thread |
| `NOT_PROVISIONED` | `int` | Device is not provisioned with the VLE backend |
| `NOT_AUTHORIZED` | `int` | Calling package lacks the required permission |
| `UNKNOWN` | `int` | An unclassified error occurred |

---

## BaseVideriApi

```
public class BaseVideriApi
```

**Package:** `com.videri.lib.icanvasplayer`

Static helper methods for building standardised JSON response objects, and nested enum types used across the API.

### Nested enums

| Type | Description |
|------|-------------|
| `BaseVideriApi.AlertCategory` | Category values for `AdSyncVServiceConnection.sendAlert()` |
| `BaseVideriApi.AlertSeverity` | Severity levels: low (0), medium (1), high (2) |
| `BaseVideriApi.ErrorType` | Error type classifications for error responses |
| `BaseVideriApi.StatusCode` | HTTP-style status code constants |

### Static methods

```java
static JSONObject getStandardSuccessResponse(String name, String msgUuid, String description)
```
Builds a JSON object conforming to the standard Videri success response schema.

```java
static JSONObject getStandardErrorResponse(String name, String msgUuid, String description)
static JSONObject getStandardErrorResponse(String name, String msgUuid, String description, BaseVideriApi.ErrorType errorType)
```
Builds a JSON object conforming to the standard Videri error response schema. The second overload includes a typed error classification.

| Parameter | Description |
|-----------|-------------|
| `name` | Command or operation name |
| `msgUuid` | UUID of the originating message |
| `description` | Human-readable result description |
| `errorType` | Optional error type classification |

---

*Reference: icanvasplayer Javadoc JAR, version 6.1.38 — `com.videri.libs:icanvasplayer`*
