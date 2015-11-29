/*
 * Copyright (c) 2013 mellowtech.org.
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 *
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 *
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 */
package org.mellowtech.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.zip.ZipEntry;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.mellowtech.core.CoreLog;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Loads a set of plug-ins in a generic fashion. PluginSet represents a minimum
 * effort approach to handle plugins of various types.
 * <p>
 * A plug-in is a
 * named class in a jar file. The plug-in must have a class who is a descendent
 * of a named class - this name is given to the PluginSet at instantiation in
 * the constructor.
 * <p>
 * Typical use would be to load classes/jars from a
 * directory and then get an ArrayList of plugin objects.
 * <p>
 * For each jar file that contains a plugin there must be an xml file that specifies which
 * class or classes that are plugins. The xml file must be in the META-INF
 * directory inside the jar file and it must be named PLUGIN.XML (all capitals).
 * Format for PLUGIN.XML that defines one class "PluginTestImplementation":
 * several classes may be separated by space, newline or commas.
 * <blockquote>
 * <pre>
 *  &lt;Plugin&gt;
 *   &lt;Classes&gt;
 *   PluginTestImplementation
 *   &lt;/Classes&gt;
 *  &lt;/Plugin&gt;
 * </pre>
 * </blockquote>
 * 
 * <blockquote>
 * <pre>
 *  // Sample code &lt;break&gt;
 *  PluginSet ps = new PluginSet(&quot;IPlugin&quot;); &lt;break&gt;
 *  ps.loadFromDirectory(root); &lt;break&gt;
 *  ArrayList objs = ps.getPlugins(); &lt;break&gt;
 * </pre>
 * 
 * </blockquote>
 * 
 */
@Deprecated
public class PluginSet {
  String fParentClassName;
  Class fParent;
  ArrayList<Class> fPlugins = new ArrayList<Class>();

  public PluginSet(String pParentClassName) throws ClassNotFoundException,
      InstantiationException, IllegalAccessException {
    fParentClassName = pParentClassName;
    fParent = Class.forName(fParentClassName);
  } // constructor

  /**
   * Returns a list of plugin objects.
   * 
   * @return an array of plugin objects. Note that it is up to the caller to
   *         know about the objects. The only thing that is ensured is that the
   *         objects are instances of fParent. If an error occurs, null is
   *         returned.
   */
  public ArrayList<Object> getPlugins() {
    try {
      ArrayList<Object> cpy = new ArrayList<Object>();

      for (int i = 0; i < fPlugins.size(); i++) {
        Class pluginClass = (Class) fPlugins.get(i);
        cpy.add(pluginClass.newInstance());
      }

      return cpy;
    }
    catch (Exception e) {
      CoreLog.L().log(Level.WARNING, "", e);
      return null;
    }
  } // getPlugins

  /**
   * Loads a jar file into the PluginSet if the xml defined classes in the file
   * are descendents (instances) of fParent.
   * 
   * @param pJarFile
   *          holds the name of the jar file.
   * @return true if any class was added to the PluginSet, false otherwise.
   */
  public boolean loadJarFile(File pJarFile) {
    try {
      // Read main-class attribute from jar file.
      JarFile jf = new JarFile(pJarFile);

      // Try reading the named classes from the plugin.xml file.
      String[] classes = readXmlInJarFile(jf);

      // See if any classes were exposed at all.
      if (classes == null || classes.length == 0) {
        CoreLog.L().info("plugin did not contain PLUGIN.XML or any exposed classes");
        return false;
      }

      // Build a file specified absolut path for the class loader. Use two
      // entries
      // ..one for the dir and one for the jar file - just to be sure.
      String jarDir = pJarFile.getParent();
      if (jarDir.endsWith("/") == false)
        jarDir = "file:" + jarDir + "/";
      String urlJarFilePath = new String("file:" + pJarFile.toString());

      // Load the library using URLClassLoader.
      URLClassLoader loader = new URLClassLoader(new URL[] { new URL(jarDir),
          new URL(urlJarFilePath) });

      // Try instance relations for the exposed classes.
      boolean didSomething = false;
      for (int i = 0; i < classes.length; i++) {
        String className = classes[i].trim();
        Class pluginClass = null;
        try {
          pluginClass = Class.forName(className, true, loader);
        }
        catch (Exception e) {
          pluginClass = null;
          CoreLog.L().warning("Plugin class " + className
                  + " rejected, class not found.");
        }
        if (pluginClass != null) {
          // Get Class from its name and allocate a fresh instance.
          Object pluginObject = pluginClass.newInstance();

          // Check if the instance is a descendent of fParent.
          if (fParent.isInstance(pluginObject)) {
            fPlugins.add(pluginClass);
            didSomething = true;
          }
          else
            CoreLog.L().warning("Plugin class " + className
                    + " rejected, invalid instance.");
        } // if class found
      } // for exposed classes in jar file
      return didSomething;
    }
    catch (Exception e) {
      CoreLog.L().log(Level.WARNING, "", e);
      return false;
    }
  } // loadJarFile

  /**
   * Iterates a directory (optionally also the sub dirs) and loads the classes
   * in the jar files that are descendents of fParent.
   * 
   * @param pRoot
   *          holds the root directory (dir that holds the classes/jars).
   * @param pSubDirs
   *          is true if subdirs are to be iterated.
   * @return the number of loaded classes.
   */
  public int loadFromDirectory(File pRoot, boolean pSubDirs) {
    Path dir = Paths.get(pRoot.toURI());
    try{
      Files.walkFileTree(dir, null, pSubDirs ? Integer.MAX_VALUE : 0,
              new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path p, BasicFileAttributes attr) throws IOException{
                  if(p.endsWith(".jar"))
                    loadJarFile(p.toFile());
                  return FileVisitResult.CONTINUE;
                }

              });
    }
    catch(Exception e){
      CoreLog.L().log(Level.SEVERE, "could not load", e);
    }

    return fPlugins.size();
  } // loadFromDirectory

  /**
   * Reads the plugin specification xml file that must reside in the plugin jar
   * file.
   * 
   * @param pJarFile
   *          holds the open jar file.
   * @return a list of class names that are supposed to be plugins. Note that no
   *         instance checking is done in this method.
   */
  protected String[] readXmlInJarFile(JarFile pJarFile) {
    try {
      ZipEntry z = pJarFile.getEntry("META-INF/PLUGIN.XML");
      InputStream s = pJarFile.getInputStream(z);
      PluginXmlParser parser = new PluginXmlParser(s);
      return parser.getExposedClasses();
    }
    catch (IOException i) {
      CoreLog.L().log(Level.WARNING, "", i);
    }
    return null;
  } // readXmlInJarFile

  /** Reads the PLUGIN.XML file in a most simple way. */
  protected class PluginXmlParser extends DefaultHandler {
    boolean fIsRecording = false;
    StringBuffer fBuffer = new StringBuffer();
    public String[] fClasses = null;

    public PluginXmlParser(InputStream pInputStream) {
      try {
        SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        saxParser.parse(pInputStream, this);

        // Split content of buffer at certain white-spaces.
        String[] classes = fBuffer.toString().trim().split(
            "[, \\t\\n\\x0B\\f\\r]");

        // Save only non-empty strings.
        int nTokens = 0;
        for (int i = 0; i < classes.length; i++)
          if (classes[i].length() > 0)
            nTokens++;
        fClasses = new String[nTokens];
        for (int i = 0, j = 0; i < classes.length; i++)
          if (classes[i].length() > 0)
            fClasses[j++] = classes[i];
      }
      catch (Exception e) {
        CoreLog.L().log(Level.WARNING, "", e);
      }
    } // constructor

    public String[] getExposedClasses() {
      return fClasses;
    } // getExposedClasses

    public void characters(char buff[], int offset, int len)
        throws SAXException {
      if (fIsRecording)
        fBuffer.append(buff, offset, len);
    } // characters

    public void startElement(String uri, String localName, String qName,
        org.xml.sax.Attributes attrs) throws SAXException {
      // Scan for source xml files to index.
      if (qName.toLowerCase().equals("classes"))
        fIsRecording = true;
    } // startElement

    public void endElement(String uri, String localName, String qName)
        throws SAXException {
      if (fIsRecording)
        fIsRecording = false;
    } // endElement
  }
} // PluginSet
