// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.intellij.lang.PairedBraceMatcher;
import com.intellij.lang.BracePair;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author maxim
 * Date: 23.09.2006
 * Time: 3:16:22w
 */
public class CppBraceMatcher implements PairedBraceMatcher {
  private static final BracePair[] PAIRS = new BracePair[] {
    new BracePair(CppTokenTypes.LPAR, CppTokenTypes.RPAR, false),
    new BracePair(CppTokenTypes.LBRACKET, CppTokenTypes.RBRACKET, false),
    new BracePair(CppTokenTypes.LBRACE, CppTokenTypes.RBRACE, true)
  };

  public BracePair[] getPairs() {
    return PAIRS;
  }

  public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType iElementType, @Nullable IElementType tokenType) {
    if (tokenType == CppTokenTypes.WHITE_SPACE ||
        CppTokenTypes.COMMENTS.contains(tokenType) ||
        CppTokenTypes.RBRACE == tokenType ||
        null == tokenType ||
        CppTokenTypes.COMMA == tokenType ||
        CppTokenTypes.SEMICOLON == tokenType ||
        CppTokenTypes.COLON == tokenType ||
        CppTokenTypes.RPAR == tokenType ||
        CppTokenTypes.RBRACKET == tokenType
       ) {
      return true;
    }
    return false;
  }

  // IDEA8
  public int getCodeConstructStart(PsiFile psiFile, int i) {
    return i;
  }
}
