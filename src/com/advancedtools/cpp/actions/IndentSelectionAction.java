// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.actions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.advancedtools.cpp.psi.CppFile;
import com.advancedtools.cpp.commands.StringCommand;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.advancedtools.cpp.actions.refactoring.ChangesSupport;

/**
 * @author maxim
 * Date: 08.06.2009
 * Time: 13:53:47
 */
public class IndentSelectionAction extends BaseEditorAction {
  @Override
   protected void execute(Editor editor, PsiFile file, final Project project) {
    new StringCommand(
      "indent " +
        BuildingCommandHelper.getQuotedVirtualFileNameAsString(file) +
        " " + editor.getSelectionModel().getSelectionStart() +
        " " + editor.getSelectionModel().getSelectionEnd()
    ) {
      final ChangesSupport changesSupport = new ChangesSupport();

      @Override
      public boolean doInvokeInDispatchThread() {
        return true;
      }

      @Override
      public void commandOutputString(String str) {
        changesSupport.appendChangesFromString(str);
      }

      @Override
      public void doExecute() {
        changesSupport.applyChanges("Cpp.Indent", project);
      }
    }.post(project);
  }

  protected boolean acceptableState(Editor editor, PsiFile file) {
    if (file instanceof CppFile && editor.getSelectionModel().hasSelection()) {
      return true;
    }
    return false;
  }
}
