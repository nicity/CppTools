// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
