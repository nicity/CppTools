// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.makefile;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.tree.IFileElementType;
import com.advancedtools.cpp.CppSupportLoader;

import javax.print.DocFlavor;

/**
 * @author maxim
 */
public interface MakefileTokenTypes {
  IElementType CONTINUE_STATEMENT = new MakefileElementType("CONTINUE_STATEMENT");
  IElementType STATEMENT = new MakefileElementType("STATEMENT");
  IElementType SEMANTIC_WHITESPACE = new MakefileElementType("SEMANTIC_WHITESPACE");

  IElementType WHITE_SPACE = TokenType.WHITE_SPACE;
  IElementType BAD_CHARACTER = TokenType.BAD_CHARACTER;
  IElementType END_OF_LINE_COMMENT = new MakefileElementType("END_OF_LINE_COMMENT");
  IElementType IDENTIFIER = new MakefileElementType("IDENTIFIER");
  IElementType IDENTIFIER_PART = new MakefileElementType("IDENTIFIER");
  IElementType TARGET_IDENTIFIER = new MakefileElementType("TARGET_IDENTIFIER");
  IElementType TARGET_IDENTIFIER_PART = new MakefileElementType("TARGET_IDENTIFIER");
  IElementType TARGET_DECLARATION = new MakefileElementType("TARGET_DECLARATION");
  IElementType TARGET_IDENTIFIER_START = new MakefileElementType("TARGET_IDENTIFIER_START");
  IElementType TARGET_IDENTIFIER_END = new MakefileElementType("TARGET_IDENTIFIER_END");

  IElementType IDENTIFIER_START = new MakefileElementType("IDENTIFIER_START");
  IElementType IDENTIFIER_END = new MakefileElementType("IDENTIFIER_END");
  IElementType TARGETS_END = new MakefileElementType("TARGETS_END");

  IElementType VAR_DEFINITION = new MakefileElementType("VAR_DEFINITION");
  IElementType VAR_REFERENCE = new MakefileElementType("VAR_REFERENCE");
  IElementType VAR_SELECTION_START = new MakefileElementType("VAR_SELECTION_START");
  IElementType VAR_REFERENCE_END = new MakefileElementType("VAR_SELECTION_END");
  IElementType BUILTIN_VAR = new MakefileElementType("BUILTIN_VAR");

  IElementType EQ = new MakefileElementType("EQ");
  IElementType ADD_EQ = new MakefileElementType("ADD_EQ");
  IElementType TEMPLATE_DATA = new MakefileElementType("TEMPLATE_DATA");

  IElementType INCLUDE_KEYWORD = new MakefileElementType("INCLUDE_KEYWORD");
  IElementType ERROR_KEYWORD = new MakefileElementType("ERROR_KEYWORD");
  IElementType ELSE_KEYWORD = new MakefileElementType("ELSE_KEYWORD");
  IElementType IFDEF_KEYWORD = new MakefileElementType("IFDEF_KEYWORD");
  IElementType IFNDEF_KEYWORD = new MakefileElementType("IFNDEF_KEYWORD");
  IElementType IFEQ_KEYWORD = new MakefileElementType("IFEQ_KEYWORD");
  IElementType IFNEQ_KEYWORD = new MakefileElementType("IFNEQ_KEYWORD");
  IElementType ENDIF_KEYWORD = new MakefileElementType("ENDIF_KEYWORD");

  IFileElementType MAKE_FILE = new IFileElementType(CppSupportLoader.MAKEFILE_LANGUAGE);

  TokenSet WHITE_SPACES = TokenSet.create(WHITE_SPACE);
  TokenSet COMMENTS = TokenSet.create(END_OF_LINE_COMMENT);
  TokenSet LITERALS = TokenSet.create();
  TokenSet KEYWORDS = TokenSet.create(INCLUDE_KEYWORD, ELSE_KEYWORD, IFDEF_KEYWORD, IFNDEF_KEYWORD, ENDIF_KEYWORD, ERROR_KEYWORD, IFEQ_KEYWORD, IFNEQ_KEYWORD);
}