/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;

/**
 * @author maxim
 * Date: Sep 24, 2006
 * Time: 4:08:13 AM
 */
public class GotoInheritorsAction extends BaseEditorAction {
  public void actionPerformed(AnActionEvent anActionEvent) {
    GotoSupersAction.doAction(anActionEvent, false);
  }

  protected boolean acceptableState(Editor editor, PsiFile file) {
    return true;
  }
}
