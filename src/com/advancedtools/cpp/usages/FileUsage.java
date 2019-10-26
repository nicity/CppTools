// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
