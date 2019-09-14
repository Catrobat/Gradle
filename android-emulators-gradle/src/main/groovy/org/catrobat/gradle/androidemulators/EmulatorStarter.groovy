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

@TypeChecked
class EmulatorStarter {
    String skin = ''
    String language = ''
    String country = ''
    boolean keepUserData = false
    List<String> additionalParameters = ['-gpu', 'swiftshader_indirect', '-no-boot-anim', '-noaudio', '-no-snapshot-save']

    String getResolution() {
        skin
    }

    void setResolution(String resolution) {
        skin = resolution
    }

    /**
     * Starts the emulator asynchronously without checking for success.
     * @return EmulatorStarter process
     */
    Process start(String avdName, File sdkDirectory, Map environment, boolean showWindow, File logcat) {
        def emulator = new CommandBuilder(Utils.joinPaths(sdkDirectory, 'emulator', 'emulator'), '.exe')

        emulator.addArguments(['-avd', avdName])
        emulator.addOptionalArguments(skin, ['-skin', skin])
        emulator.addOptionalArguments(language, ['-prop', "persist.sys.language=$language"])
        emulator.addOptionalArguments(country, ['-prop', "persist.sys.country=$country"])
        emulator.addOptionalArguments(!showWindow, ['-no-window'])
        emulator.addOptionalArguments(!keepUserData, ['-wipe-data'])
        emulator.addOptionalArguments(logcat, ['-logcat', '*:e', '-logcat-output', logcat.absolutePath])
        emulator.addArguments(additionalParameters)

        emulator.environment(environment).verbose().executeAsynchronously()
    }
}
