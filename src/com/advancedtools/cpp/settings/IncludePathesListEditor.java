// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.settings;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.advancedtools.cpp.settings.AbstractFileListEditor;

/**
 * @author maxim
 */
public class IncludePathesListEditor extends AbstractFileListEditor {
  public IncludePathesListEditor(String title, @NotNull String pathesList) {
    super(title, pathesList);

    init();
  }

  protected FileChooserDescriptor configureFileChooserDescriptor() {
    return new FileChooserDescriptor(false, true, false, false, false, false) {
      public boolean isFileSelectable(VirtualFile virtualFile) {
        return virtualFile.isDirectory();
      }
    };
  }

  protected boolean isValidFile(Object value) {
    VirtualFile byPath = getFile(value);
    return byPath != null && byPath.isDirectory();
  }

  protected String getChooseTitle() {
    return "Choose Include Path";
  }
}
