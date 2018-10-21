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

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.TypeChecked

@TypeChecked
class AvdStore {
    File avdStore
    File existingAvds

    AvdStore(File avdStore) {
        this.avdStore = avdStore
        this.existingAvds = new File(avdStore, 'avdstore.json')
    }

    static AvdStore create(Map environment) {
        String avd_home = environment['ANDROID_AVD_HOME']
        if (!avd_home) {
            throw new IllegalStateException("The environment does not contain an ANDROID_AVD_HOME.")
        }

        new AvdStore(new File(avd_home))
    }

    /**
     * Stores the AVD that is created by calling avdCreation() with name.
     *
     * The config.ini of the AVD is automatically updated according to the handed-in settings.
     */
    void store(String name, AvdSettings settings, Closure avdCreation) {
        // first storing the AVD
        println("Storing avd [$name]")
        avdDir(name).deleteDir()
        avdIni(name).delete()

        Map avds = readExistingAvds()
        avds[name] = Utils.asMap(settings)
        existingAvds.delete()
        existingAvds << JsonOutput.toJson(avds)

        // second creating the AVD
        avdCreation()

        // third updating the config.ini
        def avdConfigFile = new IniFile(Utils.joinPaths(avdStore, name + '.avd', 'config.ini'))
        avdConfigFile.updateValues(settings.hardwareProperties)
    }

    boolean contains(String name, AvdSettings settings) {
        def avds = readExistingAvds()
        if (avds[name] != Utils.asMap(settings)) {
            return false
        }

        avdDir(name).exists() && avdIni(name).exists()
    }

    List<String> emulators() {
        def emulators = readExistingAvds().keySet() as List<String>
        emulators.sort()
    }

    /**
     * Removes all AVDs in the store but keeps all untracked files.
     */
    void clear() {
        readExistingAvds().keySet().each {
            String name = (String) it
            avdDir(name).deleteDir()
            avdIni(name).delete()
        }
        existingAvds.delete()
    }

    private Map readExistingAvds() {
        if (!existingAvds.exists() || !existingAvds.canRead()) {
            return [:]
        }

        new JsonSlurper().parseText(existingAvds.text) as Map ?: [:]
    }

    private File avdDir(String name) {
        new File(avdStore, "${name}.avd")
    }

    private File avdIni(String name) {
        new File(avdStore, "${name}.ini")
    }
}
