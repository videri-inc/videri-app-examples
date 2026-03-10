# AppVar Example

This is an example of how to write a simple Android application that can be scheduled and displayed on the Videri's Android-based digital signage platform. It demonstrates two key integration points: reading schedule variables passed via launch intents and reacting to app foreground/background lifecycle transitions.

## Overview

Applications running on the Videri signage platform are ingested, scheduled and launched via the Videri portal. The scheduling of the application can include a per-event configuration to your app as key/value variables, and it controls exactly when your app is brought to the foreground or sent to the background as part of a content scheduling.

## Reading Variables via the Launch Intent

When the Videri platform launches or resumes your app as part of a schedule, it passes any configured variables as extras on the launch `Intent`. Your app reads these in `onResume()` by calling `getIntent().getExtras()`:

```java
@Override
protected void onResume() {
    super.onResume();
    Intent intent = getIntent();
    if (intent != null) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                String value = "" + extras.get(key);
                // use key/value pair
            }
        }
    }
}
```

Because the platform may deliver updated variables to an already-running app, you must also override `onNewIntent()` to replace the stored intent — otherwise `getIntent()` will return the original (stale) launch intent:

```java
@Override
protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent); // ensures onResume() sees the latest intent
}
```

## Handling Foreground / Background Lifecycle with ProcessLifecycleOwner

When the Videri scheduler transitions between scheduled content, your app may be moved to the background and later returned to the foreground without being destroyed. Standard `Activity` lifecycle callbacks (`onPause`/`onResume`) fire for many reasons (e.g. dialogs, screen rotation), so they are not a reliable signal for true app-level background/foreground transitions.

The correct approach is to use `ProcessLifecycleOwner` from the AndroidX Lifecycle library, which observes the lifecycle of the entire app process rather than a single Activity:

```java
ProcessLifecycleOwner.get().getLifecycle().addObserver(lifecycleEventObserver);
```

`ON_PAUSE` fires only when the entire app moves to the background (no activities are visible), and `ON_RESUME` fires when the app returns to the foreground. This makes them reliable hooks for releasing or re-acquiring resources tied to display time — for example, pausing video playback, stopping animations, or logging active screen time.

```java
private final LifecycleEventObserver lifecycleEventObserver = new LifecycleEventObserver() {
    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        switch (event) {
            case ON_RESUME:
                onAppForegrounded();
                break;
            case ON_PAUSE:
                onAppBackgrounded();
                break;
        }
    }
};
```

## Dependencies

The `ProcessLifecycleOwner` API requires the following dependency in your `build.gradle`:

```groovy
implementation 'androidx.lifecycle:lifecycle-process:<version>'
```
