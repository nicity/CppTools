// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.actions;

import com.advancedtools.cpp.commands.NavigationCommand;
import com.advancedtools.cpp.communicator.Communicator;
import com.advancedtools.cpp.utils.NavigationUtils;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NonNls;

/**
 * @author maxim
 * Date: Sep 24, 2006
 * Time: 4:08:13 AM
 */
public class GotoSupersAction extends BaseEditorAction {
  public void actionPerformed(AnActionEvent anActionEvent) {
    doAction(anActionEvent, true);
  }

  static void doAction(AnActionEvent anActionEvent, final boolean supers) {
    final DataContext dataContext = anActionEvent.getDataContext();
    final PsiFile psiFile = findFileFromDataContext(dataContext);
    final Editor editor = findEditorFromDataContext(dataContext);

    Communicator.getInstance(psiFile.getProject()).sendCommand(
      new NavigationCommand(psiFile, editor.getCaretModel().getOffset()) {
        @NonNls @Override protected String getCommandText() {
          return supers ? "find-parents" : "find-inheritors";
        }

        public boolean doInvokeInDispatchThread() {
          return true;
        }

        public void doExecute() {
          super.doExecute();
          NavigationUtils.navigate(project, usagesList);
        }
      }
    );
  }

  protected boolean acceptableState(Editor editor, PsiFile file) {
    return true;
  }
}
