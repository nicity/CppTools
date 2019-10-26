// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.facade;

import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.advancedtools.cpp.commands.NavigationCommand;
import com.advancedtools.cpp.utils.NavigationUtils;
import org.jetbrains.annotations.NonNls;

/**
 * @author maxim
 * Date: Dec 24, 2008
 * Time: 9:06:02 PM
 */
public class GotoSuperActionHandler implements LanguageCodeInsightActionHandler {
  public boolean isValidFor(Editor editor, PsiFile file) {
    return true;
  }

  public void invoke(Project project, Editor editor, PsiFile file) {
    new NavigationCommand(file, editor.getCaretModel().getOffset()) {
      @NonNls
      @Override
      protected String getCommandText() {
        return "find-immediate-parents";
      }

      public boolean doInvokeInDispatchThread() {
        return true;
      }

      public void doExecute() {
        super.doExecute();
        NavigationUtils.navigate(project, usagesList);
      }
    }.post(project);
  }

  public boolean startInWriteAction() {
    return false;
  }
}
