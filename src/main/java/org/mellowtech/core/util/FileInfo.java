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

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileSystemView;

import org.mellowtech.core.CoreLog;

/**
 * File icon and file info provider.
 * 
 * This class gathers functionality regarding files and file systems that are
 * not available in java.io.File.
 * 
 * 
 */
public class FileInfo {
  
  HashMap<String, String[]> extToInfo = new HashMap<String, String[]>();
  HashMap<String, Icon> extToIcon = new HashMap<String, Icon>();
  ImageIcon iconDirOpen;
  ImageIcon iconDirClosed;
  ImageIcon iconFile;
  FileSystemView fileSystemView;
  JFileChooser fileChooser;
  
  // List of extensions that we will try to read and display, that 
  // are not in the list of default icons (they may vary between os:es). 
  String[] extList = new String[] { 
      "txt", "pdf", "doc", "html", "htm", "gif", "jpeg", "bmp", 
      "tar", "zip", "gz", "ps", "dll", "mp3", "avi", "xml",
      "png", "jpg", "dat", "jar", "rtf" };
  
  // List of default icons, shipped with the package. 
  // TODO I have not found an 
  // easy way to read the package directory etc/icons 
  // to list the icons residing there, why I list them all here.
  String[] iconExtList = new String[] {
      "doc", "dot", "odp", "ods", "odt", "otp", "pdf", "ppt", "rtf", 
      "sdw", "shtml", "sxi", "sxw", "text", "txt", "xls", "xml"
  };
  
  HashSet<String> extSet = new HashSet<String>();
  
  protected static FileInfo instance = null;

  public boolean hasNormalApplicationExtension(File f) {
    String ext = this.getExtension(f);
    if (ext == null) 
      return false;
    return extSet.contains(ext);
  }
  
  public Set<String> getExtensions() {
    HashSet<String> extensionSet = new HashSet<String>();
    for (int i = 0; i < extList.length; i++)
      extensionSet.add(extList[i]);
    return extensionSet;
  }
  private FileInfo() {
    iconDirOpen = new ImageIcon(FileInfo.class.getResource("/etc/icons/folder_open_x.gif"));
    iconDirClosed = new ImageIcon(FileInfo.class.getResource("/etc/icons/folder_closed_x.gif"));
    iconFile = new ImageIcon(FileInfo.class.getResource("/etc/icons/file_obj.gif"));
    for (int i = 0; i < extList.length; i++)
      extSet.add(extList[i]);
    setupFileInfo();
  }
  protected boolean hasLoaded = false;
  
  protected void setupFileInfo() {
    fileChooser = new JFileChooser();
    fileSystemView = FileSystemView.getFileSystemView();
    
    Thread t = new Thread(new Runnable() {
      public void run() {
        try{      
          for (int i = 0; iconExtList != null && i < iconExtList.length; i++) {
          String imagepath = "/etc/icons/" + iconExtList[i] + ".png";
          ImageIcon imageIcon = new ImageIcon(FileInfo.class.getResource(imagepath));
          extToIcon.put(iconExtList[i], imageIcon);
          }
        } catch(Exception e) {
         CoreLog.L().log(Level.WARNING, "", e);
        }
        hasLoaded = true;
      }
    });
    t.start();
  }
  private static int instanceCounter = 0;
  public static FileInfo getInstance() {
    if (instance == null) {
     instance = new FileInfo();
    }
    return instance;
  }
  
  public JFileChooser getJFileChooser() {
    return fileChooser;
  }

  public String getExtension(File file) {
  	return getExtension(file.getName());
  }
  
  public String getExtension(String fName){
  	int lastDotPos = fName.lastIndexOf('.');
    if (lastDotPos == -1)
      return null;
    return fName.substring(lastDotPos + 1).toLowerCase();
  }

  public Icon getDirOpenIcon() {
    return iconDirOpen;
  }

  public Icon getDirClosedIcon() {
    return iconDirClosed;
  }

  public Icon getDefaultFileIcon() {
    return iconFile;
  }

  public Icon getIcon(String fName){
  	long start = System.currentTimeMillis(); 
    while (!hasLoaded && (System.currentTimeMillis() - start) < 100) {
  	  try {
       Thread.sleep(20); 
      }
      catch(Exception e) {
        CoreLog.L().log(Level.WARNING, "");
      }
    }
    File f = new File(fName);
    if(f.exists())
  		return getIcon(f);
  	String ext = getExtension(fName);
  	if(ext == null)
  		return iconFile;
  	Icon icon = extToIcon.get(ext);
  	if(icon == null)
  		return iconFile;
  	return icon;
  	
  }
  
  /**
   * Returns an icon representing the specified file.
   * 
   * @param file file
   * @return the icon
   */
  public Icon getIcon(File file) {
    long start = System.currentTimeMillis(); 
    while (!hasLoaded && (System.currentTimeMillis() - start) < 100) {
      try {
       Thread.sleep(20); 
      }
      catch(Exception e) {
        CoreLog.L().log(Level.WARNING, "", e);
      }
    }
    if (file.isDirectory()) {
      return fileChooser.getIcon(file);// iconDirClosed;
    }
    String ext = getExtension(file);
    if (ext == null) {
      return iconFile;
    }
    
    if (extSet.contains(ext)) {
      Icon icon = extToIcon.get(ext);
      if (icon != null) {
        return icon;
      }
      icon = fileChooser.getIcon(file);
      if (icon == null) {
        extToIcon.put(ext, iconFile);
        return iconFile;
      }
      else {
        extToIcon.put(ext, icon);
        return icon;
      }
    }
    else {
      return fileChooser.getIcon(file);
    }
  }

  /**
   * Get the system display name [0] and type [1] for the file
   * 
   * @param file file
   * @return the icon
   * @throws Exception if error
   */
  public String[] getSystemDisplayNameAndType(File file) throws Exception {
    String ext = getExtension(file);
    String[] info = extToInfo.get(ext);
    if (info == null) {
      info = new String[2];
      if (file.exists())
        info[1] = fileChooser.getTypeDescription(file);
      else
        info[1] = " (File not found)";
      extToInfo.put(ext, info);
    }
    info[0] = fileChooser.getName(file);
    if (!file.exists()) 
      info[0] = new String(info[0] + " (File Not Found, removed?)");
    return info;
  }

  public boolean isTraversable(File file) {
    return fileChooser.isTraversable(file);
  }
  
  public File[] getRoots() {
    File[] roots = fileSystemView.getRoots();
    if (Platform.isWindows() && roots.length == 1) {
      File f = roots[0];
      roots = getFiles(f, true, false); 
      ArrayList<File> directories = new ArrayList<File>();
      for (File dir : roots) {
        if(isTraversable(dir))
          directories.add(dir);
      }
      roots = directories.toArray(new File[0]);
    }
    return roots;
  }

  public File[] getFiles(File file, boolean useFileHiding) {
    return getFiles(file, useFileHiding, false);
  }
  
  public File[] getFiles(File file, boolean useFileHiding, boolean removeNonFileSystemFiles) {
    if (!file.exists()) 
      return null;
    File[] files = fileSystemView.getFiles(file, useFileHiding);
    if (!removeNonFileSystemFiles) 
      return files;
    List<File> fileSystemFiles = new ArrayList<File>();
    for (File f : files) {
      if (isFileSystem(f)) 
        fileSystemFiles.add(f);
    }
    return fileSystemFiles.toArray(new File[0]);
  }

  public File getParentDirectory(File file) {
    return fileSystemView.getParentDirectory(file);
  }

  public boolean isFileSystem(File f) {
    return fileSystemView.isFileSystem(f);
  }
  
  public boolean isParent(File dir, File file) {
    return fileSystemView.isParent(dir, file);
  }
  
  public boolean isLinkOrNonExisting(File f) throws IOException {
    if (!f.exists())
      return true; // throw new IOException("Non-existent file");
    String ext = getExtension(f);
    if (ext != null && ext.equalsIgnoreCase("lnk"))
      return true;
    String canonical = f.getCanonicalPath();
    String absolute = f.getAbsolutePath();

    return !canonical.equalsIgnoreCase(absolute);
  }

    int counter = 0;
  int found = 0;

  public void generateIcons(String dir) throws Exception {
    final TreeMap<String, Icon> iconMap = new TreeMap<String, Icon>();

    Path p = Paths.get(dir);
    Files.walkFileTree(p, new SimpleFileVisitor<Path>(){
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String ext = FileInfo.getInstance().getExtension(file.toFile());
        if (ext != null && !iconMap.containsKey(ext)) {
          found++;
          Icon icn = FileInfo.getInstance().fileSystemView.getSystemIcon(file.toFile());
          iconMap.put(ext, icn);
        }
        return FileVisitResult.CONTINUE;

      }
    });



    Map<String, BufferedImage> extpng = new TreeMap<String, BufferedImage>();

    for (Map.Entry<String, Icon> m : iconMap.entrySet()) {
      String name = m.getKey();
      Icon icon = m.getValue();
      if (! (icon instanceof ImageIcon)) {
        continue;
      }
      ImageIcon ii = (ImageIcon) icon;
      Image image = ii.getImage();
      String ext = "png";
      Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(ext);
      while (iter.hasNext()) {
        ImageWriter iw = iter.next();
        File f = new File("i:/Icons/" + name + "." + ext);
        try {
          ImageOutputStream ios = ImageIO.createImageOutputStream(f);
          iw.setOutput(ios);
          BufferedImage bi = toBufferedImage(image);
          iw.write(bi);
          extpng.put(name, bi);
          ios.close();
          break;
        }
        catch (Exception e) {
          CoreLog.L().log(Level.WARNING, "could not create icon", e);
        }
      }
    }

    JFrame frame = new JFrame("Icons");
    frame.getContentPane().setLayout(
        new GridLayout(iconMap.entrySet().size() / 2, 6));
    for (Iterator<Map.Entry<String, Icon>> iter = iconMap.entrySet().iterator(); iter
        .hasNext();) {
      Map.Entry<String, Icon> e = iter.next();
      String name = e.getKey();
      Icon icon = e.getValue();
      BufferedImage image = new BufferedImage(icon.getIconWidth(), icon
          .getIconHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics g = image.getGraphics();
      JLabel dummyLabel = new JLabel();
      icon.paintIcon(dummyLabel, g, 0, 0);
      g.dispose();
      frame.getContentPane().add(new JLabel(name, icon, SwingConstants.LEFT));
      frame.getContentPane().add(
          new JLabel(name, new ImageIcon(image), SwingConstants.LEFT));
      BufferedImage bi = extpng.get(name.substring(0, name.length() - 1));
      if (bi != null)
        frame.getContentPane().add(
            new JLabel(name + "[]", new ImageIcon(bi), SwingConstants.LEFT));
      else
        frame.getContentPane().add(new JLabel(name, SwingConstants.LEFT));
    }

    frame.setSize(250, 400);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);

  }

  // This method returns true if the specified image has transparent pixels
  public static boolean hasAlpha(Image image) {
    // If buffered image, the color model is readily available
    if (image instanceof BufferedImage) {
      BufferedImage bimage = (BufferedImage) image;
      return bimage.getColorModel().hasAlpha();
    }

    // Use a pixel grabber to retrieve the image's color model;
    // grabbing a single pixel is usually sufficient
    PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
    try {
      pg.grabPixels();
    }
    catch (InterruptedException e) {
    }

    // Get the image's color model
    ColorModel cm = pg.getColorModel();
    return cm.hasAlpha();
  }

  // This method returns a buffered image with the contents of an image
  public static BufferedImage toBufferedImage(Image image) {
    if (image instanceof BufferedImage) {
      return (BufferedImage) image;
    }

    // This code ensures that all the pixels in the image are loaded
    image = new ImageIcon(image).getImage();

    // Determine if the image has transparent pixels; for this method's
    // implementation, see e661 Determining If an Image Has Transparent Pixels
    boolean hasAlpha = hasAlpha(image);

    // Create a buffered image with a format that's compatible with the screen
    BufferedImage bimage = null;
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    try {
      // Determine the type of transparency of the new buffered image
      int transparency = Transparency.OPAQUE;
      if (hasAlpha) {
        transparency = Transparency.BITMASK;
      }

      // Create the buffered image
      GraphicsDevice gs = ge.getDefaultScreenDevice();
      GraphicsConfiguration gc = gs.getDefaultConfiguration();
      bimage = gc.createCompatibleImage(image.getWidth(null), image
          .getHeight(null), transparency);
    }
    catch (HeadlessException e) {
      // The system does not have a screen
    }

    if (bimage == null) {
      // Create a buffered image using the default color model
      int type = BufferedImage.TYPE_INT_RGB;
      if (hasAlpha) {
        type = BufferedImage.TYPE_INT_ARGB;
      }
      bimage = new BufferedImage(image.getWidth(null), image.getHeight(null),
          type);
    }

    // Copy image to buffered image
    Graphics g = bimage.createGraphics();

    // Paint the image onto the buffered image
    g.drawImage(image, 0, 0, null);
    g.dispose();

    return bimage;
  }


}
