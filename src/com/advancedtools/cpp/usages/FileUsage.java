/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.usages;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.LocalFileSystem;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * @author maxim
 * Date: 02.06.2006
 * Time: 18:39:09
 */
public class FileUsage {
  public final String fileName;
  private VirtualFile vfile;
  public final List<OurUsage> usageList = new ArrayList<OurUsage>();

  public FileUsage(String _fileName) {
    fileName = _fileName;
  }

  public VirtualFile findVirtualFile() {
    if (vfile == null) {
      vfile = LocalFileSystem.getInstance().findFileByPath(fileName.replace(File.separatorChar,'/'));
    }
    return vfile;
  }

  public String getFileLocaton() {
    VirtualFile file = findVirtualFile();
    return formatFile(file);
  }

  public static String formatFile(VirtualFile file) {
    return file.getName() + " ( " + file.getParent().getPath() + " )";
  }
}
