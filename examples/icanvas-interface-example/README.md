# ICanvasplayer Interface Test Application

This app is an example of how to interact with the icanvasplayer application to do the following:

* Send Commands from the Demo API
* Receive command responses from the Demo API
* Create new demo commands and respond to them

## Setup:
* Add the following to the AndroidManifest.xml communicate with icanvasplayer:
```
 <uses-permission android:name="com.videri.permission.VSERVICE" />
 ```
* Add maven repo "https://gems.videri.com/artifactory/videri-core"
* Add the icanvasplayer lib as a dependency to your gradle file - you can find the latest version at: https://gems.videri.com/artifactory/webapp/#/artifacts/browse/tree/General/videri-core/com/videri/libs/icanvasplayer

## Features:
### VServiceConnection
The AIDL interface to iCanvasPlayer is made available through the `VServiceConnection` class. This interface is to be used for direct communication + fetching information.
* `getVersion` method is available to retrieve the current verison of icanvasplayer
* `getUuid` method returns the unique identifier that icanvasplayer associates this device with VLE - this is usually the device's serial number
* `sendCommand` method allows the user to send a command from the demo command API directly to iCanvasPlayer.  The list of commands available to a given application is restricted and the `getAllowedCommands` call can be used to determine which commands are available.  The response will be sent back with an `ACTION_DEMO_COMMAND_RESPONSE` intent
* `getAllowedCommands` returns the commands that are available to this application
* The `VServiceConnection.ConnectionListener` class is available to be notified of being connected / disconnected from this interface
### Command Registry
It is also possible to register a new command using the command registry feature.
* `CommandBroadcastReceiver.kt` is an example of how to receive commands and respond to them and can also be used to extend the functionality of existing commands.  All commands handled here need to be registered by metadata in the `AndroidManifest.xml` file.  Since this is done though a BroadcastReceiver, applications can respond to commands without needing to already be running.

The registry of the command in the AndroidManifest in this example is:
```
<meta-data
    android:name="com.videri.icanvasplayer.priv_commands"
    android:value="
        info,
        echo,
        help
    " />
```
This will end up registering the following 3 commands with the packagename + ":" prepended - so in this case:
* com.videri.testapp:info
* com.videri.testapp:echo
* com.videri.testapp:help

The main feature of the private command registry is that:
* extend the command API to include new custom commands intended for a particular app
* the commands parameters are not logged, so can be used to pass in secure information like API keys

Note - one can use the command `list_command_subscriptions` in the command terminal to determine if the new commands are successfully registered

## API Reference

For full details on the iCanvasPlayer Interface SDK — including `VServiceConnection`, `VServiceBroadcastDefinitions`, and all available constants — see the [iCanvasPlayer Interface SDK — API Reference](../../docs/ICANVAS-INTERFACE-API.md).