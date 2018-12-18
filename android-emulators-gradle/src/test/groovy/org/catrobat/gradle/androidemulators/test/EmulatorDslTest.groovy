package org.catrobat.gradle.androidemulators.test

import groovy.transform.TypeChecked
import org.catrobat.gradle.androidemulators.EmulatorsPluginExtension
import org.gradle.api.InvalidUserDataException
import org.junit.Test

@TypeChecked
class EmulatorDslTest {

    @Test(expected = InvalidUserDataException)
    void avdAndParametersNeedToBeSpecified() {
        def emulators = new EmulatorsPluginExtension(false)
        emulators.emulator('invalid', {})
    }

    @Test
    void emulator() {
        def emulators = new EmulatorsPluginExtension(false)
        def emulator = emulators.emulator('emulator1', {
            avd {
                systemImage = 'someImage'
                screenDensity = 'xhdpi'

            }
            parameters {
                language = 'en'
                country = 'US'
            }
        })

        assert emulator
        assert emulator.avdSettings.systemImage == 'someImage'
        assert emulator.avdSettings.screenDensity == '320'
        assert emulator.emulatorParameters.language == 'en'
        assert emulator.emulatorParameters.country == 'US'
    }

    @Test(expected = InvalidUserDataException)
    void emulatorNeedsName() {
        def emulators = new EmulatorsPluginExtension(false)
        emulators.emulator('', {
            avd {
                systemImage = 'someImage'
                screenDensity = 'xhdpi'

            }
            parameters {
                language = 'en'
                country = 'US'
            }
        })
    }

    @Test
    void emulatorBasedOnTemplate() {
        def emulators = new EmulatorsPluginExtension(false)
        emulators.emulatorTemplate('template1', {
            avd {
                systemImage = 'someImage'
                screenDensity = 'xhdpi'

            }
            parameters {
                language = 'en'
                country = 'US'
            }
        })

        def emulator = emulators.emulator('emulator1', 'template1', {
            avd {
                systemImage = 'anotherImage'
            }
            parameters {
                country = 'UK'
            }
        })

        assert emulator
        assert emulator.avdSettings.systemImage == 'anotherImage'
        assert emulator.avdSettings.screenDensity == '320'
        assert emulator.emulatorParameters.language == 'en'
        assert emulator.emulatorParameters.country == 'UK'
    }

    @Test(expected = InvalidUserDataException)
    void needsNoneEmptyNameOfTemplate() {
        def emulators = new EmulatorsPluginExtension(false)
        emulators.emulatorTemplate('', {
            avd {
            }
            parameters {
            }
        })
    }

    @Test(expected = InvalidUserDataException)
    void templateCannotBeBasedOnItself() {
        def emulators = new EmulatorsPluginExtension(false)
        emulators.emulatorTemplate('template1', 'template1', {
            avd {
            }
            parameters {
            }
        })
    }

    @Test(expected = InvalidUserDataException)
    void needsValidNameOfTemplate() {
        def emulators = new EmulatorsPluginExtension(false)
        emulators.emulatorTemplate('template1', 'notExisting', {
            avd {
            }
            parameters {
            }
        })
    }

    @Test
    void emulatorBasedOnTemplateBasedOnTemplate() {
        def emulators = new EmulatorsPluginExtension(false)
        emulators.emulatorTemplate('template1', {
            avd {
                systemImage = 'someImage'
                screenDensity = 'xhdpi'

            }
            parameters {
                language = 'en'
                country = 'US'
            }
        })
        emulators.emulatorTemplate('template2', 'template1', {
            avd {
                systemImage = 'anotherImage'
            }
            parameters {
                country = 'UK'
            }
        })

        def emulator = emulators.emulator('emulator1', 'template2', {})

        assert emulator
        assert emulator.avdSettings.systemImage == 'anotherImage'
        assert emulator.avdSettings.screenDensity == '320'
        assert emulator.emulatorParameters.language == 'en'
        assert emulator.emulatorParameters.country == 'UK'
    }

    @Test
    void emulatorBasedOnAnotherEmulator() {
        def emulators = new EmulatorsPluginExtension(false)
        emulators.emulator('emulator1', {
            avd {
                systemImage = 'someImage'
                screenDensity = 'xhdpi'

            }
            parameters {
                language = 'en'
                country = 'US'
            }
        })

        def emulator = emulators.emulator('emulator2', 'emulator1', {
            avd {
                systemImage = 'anotherImage'
            }
            parameters {
                country = 'UK'
            }
        })

        assert emulator
        assert emulator.avdSettings.systemImage == 'anotherImage'
        assert emulator.avdSettings.screenDensity == '320'
        assert emulator.emulatorParameters.language == 'en'
        assert emulator.emulatorParameters.country == 'UK'
    }
}
