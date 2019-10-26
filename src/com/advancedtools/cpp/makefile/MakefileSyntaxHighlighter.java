// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.makefile;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author maxim
*/
class MakefileSyntaxHighlighter extends SyntaxHighlighterBase {
  private static Map<IElementType, TextAttributesKey> keys1;
  private static Map<IElementType, TextAttributesKey> keys2;

  public static final TextAttributesKey Makefile_KEYWORD = TextAttributesKey.createTextAttributesKey(
    "Makefile.KEYWORD",
    SyntaxHighlighterColors.KEYWORD.getDefaultAttributes()
  );

  static final TextAttributesKey Makefile_LINE_COMMENT = TextAttributesKey.createTextAttributesKey(
    "Makefile.LINE_COMMENT",
    SyntaxHighlighterColors.LINE_COMMENT.getDefaultAttributes()
  );

  static final TextAttributesKey Makefile_TEMPLATE_DATA = TextAttributesKey.createTextAttributesKey(
    "Makefile.TEMPLATE_DATA",
    SyntaxHighlighterColors.NUMBER.getDefaultAttributes()
  );

  static final TextAttributesKey Makefile_TARGET = TextAttributesKey.createTextAttributesKey(
    "Makefile.TARGET",
    SyntaxHighlighterColors.DOC_COMMENT_TAG.getDefaultAttributes()
  );

  static final TextAttributesKey Makefile_DEFINITION = TextAttributesKey.createTextAttributesKey(
    "Makefile.DEFINITION",
    SyntaxHighlighterColors.DOC_COMMENT_MARKUP.getDefaultAttributes()
  );

  static {
    keys1 = new HashMap<IElementType, TextAttributesKey>();
    keys2 = new HashMap<IElementType, TextAttributesKey>();

    fillMap(keys1, MakefileTokenTypes.KEYWORDS, Makefile_KEYWORD);
    keys1.put(MakefileTokenTypes.END_OF_LINE_COMMENT, Makefile_LINE_COMMENT);
    keys1.put(MakefileTokenTypes.TEMPLATE_DATA, Makefile_TEMPLATE_DATA);
    keys1.put(MakefileTokenTypes.TARGET_IDENTIFIER_PART, Makefile_TARGET);
    keys1.put(MakefileTokenTypes.VAR_DEFINITION, Makefile_DEFINITION);
  }

  @NotNull
  public Lexer getHighlightingLexer() {
    return new MakefileLexer(true);
  }

  @NotNull
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    return pack(keys1.get(tokenType), keys2.get(tokenType));
  }
}
