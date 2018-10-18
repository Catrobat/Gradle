/*
 *  Android Emulators Plugin: A gradle plugin to manage Android emulators.
 *  Copyright (C) 2018 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.gradle.androidemulators

import groovy.transform.TypeChecked

/**
 * Creates an avd with the given settings.
 *
 * The hardware properties are written to the avd config file.
 */
@TypeChecked
class AvdCreator {
    File sdkDirectory
    Map environment
    AvdStore avdStore

    AvdCreator(File sdkDirectory, Map environment) {
        this.sdkDirectory = sdkDirectory
        this.environment = environment
        this.avdStore = AvdStore.create(environment)
    }

    void createAvd(String avdName, AvdSettings settings) {
        checkSettings(settings)

        avdStore.store(avdName, settings) {
            def avdmanager = new CommandBuilder(Utils.joinPaths(sdkDirectory, 'tools', 'bin', 'avdmanager'), '.bat')

            avdmanager.addArguments(['create', 'avd', '-f', '-n', avdName])
            avdmanager.addOptionalArguments(settings.sdcardSizeMb, ['-c', "${settings.sdcardSizeMb}M"])
            avdmanager.addArguments(['-k', settings.systemImage])
            avdmanager.addArguments(settings.arguments)

            avdmanager.input('no\r\n').directory(avdStore.avdStore).environment(environment).verbose()
            avdmanager.execute()
        }
    }

    void reuseOrCreateAvd(String avdName, AvdSettings settings) {
        if (!avdStore.contains(avdName, settings)) {
            println("Create AVD")
            createAvd(avdName, settings)
        }
    }

    private void checkSettings(AvdSettings settings) {
        def throw_if_null = { name, value ->
            if (!value) {
                throw new IllegalStateException("Setting '$name' is not specified but needed by createAvd.")
            }
        }

        throw_if_null(settings.systemImage, 'systemImage')
        throw_if_null(settings.screenDensity, 'screenDensity')
    }
}
