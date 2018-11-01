# Gradle
Repository for Gradle plugins used by the Catrobat Project.

Contains the following plugins:
* android-emulators-gradle allows to create, start, and stop emulators via gradle.
  The emulators themselves as well as their dependencies like Android SDK and Android NDK
  can be installed automatically.

## android-emulators-gradle Plugin
This plugin allows to manage Android emulators via gradle:
* Creating Android images.
* Starting an Android Emulator.
* Turning off animations.
* Stopping an emulator.
* Automatically retrieves the logcat file.
* Android SDK can be installed automatically.
* Android NDK can be installed automatically.
* The necessary Android images as well as the emulator can be installed automatically.
* Emulator templates avoid redundancies in your emulator specification.

### Gradle Commands
| Command | Description |
| --- | --- |
| `./gradlew startEmulator [-Pemulator=EMULATOR_NAME] [-PlogcatFile=LOGCAT_NAME]` | Creates the emulator if necessary, then starts it, and disables animations globally. If you configured multiple emulators you need to select which via `-Pemulator`. The logcat file is automatically stored as logcat.txt. |
| `./gradlew startEmulatorWithAnimations [-Pemulator=EMULATOR_NAME] [-PlogcatFile=LOGCAT_NAME]` | Like `startEmulator` but enables global animations. |
| `./gradlew stopEmulator` | Stops the first emulator it finds. Running multiple emulators at the same time is not supported. |
| `./gradlew adbDisableAnimationsGlobally` | Turns-off animations of the first running emulator it finds. |
| `./gradlew adbResetAnimationsGlobally` | Turns-on animations of the first running emulator it finds. |
| `./gradlew clearAvdStore` | Clears all AVDs in the AVD store, which can be useful to save space or to force their recreation. |
| `./gradlew listEmulators` | Lists all emulators that can be started via startEmulator. |

### Basic Emulator Managment

Place the following lines in your build.gradle file.
```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'org.catrobat.gradle.androidemulators:android-emulators-gradle:1.2.0'
    }
}

// Place this at the very top.
// This ensures that the dependencies (if you install them) are present
// when other plugins try to access them.
emulators {
    // Whether to install the dependencies or not.
    // The dependencies are installed during the configuration step.
    // As a result you should not always install them, to not slow down configuration.
    // The install function takes a boolean whether to install the dependencies or not.
    install project.hasProperty('installSdk')

    dependencies {
        sdk() // install the most recent Android SDK known by the plugin.
    }

    // Name the emulator you want to create, here it is called android24
    emulator 'android24', {
        avd {
            systemImage = 'system-images;android-24;default;x86_64'
            sdcardSizeMb = 200
            hardwareProperties += ['hw.ramSize': 800, 'vm.heapSize': 128]
            screenDensity = 'xhdpi' // the plugin automatically maps xhdpi to the correct screen density
        }

        // Paramters that are used to start the emulator.
        // Some sensible defaults are provided automatically, see EmulatorStarter.groovy
        parameters {
            resolution = '768x1280'
            language = 'en'
            country = 'US'
        }
    }
}
```

Calling `./gradlew -PinstallSdk` in your project will install the dependencies automatically.
Afterwards you can create and start the emulator with `./gradlew startEmulator adbDisableAnimationsGlobally`
with animations disabled.
Finally you can stop the emulator with `./gradlew stopEmulator`.

### Multiple Emulators and Templates
When you need multiple emulators you often and up with a lot duplication between their configuration.
Templates can help here.

```
emulators {
    install project.hasProperty('installSdk')

    dependencies {
        sdk()
    }

    // Specify a template like any other emulator.
    emulatorTemplate 'englishTemplate', {
        avd {
            sdcardSizeMb = 200
            hardwareProperties += ['hw.ramSize': 800, 'vm.heapSize': 128]
            screenDensity = 'xhdpi'
        }

        parameters {
            resolution = '768x1280'
            language = 'en'
            country = 'US'
        }
    }

    // Now reference the template as second parameter.
    emulator 'android24', 'englishTemplate', {
        // You only need to provide what you want to override.
        avd {
            systemImage = 'system-images;android-24;default;x86_64'
        }
    }

    emulator 'android25', 'englishTemplate', {
        avd {
            systemImage = 'system-images;android-25;default;x86_64'
        }
    }
}
```

When you have configured multiple emulators you need to specify which emulator to start, for example,
`./gradlew -Pemulator=android24 startEmulator`.

### Known Shortcomings
* Only one emulator can be started at the same time.
