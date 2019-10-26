// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandler;
import com.intellij.codeInsight.editorActions.SelectWordUtil;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.wm.WindowManager;
import com.advancedtools.cpp.commands.SelectWordCommand;

import java.util.List;
import java.util.ArrayList;

/**
 * @author maxim
 * Date: Sep 24, 2006
 * Time: 6:17:59 AM
 */
public class CppSelectioner implements ExtendWordSelectionHandler {
  public boolean canSelect(PsiElement psiElement) {
    return psiElement.getLanguage() == CppSupportLoader.CPP_FILETYPE.getLanguage();
  }

  public List<TextRange> select(PsiElement psiElement, CharSequence charSequence, int i, Editor editor) {
    final PsiFile psiFile = psiElement.getContainingFile();
    final SelectWordCommand command = new SelectWordCommand(psiFile.getVirtualFile().getPath(),editor);
    command.post(psiFile.getProject());

    List<TextRange> result = new ArrayList<TextRange>();
    ASTNode node = psiElement.getNode();
    if (node != null && node.getElementType() == CppTokenTypes.STRING_LITERAL) {
      int textOffset = psiElement.getTextOffset();
      result.add(new TextRange(textOffset + 1, textOffset + psiElement.getTextLength() - 1));
    }
    if (!command.hasReadyResult()) {
      WindowManager.getInstance().getStatusBar(psiFile.getProject()).setInfo("Command was cancelled");
      return result;
    }

    final int start = command.getSelectionStart();
    int selectionEnd = command.getSelectionEnd();

    if (selectionEnd >= editor.getDocument().getTextLength()) {
      selectionEnd = editor.getDocument().getTextLength();
    }
    result.add(new TextRange(start, selectionEnd));

    return result;
  }
}
