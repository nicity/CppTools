// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.rename.PsiElementRenameHandler;
import com.intellij.refactoring.rename.RenameHandler;
import org.jetbrains.annotations.NotNull;

/**
* @author maxim
* Date: 2/7/12
* Time: 12:48 PM
*/
public class CppRenameHandler implements RenameHandler {
  final PsiElementRenameHandler renameHandler = new PsiElementRenameHandler();

  public boolean isAvailableOnDataContext(DataContext dataContext) {
    return CppSupportSettings.getInstance().canDoSomething(dataContext);
  }

  public boolean isRenaming(DataContext dataContext) {
    return CppSupportSettings.getInstance().canDoSomething(dataContext);
  }

  public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile, DataContext dataContext) {
    renameHandler.invoke(project, editor, psiFile, dataContext);
  }

  public void invoke(@NotNull Project project, @NotNull PsiElement[] psiElements, DataContext dataContext) {
    renameHandler.invoke(project, psiElements, dataContext);
  }
}
