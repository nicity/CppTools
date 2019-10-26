// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.advancedtools.cpp.communicator.Communicator;
import com.advancedtools.cpp.commands.ShowPreprocessedCommand;

/**
 * @author maxim
 * Date: Sep 24, 2006
 * Time: 4:08:13 AM
 */
public class ShowPreprocessedTextAction extends BaseEditorAction {
  public void actionPerformed(AnActionEvent anActionEvent) {
    final DataContext dataContext = anActionEvent.getDataContext();
    final PsiFile psiFile = findFileFromDataContext(dataContext);
    final Editor editor = findEditorFromDataContext(dataContext);

    if (!editor.getSelectionModel().hasSelection()) editor.getSelectionModel().selectLineAtCaret();

    Communicator.getInstance(psiFile.getProject()).sendCommand(
      new ShowPreprocessedCommand(psiFile.getVirtualFile().getPath(), editor)
    );
  }

  protected boolean acceptableState(Editor editor, PsiFile file) {
    return true;
  }
}
