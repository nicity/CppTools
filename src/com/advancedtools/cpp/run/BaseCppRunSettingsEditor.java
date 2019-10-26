// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.run;

import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.advancedtools.cpp.run.CppRunConfiguration;
import com.advancedtools.cpp.run.CppRunnerParameters;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author maxim
 * Date: 28.03.2009
 * Time: 22:18:19
 */
public abstract class BaseCppRunSettingsEditor<T extends BaseCppConfiguration> extends SettingsEditor<T> {

  private static final String CHOOSE_EXECUTABLE_TITLE = "Choose Executable";

  protected void setupCommonUI(Project project) {
    ComboboxWithBrowseButton myExecutableName = getExecutableName();
    myExecutableName.getComboBox().setEditable(true);
    myExecutableName.addBrowseFolderListener(
      CHOOSE_EXECUTABLE_TITLE,
      CHOOSE_EXECUTABLE_TITLE,
      project,
      new FileChooserDescriptor(true, false, false, false, false, false) {
        @Override
        public boolean isFileSelectable(VirtualFile file) {
          return super.isFileSelectable(file);
        }
      },
      TextComponentAccessor.STRING_COMBOBOX_WHOLE_TEXT
    );
  }

  protected abstract @NotNull ComboboxWithBrowseButton getExecutableName();

  protected void resetEditorFrom(T s) {
    BaseCppRunnerParameters runnerParameters = s.getRunnerParameters();
    getExecutableName().getComboBox().getEditor().setItem(runnerParameters != null ? runnerParameters.getExecutableName():"");
  }

  protected void applyEditorTo(T s) throws ConfigurationException {
    BaseCppRunnerParameters runnerParameters = s.getRunnerParameters();
    if (runnerParameters == null) {
      runnerParameters = s.createRunnerParameters();
    }
    runnerParameters.setExecutableName((String)getExecutableName().getComboBox().getEditor().getItem());

    s.setRunnerParameters(runnerParameters);
  }
}