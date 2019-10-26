// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.refactoring;

import com.advancedtools.cpp.CppSupportLoader;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.refactoring.ui.NameSuggestionsField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author maxim
* Date: 11.06.2008
* Time: 1:04:40
*/
class ExtractFunctionDialog extends DialogWrapper {
  private JPanel myPanel;
  private NameSuggestionsField functionMethodNameTextField;

  protected ExtractFunctionDialog(final Project project) {
    super(project, true);
    setTitle("Extract Function / Method");

    IntroduceVariableDialog.setupNameValidation(project, functionMethodNameTextField, getOKAction());

    init();
  }

  @Nullable
  protected JComponent createCenterPanel() {
    return myPanel;
  }

  public String getFunctionName() {
    return functionMethodNameTextField.getEnteredName();
  }

  public JComponent getPreferredFocusedComponent() {
    return functionMethodNameTextField.getFocusableComponent();
  }

  private void createUIComponents() {
    functionMethodNameTextField = new NameSuggestionsField(
      new String[] {"fun"},
      (Project) DataManager.getInstance().getDataContext().getData(DataConstants.PROJECT),
      CppSupportLoader.CPP_FILETYPE
    );
  }
}
