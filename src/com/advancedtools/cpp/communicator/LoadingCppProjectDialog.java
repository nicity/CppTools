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
public class LoadingCppProjectDialog extends DialogWrapper {
  private JComboBox comboBox1;
  private JPanel panel;

  protected LoadingCppProjectDialog(Project project, String[] files) {
    super(project, false);
    setTitle(CppBundle.message("loading.cpp.project.dialog.title"));
    comboBox1.setModel(new DefaultComboBoxModel(files));
    if (files.length > 0) comboBox1.setSelectedItem(files[0]);
    init();
  }

  @Nullable
  protected JComponent createCenterPanel() {
    return panel;
  }

  public String getSelectedProjectFile() {
    return (String) comboBox1.getSelectedItem();
  }
}
