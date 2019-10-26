// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
