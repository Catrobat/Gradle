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
import org.gradle.api.InvalidUserDataException

/**
 * Handles installation of dependencies and setup of emulators.
 *
 * @note Installation of dependencies only happens for those statements above a call to
 *       shouldInstallEverythingAbove()
 *       The installation is performed directly in the configuration step.
 *       This is necessary since some gradle plugins fail to run if the NDK is not installed yet.
 *
 *       It might be that in the future this separate handling is not necessary anymore.
 *       To figure out if it is still necessary
 *       1) deactivate the installation via calling installEverythingAbove(false)
 *       2) remove the directory ANDROID_SDK_ROOT
 *       3) run gradle without a task
 *       If the build succeeds than the hack can be removed and installation of dependencies
 *       can become a regular task.
 *
 *       To avoid issues on Jenkins and slower performance locally make shouldInstallEverythingAbove
 *       dynamic:
 *          shouldInstallEverythingAbove(project.hasProperty('installSdk'))
 */
@TypeChecked
class EmulatorsPluginExtension {
    private boolean performInstallation = false
    boolean showWindow
    Map<String, EmulatorExtension> emulatorLookup = [:]
    Map<String, List<Closure>> emulatorTemplates = [:]
    private String defaultEmulator = ''
    DependenciesExtension dependencies = new DependenciesExtension()
    Installer installer = null

    EmulatorsPluginExtension(boolean showEmulatorWindowDefault) {
        this.showWindow = showEmulatorWindowDefault
    }

    void dependencies(@DelegatesTo(DependenciesExtension) Closure settings) {
        Utils.applySettings(settings, dependencies)
        if (performInstallation) {
            installDependencies()
        }
    }

    /**
     * The description of the emulator to create.
     * @param name The name the emulator should be referenced within gradle.
     *             This is also the name of the avd.
     * @param settings of the emulator
     * @return the created emulator extension
     * @example
     *  emulator 'android24', {
     *      avd {
     *          systemImage = 'system-images;android-24;default;x86_64'
     *          // ...
     *          // see AvdCreator for the possible parameters
     *      }
     *
     *      parameters {
     *          // see EmulatorStarter for the possible parameters
     *      }
     *  }
     */
    EmulatorExtension emulator(String name, @DelegatesTo(EmulatorExtension) Closure settings) {
        emulator(name, '', settings)
    }

    /**
     * Like emulator(String, Closure) only that a settings template can be specified.
     * @param name
     * @param templateName Name of the emulatorTemplate to use. When empty no template is used.
     * @param settings
     * @return the created emulator extension
     */
    EmulatorExtension emulator(String name, String templateName, @DelegatesTo(EmulatorExtension) Closure settings) {
        if (!name) {
            throw new InvalidUserDataException('The emulator needs a name!')
        }
        if (emulatorLookup.containsKey(name)) {
            throw new InvalidUserDataException("There is already an emulator named [$name]!")
        }

        emulatorTemplate(name, templateName, settings)
        def e = new EmulatorExtension()
        emulatorTemplates[name].each {
            Utils.applySettings(it, e)

        }

        if (!e.avdSettings || !e.emulatorParameters) {
            throw new InvalidUserDataException("Specify both an 'avd' and a 'parameters' block for [$name]!")
        }

        emulatorLookup[name] = e

        // install necessary images if required
        if (this.performInstallation) {
            installEmulator(e)
        }

        e
    }

    /**
     * Add a named settings template for the emulator.
     * This template can then be used in the emulator call.
     * As a result you can put common avd settings and emulator parameters into a template
     * instead of duplicating them.
     * @param name the template settings should be referenced with.
     * @param settings
     */
    void emulatorTemplate(String name, @DelegatesTo(EmulatorExtension) Closure settings) {
        emulatorTemplate(name, '', settings)
    }

    /**
     * Like emulatorTemplate(String, Closure) only that a settings template can be specified.
     * @param name
     * @param templateName Name of the emulatorTemplate to use. When empty no template is used.
     * @param settings
     * @return the create emulator extension
     */
    void emulatorTemplate(String name, String templateName, @DelegatesTo(EmulatorExtension) Closure settings) {
        if (!name) {
            throw new InvalidUserDataException('The template needs a name!')
        }
        if (name == templateName) {
            throw new InvalidUserDataException("Template [$name] cannot refer to itself!")
        }
        if (emulatorTemplates.containsKey(name)) {
            throw new InvalidUserDataException("There is already an emulator template named [$name]!")
        }
        if (templateName && !emulatorTemplates.containsKey(templateName)) {
            throw new InvalidUserDataException("Unkown emulator template named [$name]!")
        }

        def templates = emulatorTemplates.get(templateName)?.collect() ?: []
        templates.add(settings)
        emulatorTemplates[name] = templates as List<Closure>
    }

    void install(boolean performInstallation) {
        this.performInstallation = performInstallation
        if (this.performInstallation) {
            installDependencies()
            // If install = appears after emulators, retroactively install any emulators
            //  we've already defined.
            installEmulators()
        }
    }

    String getDefaultEmulator() {
        this.defaultEmulator
    }

    Installer getInstaller() {
        if(installer == null) {
            installer = new Installer()
        }
        return installer
    }

    void setDefaultEmulator(String emulator) {
        if (emulator && !(emulator in emulatorLookup)) {
            throw new InvalidUserDataException("Invalid default emulator [$emulator], available emulators: ${emulatorLookup.keySet()}")
        }

        this.defaultEmulator = emulator
    }

    private void installDependencies() {
        def installer = getInstaller()

        installer.writeLicenseFiles()
        installer.installSdk(dependencies.sdkSettings)
        installer.installNdk(dependencies.ndkSettings)
        installer.installPackages(dependencies.packages)
    }

    private void installEmulators() {
        emulatorLookup.forEach { k, v -> installEmulator(v as EmulatorExtension) }
    }

    private void installEmulator(EmulatorExtension emulator) {
        def installer = getInstaller()
        installer.writeLicenseFiles()
        installer.installImage(emulator.avdSettings.systemImage)
    }
}
