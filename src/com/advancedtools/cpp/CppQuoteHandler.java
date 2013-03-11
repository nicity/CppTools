package com.advancedtools.cpp;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;

/**
 * @author maxim
 */
public class CppQuoteHandler extends SimpleTokenSetQuoteHandler {
  public CppQuoteHandler() {
    super(CppTokenTypes.STRING_LITERAL, CppTokenTypes.SINGLE_QUOTE_STRING_LITERAL);
  }
}
