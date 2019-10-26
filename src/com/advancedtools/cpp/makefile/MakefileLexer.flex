// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.makefile;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

%%

%{
    public _MakefileLexer() {
      this((java.io.Reader)null);
    }

    public _MakefileLexer(boolean highlightMode) {
      this((java.io.Reader)null);
      isHighlightModeOn = highlightMode;
    }

    boolean isHighlightModeOn = false;
%}

%class _MakefileLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

SIMPLE_SPACE_CHAR=[\ \t\f]
NEWLINE_SPACE_CHAR=(\n | \r | \r\n)
DIGIT=[0-9]
IDENTIFIER= ([:letter:]|_) ([:letter:]|{DIGIT}|_ )*

%state CONTINUE
%state DEF_VALUE
%state VAR_SELECTION
%state VAR_REF
%state TARGETS
%state IDENTIFIER_PART
%state TARGET_IDENTIFIER_PART
%state DEFINITION
%%

<YYINITIAL, VAR_SELECTION, TARGETS, DEFINITION, VAR_REF> {SIMPLE_SPACE_CHAR}+   { return MakefileTokenTypes.WHITE_SPACE; }
<YYINITIAL> "#" [^\r\n]* { return MakefileTokenTypes.END_OF_LINE_COMMENT; }

<YYINITIAL> "ifdef"          {  yybegin(VAR_REF); return MakefileTokenTypes.IFDEF_KEYWORD; }
<YYINITIAL> "ifndef"          {  yybegin(VAR_REF); return MakefileTokenTypes.IFNDEF_KEYWORD; }
<VAR_REF> {IDENTIFIER} { yybegin(YYINITIAL); return MakefileTokenTypes.VAR_REFERENCE; }
<VAR_REF> [^] { yybegin(YYINITIAL); return MakefileTokenTypes.BAD_CHARACTER; }
<YYINITIAL> "ifeq"          {  return MakefileTokenTypes.IFEQ_KEYWORD; }
<YYINITIAL> "ifneq"          {  return MakefileTokenTypes.IFNEQ_KEYWORD; }
<YYINITIAL, DEF_VALUE, TARGET_IDENTIFIER_PART, IDENTIFIER_PART> "$(" {  prevState = zzLexicalState; yybegin(VAR_SELECTION); return MakefileTokenTypes.VAR_SELECTION_START; }
<VAR_SELECTION> {IDENTIFIER} { return MakefileTokenTypes.VAR_REFERENCE; }
<VAR_SELECTION> ")" { yybegin(prevState); prevState = 0; return MakefileTokenTypes.VAR_REFERENCE_END; }
<VAR_SELECTION> [^] { yybegin(prevState); prevState = 0; return MakefileTokenTypes.BAD_CHARACTER; }
<YYINITIAL> "$@"  {  return MakefileTokenTypes.BUILTIN_VAR; }
<YYINITIAL> "$<"  {  return MakefileTokenTypes.BUILTIN_VAR; }
<YYINITIAL> "$^"  {  return MakefileTokenTypes.BUILTIN_VAR; }
<YYINITIAL> "endif"          {  return MakefileTokenTypes.ENDIF_KEYWORD; }
<YYINITIAL> "else"          {  return MakefileTokenTypes.ELSE_KEYWORD; }
<YYINITIAL> "error"          {  return MakefileTokenTypes.ERROR_KEYWORD; }
<YYINITIAL> "include"          {  return MakefileTokenTypes.INCLUDE_KEYWORD; }
<DEF_VALUE> "\\" {NEWLINE_SPACE_CHAR} { yybegin(CONTINUE); yypushback(yylength()); }
<CONTINUE> "\\" { return MakefileTokenTypes.CONTINUE_STATEMENT; }

<CONTINUE> {NEWLINE_SPACE_CHAR} { return MakefileTokenTypes.WHITE_SPACE; }
<YYINITIAL, DEF_VALUE, DEFINITION> {NEWLINE_SPACE_CHAR} { yybegin(YYINITIAL); prevState = 0; return MakefileTokenTypes.WHITE_SPACE; }
<CONTINUE> [^] { yybegin(DEF_VALUE); yypushback(yylength()); }

<YYINITIAL> ^ [^ \n\t]+ ":" {  yybegin(TARGET_IDENTIFIER_PART); yypushback(yylength()); return MakefileTokenTypes.TARGET_IDENTIFIER_START; }
<TARGET_IDENTIFIER_PART> [^ \t\n:\$]+ {  return MakefileTokenTypes.TARGET_IDENTIFIER_PART; }
<TARGET_IDENTIFIER_PART> ":" { yybegin(TARGETS); return MakefileTokenTypes.TARGET_IDENTIFIER_END; }
<TARGETS> [^ \t\n:]+ {  yypushback(yylength()); yybegin(IDENTIFIER_PART); return MakefileTokenTypes.IDENTIFIER_START; }
<IDENTIFIER_PART> ([^ \t\n\$] | ("$"[^\(])) + {  return MakefileTokenTypes.IDENTIFIER_PART; }
<IDENTIFIER_PART> [^] {  yybegin(TARGETS); yypushback(yylength()); return MakefileTokenTypes.IDENTIFIER_END; }
<TARGET_IDENTIFIER_PART, IDENTIFIER_PART> [ \t\n:] { yybegin(TARGETS); }
<TARGETS> "\n" { yybegin(YYINITIAL); yypushback(1); return MakefileTokenTypes.TARGETS_END; }

<YYINITIAL> ^ " "* {IDENTIFIER} " "* ("=" | "+=") {  yypushback(yylength()); yybegin(DEFINITION); }
<DEFINITION> {IDENTIFIER} {  return MakefileTokenTypes.VAR_DEFINITION; }
<DEFINITION> "="          {  yybegin(DEF_VALUE); return MakefileTokenTypes.EQ; }
<DEFINITION> "+="          {  yybegin(DEF_VALUE); return MakefileTokenTypes.ADD_EQ; }
<DEFINITION> [^#\r\n \t\f\\]  { return MakefileTokenTypes.TEMPLATE_DATA; }
<DEF_VALUE> ([^ \n\t\\$]|"$"[^\\(])+ {  return MakefileTokenTypes.TEMPLATE_DATA; }
[^] {  return MakefileTokenTypes.TEMPLATE_DATA; }