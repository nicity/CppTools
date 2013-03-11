/* AdvancedTools, 2007, all rights reserved */
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
