// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.makefile;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;

/**
 * @author maxim
 */
class MakefileLexer extends MergingLexerAdapter {
  private static final TokenSet tokenSet = TokenSet.create(MakefileTokenTypes.TEMPLATE_DATA, MakefileTokenTypes.WHITE_SPACE);

  public MakefileLexer(boolean highlighting) {
    super(new FlexAdapter(new _MakefileLexer(highlighting)), tokenSet);
  }
}
