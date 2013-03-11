/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.communicator;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.project.Project;
import com.advancedtools.cpp.CppBundle;

import javax.swing.*;

import org.jetbrains.annotations.Nullable;

/**
 * @author maxim
 */
public class WarnAboutFilesOutOfSourceRootsDialog extends DialogWrapper {
  private JPanel panel;

  public WarnAboutFilesOutOfSourceRootsDialog(Project project) {
    super(project, false);
    setTitle(CppBundle.message("c.c.file.outside.of.source.roots.dialog.title"));
    init();
  }
  
  @Nullable
  protected JComponent createCenterPanel() {
    return panel;
  }
}
