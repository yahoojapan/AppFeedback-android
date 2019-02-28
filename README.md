![](./assets/Logo.png)
[![](https://jitpack.io/v/yahoojapan/AppFeedback-android.svg)](https://jitpack.io/#yahoojapan/AppFeedback-android)


# AppFeedback
You can post feedback messages and screenshots to Slack from your Android app!

![](./assets/demo.png)

It is very useful for internal test!

## How to feedback

Introducing this SDK, a floating icon of feedback is displayed. Tapping it, a feedback dialog is displayed.

## Feature

- Show feedback button
- Two fingers long press to show feedback dialog
- Take a screenshot & Record screen

## Requirements

- **Minimum SDK Version** - requires a minimum SDK version of 21 or higher
- **Compile SDK Version** -  must be compiled against SDK version 27 or higher
- **Support Library Version** - uses support library version 27.

## Usage

### Integrate SDK

**Step 1**. Add the JitPack repository to your build file:

```
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```

**Step 2**. Add the dependency:


```
    dependencies {
        implementation 'com.github.taisukeh.AppFeedback-android:sdk:1.+'
    }
```

In order to remove sdk code from your release bundle, use `sdk_stub` module. `sdk_stub` has same I/F with empty implementation.

```
    dependencies {
        releaseImplementation 'com.github.taisukeh.AppFeedback-android:sdk_stub:1.+'
    }
```

### MainActivity

Call `AppFeedback.start` in the MainActivity.

```java
import jp.co.yahoo.appfeedback.core.AppFeedback;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
 
        // Start feedback SDK
        AppFeedback.start(this,
                          "<slack token>",
                          "<slack channel id>");
        );
    }
```
