// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.actions.generate;

import com.advancedtools.cpp.actions.BaseEditorAction;
import com.advancedtools.cpp.commands.GenerateSomethingCommand;
import com.advancedtools.cpp.communicator.Communicator;
import com.advancedtools.cpp.psi.CppFile;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

/**
 * @author maxim
 */
public abstract class BaseGenerateAction extends BaseEditorAction {
  public enum GenerateType {
    CONSTRUCTOR, CONSTRUCTOR_WITH_PARAMETERS, COPY_CONSTRUCTOR, ASSIGNMENT_OPERATOR
  }

  @Override
  protected void execute(Editor editor, PsiFile file, Project project) {
    Communicator.getInstance(project).sendCommand(
      new GenerateSomethingCommand(file.getVirtualFile().getPath(), editor, getGenerationType())
    );
  }

  protected abstract GenerateType getGenerationType();

  protected boolean acceptableState(Editor editor, PsiFile file) {
    if (file instanceof CppFile) {
      return true;
    }
    return false;
  }
}
