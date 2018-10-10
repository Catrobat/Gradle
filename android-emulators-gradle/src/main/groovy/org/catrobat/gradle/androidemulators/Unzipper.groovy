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
import groovy.transform.TypeCheckingMode
import org.apache.tools.ant.taskdefs.condition.Os

/**
 * Unfortunately the Java and Groovy unzip solutions do not keep file permissions on linux.
 * Thus call 'unzip' directly on Linux and Mac and use ant on Windows
 */
@TypeChecked
class Unzipper {
    static void unzip(File zipFile, File destinationDir) {
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            antUnzip(zipFile, destinationDir)
        } else {
            new CommandBuilder(new File('unzip')).addArguments([zipFile]).directory(destinationDir).execute()
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    private static void antUnzip(File zipFile, File destinationDir) {
        def ant = new AntBuilder()
        ant.unzip(src: zipFile, dest: destinationDir)
    }
}
