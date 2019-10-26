// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.actions.generate;

import com.advancedtools.cpp.actions.BaseEditorAction;
import com.advancedtools.cpp.actions.refactoring.ChangesSupport;
import com.advancedtools.cpp.commands.StringCommand;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

/**
 * @author maxim
 */
public class GenerateCaseBranchesFromEnumAction extends BaseEditorAction {
  @Override
  protected void execute(Editor editor, PsiFile file, final Project project) {
    new MyStringCommand(file, editor, project).post(project);
  }

  protected boolean acceptableState(Editor editor, PsiFile file) {
    return true;
  }

  private class MyStringCommand extends StringCommand {
    final ChangesSupport changesSupport;
    private final Project project;

    public MyStringCommand(PsiFile file, Editor editor, Project project) {
      super("generate-enum-switch-at " +
        BuildingCommandHelper.quote(file.getVirtualFile().getPath()) + " " +
        editor.getCaretModel().getOffset());
      this.project = project;
      changesSupport = new ChangesSupport();
    }

    @Override
    public void commandOutputString(String str) {
      changesSupport.appendChangesFromString(str);
    }

    @Override
    public boolean doInvokeInDispatchThread() {
      return true;
    }

    @Override
    public void doExecute() {
      changesSupport.applyChanges("CppSupport." + getClass().getName(), project);
    }
  }
}
