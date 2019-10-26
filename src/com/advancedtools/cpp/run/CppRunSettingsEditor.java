// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.run;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
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
public class CppRunSettingsEditor extends BaseCppRunSettingsEditor<CppRunConfiguration> {
  private JPanel myPanel;
  private ComboboxWithBrowseButton myExecutableName;
  private JComboBox myExecutableParameters;
  private TextFieldWithBrowseButton workingDirectoryTextField;
  private static final String CHOOSE_WORKING_DIRECTORY_TITLE = "Choose working directory";

  public CppRunSettingsEditor(Project project) {
    setupCommonUI(project);
    workingDirectoryTextField.addBrowseFolderListener(
      CHOOSE_WORKING_DIRECTORY_TITLE,
      CHOOSE_WORKING_DIRECTORY_TITLE,
      project,
      new FileChooserDescriptor(false, true, false, false, false, false),
      TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
    );
  }

  protected void resetEditorFrom(CppRunConfiguration s) {
    super.resetEditorFrom(s);
    CppRunnerParameters runnerParameters = s.getRunnerParameters();
    myExecutableParameters.getEditor().setItem(runnerParameters != null ? runnerParameters.getExecutableParameters() : "");
    workingDirectoryTextField.setText(runnerParameters != null ? runnerParameters.getWorkingDir() : "");
  }

  protected void applyEditorTo(CppRunConfiguration s) throws ConfigurationException {
    super.applyEditorTo(s);
    CppRunnerParameters runnerParameters = s.getRunnerParameters();
    runnerParameters.setExecutableParameters((String) myExecutableParameters.getEditor().getItem());
    runnerParameters.setWorkingDir(workingDirectoryTextField.getText());
  }

  @NotNull
  protected JComponent createEditor() {
    return myPanel;
  }

  protected void disposeEditor() {
    myPanel = null;
    myExecutableName = null;
    workingDirectoryTextField = null;
    myExecutableParameters = null;
  }

  protected ComboboxWithBrowseButton getExecutableName() {
    return myExecutableName;
  }
}
