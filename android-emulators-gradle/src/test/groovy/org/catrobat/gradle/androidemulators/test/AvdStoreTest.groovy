package org.catrobat.gradle.androidemulators.test

import groovy.transform.TypeChecked
import org.catrobat.gradle.androidemulators.AvdSettings
import org.catrobat.gradle.androidemulators.AvdStore
import org.junit.After;
import org.junit.Before
import org.junit.Rule;
import org.junit.Test
import org.junit.rules.TemporaryFolder;

@TypeChecked
class AvdStoreTest {
    AvdStore avdStore

    @Rule
    public TemporaryFolder avdFolder = new TemporaryFolder();

    @Before
    void setUp() {
        avdStore = new AvdStore(avdFolder.root)
    }

    private AvdSettings android24() {
        def settings = new AvdSettings()
        settings.systemImage = 'system-images;android-24;default;x86_64'
        settings.sdcardSizeMb = 200
        settings.hardwareProperties += ['hw.ramSize': 800, 'vm.heapSize': 128]
        settings.screenDensity = 'xhdpi'

        settings
    }

    @Test
    void emptyStoreContainsNoAvd() {
        assert !avdStore.contains('android24', android24())
        assert !avdStore.existingAvds.exists()
    }

    @Test
    void store() {
        avdStore.store('android24', android24(), { createAvd('android24') })
        assert avdStore.existingAvds.exists()
        assert avdStore.contains('android24', android24())
    }

    /**
     * Simulate the creation of AVDs.
     * The store itself only tracks the AVDs but does not create them.
     * Thus there is no avd directory nor ini file.
     * This function simulates that.
     */
    private void createAvd(String name) {
        new File(avdFolder.root, "${name}.ini").write('lorem ipsum')

        def avdDir = new File(avdFolder.root, "${name}.avd")
        avdDir.mkdir()

        new File(avdDir, 'config.ini').write('x=y')
    }

}
