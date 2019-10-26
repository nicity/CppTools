// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.advancedtools.cpp.facade.EnvironmentFacade;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IElementType;

/**
* @author maxim
* Date: 2/3/12
* Time: 1:09 PM
*/
public class CppNamesValidator implements NamesValidator {

  public boolean isKeyword(String s, Project project) {
    return CppTokenTypes.KEYWORDS.contains(tokenType(s, project));

  }

  public boolean isIdentifier(String s, Project project) {
    return tokenType(s, project) == CppTokenTypes.IDENTIFIER;
  }

  private IElementType tokenType(String s, Project project) {
    final Lexer lexer = CppLanguage.createLexerStatic(project);
    lexer.start(s, 0, s.length(), 0);

    final IElementType tokenType = lexer.getTokenType();

    lexer.advance();
    return lexer.getTokenType() == null ? tokenType:null;
  }
}
