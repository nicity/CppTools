// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
