/*
 * Copyright 2015 mellowtech.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mellowtech.core.util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * @author martins
 * 
 */
public class Platform {

	private static HashMap<String, String> env = null;
	//private static String libPath = null;
	//private static String exePath = null;

	/**
	 * 
	 */
	public Platform() {
		super();
	}

	// Default System properties:
	public static String getFileSeparator() {
		return getS("file.separator");
	}

	public static String getClassPath() {
		return getS("java.class.path");
	}

	public static String getClassVersion() {
		return getS("java.class.version");
	}

	public static Path getJavaHome() {
		return Paths.get(getS("java.home"));
	}

	public static String getJavaVendor() {
		return getS("java.vendor");
	}

	public static String getVendorURL() {
		return getS("java.vendor.url");
	}

	public static String getJavaVersion() {
		return getS("java.version");
	}

	public static String getLineSeparator() {
		return getS("line.separator");
	}

	public static String getOSArchiteture() {
		return getS("os.arch");
	}

	public static String getOSName() {
		return getS("os.name");
	}

	public static String getOSVersion() {
		return getS("os.version");
	}

	public static String getPathSeparator() {
		return getS("path.separator");
	}

	public static Path getWorkingDirectory() {
		return Paths.get(getS("user.dir"));
	}

	public static Path getUserHome() {
		return Paths.get(getS("user.home"));
	}

	public static String getUserName() {
		return getS("user.name");
	}

	// Additional useful properites:
	public static String getFileEncoding() {
		return getS("file.encoding");
	}

	public static String getCountryCode() {
		return getS("user.country");
	}

	public static String getLanguage() {
		return getS("user.language");
	}

	public static Path getTempDir() {
	  return Paths.get(getS("java.io.tmpdir"));
  }

	public static Path convertToDataDir(String name) {
		if (Platform.isWindows()) {
			return Paths.get(Platform.getE("APPDATA")).resolve(name);
		} else if (Platform.isMac()) {
			return Platform.getUserHome().resolve("Library").resolve(name);
		} else if (Platform.isNix())
			return Platform.getUserHome().resolve("." + name);
		else
			return Platform.getUserHome().resolve(name);
	}

	public static Path getUserDocumentFolder() {
		if (Platform.isWindows()) {
			return Platform.getUserHome().resolve("My Documents");
		} else if (Platform.isMac()) {
			return Platform.getUserHome().resolve("Documents");
		} else if (Platform.isNix())
			return Platform.getUserHome();
		else
			return Platform.getUserHome();

	}

	public static String getSystemProperty(String prop) {
		return getS(prop);
	}

	public static String getEnvironmentVariable(String var) {
		return getE(var);
	}

	// Environment properties:
	// all environment variable are converted to upper case
	public static Path getPath() {
		return Paths.get(getE("path"));
	}


	// Utility methods:
	// will search in path...
	public static String findExec(final String name, final boolean substring,
			final boolean ignoreCase) throws IOException {
		// this will search through the path
		Path path = getPath();
    final String[] toRet = new String[1];
    toRet[0] = null;
    for(int i = 0; i < path.getNameCount(); i++){
      Path p = path.getName(i);
      Files.walkFileTree(p, new SimpleFileVisitor<Path>(){
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          String str = file.toFile().getName();
          if(Platform.compareStrings(substring, ignoreCase, str, name)){
            toRet[0] = file.toFile().getAbsolutePath();
            return FileVisitResult.TERMINATE;
          }
          return FileVisitResult.CONTINUE;
        }
      });
      if(toRet[0] != null) break;
    }
    return toRet[0];
	}

	public static boolean containsExec(String name, boolean substring,
			boolean ignoreCase) throws IOException {
		String exec = findExec(name, substring, ignoreCase);
		return exec != null;
	}

	public String[] getApplicationForFile(String fileName, boolean deepCheck) {
		return null;
	}

	public String getApplicationForFil(String fileName, boolean deepCheck) {
		return null;
	}

	public static boolean isWindows() {
		String osName = Platform.getOSName();
		if (osName.toLowerCase().contains("windows"))
			return true;
		return false;
	}

	public static boolean isMac() {
		return Platform.getOSName().toLowerCase().startsWith("mac os");
	}

	public static boolean isLinux() {
		return Platform.getOSName().toLowerCase().contains("linux");
	}

	public static boolean isNix() {
		return !(Platform.isWindows() || Platform.isMac());
	}

	/**
	 * Tries to open the given file with the most appropriate application. In
	 * Windows the Start command will be used in MAC OS the open command will be
	 * used and for other systems the platform tries to find the given executable
	 * 
	 * @param fileName file to open
	 * @param executables possible programs to launch
	 * @throws Exception if error
	 */
	public static void openFile(String fileName, String[] executables)
			throws Exception {

		if (Platform.isWindows()) {

			String cmd = "cmd /C \"start " + fileName + "\"";

			Runtime.getRuntime().exec(cmd);
			return;
		} else if (Platform.isMac()) {
			String pathname = new String("\"" + fileName + "\"");
			
			String[] args = new String[2];
			args[0] = "/usr/bin/open";
			args[1] = pathname;
			Process p = Runtime.getRuntime().exec(args);
			return;
		} else if (executables != null) {
			String exec = null;
			for (int i = 0; i < executables.length; i++) {
				exec = Platform.findExec(executables[i], false, true);
				if (exec != null)
					break;
			}
			if (exec != null)
				Runtime.getRuntime().exec(exec + " " + fileName);
			return;
		}
		throw new Exception("Could not find executable");
	}

	private static boolean compareStrings(boolean substring,
			boolean ignoreCase, String str, String str1) {
		if (substring) {
			if (str.length() > str1.length())
				str = str.substring(str.length());
			if (ignoreCase) {
				return str.equalsIgnoreCase(str1);
			} else
				return str.equals(str1);
		}
		if (ignoreCase)
			return str.equalsIgnoreCase(str1);
		return str.equals(str1);

	}

	private static String getS(String property) {
		return System.getProperty(property);
	}

	private static String getE(String property) {
		if (env == null) {
			Map map = System.getenv();
			env = new HashMap<>();
			for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Map.Entry) iter.next();
				env.put(((String) entry.getKey()).toUpperCase(), (String) entry
						.getValue());
			}
		}
		return env.get(property.toUpperCase());
	}

}
