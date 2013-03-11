/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.settings;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.advancedtools.cpp.communicator.Communicator;
import com.advancedtools.cpp.settings.AbstractFileListEditor;

/**
 * @author maxim
 */
public class IncludeFilesListEditor extends AbstractFileListEditor {
  public IncludeFilesListEditor(String title, @NotNull String pathesList) {
    super(title, pathesList);

    init();
  }

  protected FileChooserDescriptor configureFileChooserDescriptor() {
    return new FileChooserDescriptor(true, false, false, false, false, false) {
      public boolean isFileSelectable(VirtualFile virtualFile) {
        return Communicator.isHeaderFile(virtualFile);
      }
    };
  }

  protected boolean isValidFile(Object value) {
    final VirtualFile byPath = getFile(value);
    return byPath != null && Communicator.isHeaderFile(byPath);
  }

  protected String getChooseTitle() {
    return "Choose Include File";
  }
}
