// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.refactoring;

import com.advancedtools.cpp.CppSupportLoader;
import com.intellij.ide.DataManager;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.refactoring.ui.NameSuggestionsField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * User: maxim
 * Date: 27.09.2006
 * Time: 1:40:19
 */
public class IntroduceVariableDialog extends DialogWrapper {
  private JPanel myPanel;
  private NameSuggestionsField myNewVarName;
  private JLabel myNewVarNameText;
  private JLabel myNewVarType;
  private String myIntroducedType;

  IntroduceVariableDialog(final Project project, String introducedType) {
    super(project, true);

    myIntroducedType = introducedType;

    setTitle("Introduce Variable");
    myNewVarType.setText(introducedType);

    setupNameValidation(project, myNewVarName, getOKAction());

    init();
  }

  static void setupNameValidation(final Project project, final NameSuggestionsField myNewVarName, final Action okAction) {
    final NameSuggestionsField.DataChanged dataChangedListener = new NameSuggestionsField.DataChanged() {
      public void dataChanged() {
        final NamesValidator namesValidator = LanguageNamesValidation.INSTANCE.forLanguage(CppSupportLoader.CPP_FILETYPE.getLanguage());
        okAction.setEnabled(
          namesValidator.isIdentifier(myNewVarName.getEnteredName(), project)
        );
      }
    };
    myNewVarName.addDataChangedListener(dataChangedListener);
    dataChangedListener.dataChanged();
  }

  public JComponent getPreferredFocusedComponent() {
    return myNewVarName.getFocusableComponent();
  }

  @Nullable
  protected JComponent createCenterPanel() {
    return myPanel;
  }

  public String getVarName() {
    return myNewVarName.getEnteredName();
  }

  private void createUIComponents() {
    myNewVarName = new NameSuggestionsField(
      calcVariants(),
      (Project) DataManager.getInstance().getDataContext().getData(DataConstants.PROJECT),
      CppSupportLoader.CPP_FILETYPE
    );
  }

  private String[] calcVariants() {
    // TODO: build many names
    String suggestedName = "";

    for(int i = myIntroducedType.lastIndexOf(':') + 1; i < myIntroducedType.length(); ++i) {
      char ch = myIntroducedType.charAt(i);
      if(Character.isJavaIdentifierPart(ch)) suggestedName += ch;
    }

    suggestedName = StringUtil.decapitalize(suggestedName);
    return new String[] {suggestedName};
  }
}
