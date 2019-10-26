// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

%%

%{
    public _CppLexer(boolean highlightMode, boolean cmode, boolean c99Mode, boolean c11Mode, boolean cpp11Mode) {
      isHighlightModeOn = highlightMode;
      CMode = cmode;
      C99Mode = c99Mode;
      C11Mode = c11Mode;
      Cpp11Mode = cpp11Mode;
    }

    final boolean isHighlightModeOn;
    final boolean CMode, C99Mode, C11Mode, Cpp11Mode;
%}

%class _CppLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

DIGIT=[0-9]
OCTAL_DIGIT=[0-7]
HEX_DIGIT=[0-9A-Fa-f]
SIMPLE_SPACE_CHAR=[\ \t\f]
NEWLINE_SPACE_CHAR=(\n | \r | \r\n)
WHITE_SPACE_CHAR=[\ \n\r\t\f]

IDENTIFIER= ([:letter:]|_) ([:letter:]|{DIGIT}|_ )*

C_STYLE_COMMENT=("/*"[^"*"]{COMMENT_TAIL})|"/*"

COMMENT_TAIL=([^"*"]*("*"+[^"*""/"])?)*("*"+"/")?
DOC_COMMENT="/*""*"+("/"|([^"/""*"]{COMMENT_TAIL}))?
END_OF_LINE_COMMENT="/""/"([^\r\n]|(\\\r?\n))*

INTEGER_LITERAL={DECIMAL_INTEGER_LITERAL}|{HEX_INTEGER_LITERAL}
DECIMAL_INTEGER_LITERAL=(0|([1-9]({DIGIT})*))
HEX_INTEGER_LITERAL=0[Xx]({HEX_DIGIT})*

FLOAT_LITERAL=({FLOATING_POINT_LITERAL1})|({FLOATING_POINT_LITERAL2})|({FLOATING_POINT_LITERAL3})|({FLOATING_POINT_LITERAL4})
FLOATING_POINT_LITERAL1=({DIGIT})+"."({DIGIT})*({EXPONENT_PART})?
FLOATING_POINT_LITERAL2="."({DIGIT})+({EXPONENT_PART})?
FLOATING_POINT_LITERAL3=({DIGIT})+({EXPONENT_PART})
FLOATING_POINT_LITERAL4=({DIGIT})+
EXPONENT_PART=[Ee]["+""-"]?({DIGIT})*

QUOTED_LITERAL="'"([^\\\'\r\n]|{ESCAPE_SEQUENCE})*("'"|\\)?
DOUBLE_QUOTED_LITERAL=\"([^\\\"\r\n]|{ESCAPE_SEQUENCE})*(\"|\\)?
ESCAPE_SEQUENCE=\\[^\r\n]
SIMPLE_PRE_KEYWORD=(include|ifdef|endif|undef|ifndef|error|defined)

%state PRE
%state PRAGMA
%state DEFINE
%state DEFINE_CONTINUATION
%state CONTINUATION
%%

<YYINITIAL> {WHITE_SPACE_CHAR}+   { return CppTokenTypes.WHITE_SPACE; }

{C_STYLE_COMMENT}     { return CppTokenTypes.C_STYLE_COMMENT; }
{DOC_COMMENT}     { return CppTokenTypes.C_STYLE_COMMENT; }
{END_OF_LINE_COMMENT} { if (!CMode) return CppTokenTypes.END_OF_LINE_COMMENT; }

{INTEGER_LITERAL}     { return CppTokenTypes.NUMERIC_LITERAL; }
{FLOAT_LITERAL}       { return CppTokenTypes.NUMERIC_LITERAL; }

{QUOTED_LITERAL}      {
                        return isHighlightModeOn ?
                          CppTokenTypes.SINGLE_QUOTE_STRING_LITERAL:
                          CppTokenTypes.STRING_LITERAL;
                      }

{DOUBLE_QUOTED_LITERAL}      { return CppTokenTypes.STRING_LITERAL; }

<YYINITIAL> "#"         { yybegin(PRE); yypushback(yylength()); }

<PRE,DEFINE> "##"               {
  return CppTokenTypes.PRE_KEYWORD;
}

<PRE,DEFINE> "#"               {
  return CppTokenTypes.PRE_KEYWORD;
}

<PRE,DEFINE> {SIMPLE_PRE_KEYWORD} { return CppTokenTypes.PRE_KEYWORD; }
<PRE>  "define" | "if" | "elif" { yybegin(DEFINE); return CppTokenTypes.PRE_KEYWORD; }
<PRE> "pragma" { yybegin(PRAGMA); return CppTokenTypes.PRE_KEYWORD; }
<PRAGMA> {IDENTIFIER} { return CppTokenTypes.PRE_KEYWORD; }

<PRE, DEFINE, PRAGMA> {SIMPLE_SPACE_CHAR}+ { return CppTokenTypes.WHITE_SPACE; }

<DEFINE> {NEWLINE_SPACE_CHAR} { yybegin(YYINITIAL); yypushback(yylength()); }
<DEFINE> "\\" {NEWLINE_SPACE_CHAR} { yybegin(DEFINE_CONTINUATION); yypushback(yylength()); }
<DEFINE_CONTINUATION> "\\" { return CppTokenTypes.PRE_KEYWORD; }
<DEFINE_CONTINUATION> {NEWLINE_SPACE_CHAR} { yybegin(DEFINE); return CppTokenTypes.WHITE_SPACE; }
<PRE, PRAGMA> {NEWLINE_SPACE_CHAR} { yybegin(YYINITIAL); yypushback(yylength()); }

"true"                { if (!CMode) return CppTokenTypes.TRUE_KEYWORD; }

"class"                { if (!CMode)  return CppTokenTypes.CLASS_KEYWORD; }
"struct"                {  return CppTokenTypes.STRUCT_KEYWORD; }
"union"                {  return CppTokenTypes.UNION_KEYWORD; }
"goto"                {  return CppTokenTypes.GOTO_KEYWORD; }
"enum"                {  return CppTokenTypes.ENUM_KEYWORD; }
"public"                { if (!CMode)  return CppTokenTypes.PUBLIC_KEYWORD; }
"protected"                { if (!CMode)  return CppTokenTypes.PROTECTED_KEYWORD; }
"template"                {  if (!CMode) return CppTokenTypes.TEMPLATE_KEYWORD; }
"typename"                {  if (!CMode) return CppTokenTypes.TYPENAME_KEYWORD; }
"explicit"                {  if (!CMode) return CppTokenTypes.EXPLICIT_KEYWORD; }
"sizeof"                {  if (!CMode) return CppTokenTypes.SIZEOF_KEYWORD; }
"mutable"                {  if (!CMode) return CppTokenTypes.MUTABLE_KEYWORD; }
"export"                {  if (!CMode) return CppTokenTypes.EXPORT_KEYWORD; }
"private"                {  if (!CMode) return CppTokenTypes.PRIVATE_KEYWORD; }
"friend"                {  if (!CMode) return CppTokenTypes.FRIEND_KEYWORD; }
"using"                {  if (!CMode) return CppTokenTypes.USING_KEYWORD; }
"namespace"                {  if (!CMode) return CppTokenTypes.NAMESPACE_KEYWORD; }

"false"               { if (!CMode) return CppTokenTypes.FALSE_KEYWORD; }
"bool"                { if (!CMode)  return CppTokenTypes.BOOL_KEYWORD; }

"break"               {  return CppTokenTypes.BREAK_KEYWORD; }
"float"               {  return CppTokenTypes.FLOAT_KEYWORD; }
"double"               {  return CppTokenTypes.DOUBLE_KEYWORD; }
"case"                {  return CppTokenTypes.CASE_KEYWORD; }
"catch"               { if (!CMode)  return CppTokenTypes.CATCH_KEYWORD; }
"char"               {  return CppTokenTypes.CHAR_KEYWORD; }
"int"               {  return CppTokenTypes.INT_KEYWORD; }
"long"               {  return CppTokenTypes.LONG_KEYWORD; }
"unsigned"               {  return CppTokenTypes.UNSIGNED_KEYWORD; }
"signed"               {  return CppTokenTypes.SIGNED_KEYWORD; }
"short"               {  return CppTokenTypes.SHORT_KEYWORD; }
"wchar_t"               { if (!CMode)  return CppTokenTypes.WCHART_KEYWORD; }
"const"               {  return CppTokenTypes.CONST_KEYWORD; }
"volatile"               {  return CppTokenTypes.VOLATILE_KEYWORD; }
"continue"            {  return CppTokenTypes.CONTINUE_KEYWORD; }
"default"             {  return CppTokenTypes.DEFAULT_KEYWORD; }
"delete"              { if (!CMode)  return CppTokenTypes.DELETE_KEYWORD; }
"do"                  {  return CppTokenTypes.DO_KEYWORD; }
"else"                {  return CppTokenTypes.ELSE_KEYWORD; }
"for"                 {  return CppTokenTypes.FOR_KEYWORD; }
"if"                  {  return CppTokenTypes.IF_KEYWORD; }

"new"                 { if (!CMode)  return CppTokenTypes.NEW_KEYWORD; }

"return"              {  return CppTokenTypes.RETURN_KEYWORD; }
"operator"            { if (!CMode)  return CppTokenTypes.OPERATOR_KEYWORD; }
"const_cast"            { if (!CMode)  return CppTokenTypes.CONST_CAST_KEYWORD; }
"static_cast"            { if (!CMode)  return CppTokenTypes.STATIC_CAST_KEYWORD; }
"dynamic_cast"            { if (!CMode)  return CppTokenTypes.DYNAMIC_CAST_KEYWORD; }
"reinterpret_cast"            { if (!CMode)  return CppTokenTypes.REINTERPRET_CAST_KEYWORD; }
"typeid"            { if (!CMode)  return CppTokenTypes.TYPEID_KEYWORD; }
"asm"               { if (!CMode)  return CppTokenTypes.ASM_KEYWORD; }
"auto"               { return CppTokenTypes.AUTO_KEYWORD; }
"register"               { return CppTokenTypes.REGISTER_KEYWORD; }

"static"              {  return CppTokenTypes.STATIC_KEYWORD; }
"switch"              {  return CppTokenTypes.SWITCH_KEYWORD; }
"this"                { if (!CMode)  return CppTokenTypes.THIS_KEYWORD; }
"throw"               { if (!CMode)  return CppTokenTypes.THROW_KEYWORD; }
"try"                 { if (!CMode) return CppTokenTypes.TRY_KEYWORD; }

"virtual"             { if (!CMode) return CppTokenTypes.VIRTUAL_KEYWORD; }
"extern"             {  return CppTokenTypes.EXTERN_KEYWORD; }
"typedef"             {  return CppTokenTypes.TYPEDEF_KEYWORD; }

"inline"             {  if (!CMode || C99Mode) return CppTokenTypes.INLINE_KEYWORD; }
"alignas"             {  if (Cpp11Mode) return CppTokenTypes.ALIGNAS_KEYWORD; }
"alignof"             {  if (Cpp11Mode) return CppTokenTypes.ALIGNOF_KEYWORD; }
"char16_t"             {  if (Cpp11Mode) return CppTokenTypes.CHAR16T_KEYWORD; }
"char32_t"             {  if (Cpp11Mode) return CppTokenTypes.CHAR32T_KEYWORD; }
"constexpr"             {  if (Cpp11Mode) return CppTokenTypes.CONSTEXPR_KEYWORD; }
"decltype"             {  if (Cpp11Mode) return CppTokenTypes.DECLTYPE_KEYWORD; }
"noexcept"             {  if (Cpp11Mode) return CppTokenTypes.NOEXCEPT_KEYWORD; }
"static_assert"             {  if (Cpp11Mode) return CppTokenTypes.STATIC_ASSERT_KEYWORD; }
"thread_local"             {  if (Cpp11Mode) return CppTokenTypes.THREAD_LOCAL_KEYWORD; }
"nullptr"             {  if (Cpp11Mode) return CppTokenTypes.NULLPTR_KEYWORD; }

"_Bool"             {  if (C99Mode) return CppTokenTypes.BOOL_KEYWORD; }
"_Complex"             {  if (C99Mode) return CppTokenTypes.COMPLEX_KEYWORD; }
"_Imaginary"             {  if (C99Mode) return CppTokenTypes.IMAGINARY_KEYWORD; }
"_Alignas"             {  if (C11Mode) return CppTokenTypes.ALIGNAS_KEYWORD; }
"_Alignof"             {  if (C11Mode) return CppTokenTypes.ALIGNOF_KEYWORD; }
"_Atomic"             {  if (C11Mode) return CppTokenTypes.ATOMIC_KEYWORD; }
"_Generic"             {  if (C11Mode) return CppTokenTypes.GENERIC_KEYWORD; }
"_Noreturn"             {  if (C11Mode) return CppTokenTypes.NORETURN_KEYWORD; }
"_Static_assert"             {  if (C11Mode) return CppTokenTypes.STATIC_ASSERT_KEYWORD; }
"_Thread_local"             {  if (C11Mode) return CppTokenTypes.THREAD_LOCAL_KEYWORD; }

"restrict"             {  if (C99Mode) return CppTokenTypes.RESTRICT_KEYWORD; }
"void"                {  return CppTokenTypes.VOID_KEYWORD; }
"while"               {  return CppTokenTypes.WHILE_KEYWORD; }

"++"                  {  return CppTokenTypes.PLUSPLUS; }
"--"                  {  return CppTokenTypes.MINUSMINUS; }

"=="                  {  return CppTokenTypes.EQEQ; }
"!="                  {  return CppTokenTypes.NE; }
"<"                   {  return CppTokenTypes.LT; }

">"                   {  return CppTokenTypes.GT; }
"<="                  {  return CppTokenTypes.LE; }
">="                  {  return CppTokenTypes.GE; }
"<<"                  {  return CppTokenTypes.LTLT; }
">>"                  {  return CppTokenTypes.GTGT; }

"&"                   {  return CppTokenTypes.AND; }
"&&"                  {  return CppTokenTypes.ANDAND; }
"|"                   {  return CppTokenTypes.OR; }
"||"                  {  return CppTokenTypes.OROR; }
"::"                  {  return CppTokenTypes.QUAL; }

"+="                  {  return CppTokenTypes.PLUSEQ; }
"-="                  {  return CppTokenTypes.MINUSEQ; }
"*="                  {  return CppTokenTypes.MULTEQ; }
"/="                  {  return CppTokenTypes.DIVEQ; }
"&="                  {  return CppTokenTypes.ANDEQ; }
"|="                  {  return CppTokenTypes.OREQ; }
"^="                  {  return CppTokenTypes.XOREQ; }
"%="                  {  return CppTokenTypes.PERCEQ; }
"<<="                 {  return CppTokenTypes.LTLTEQ; }
">>="                 {  return CppTokenTypes.GTGTEQ; }

"("                   {  return CppTokenTypes.LPAR; }
")"                   {  return CppTokenTypes.RPAR; }
"{"                   {  return CppTokenTypes.LBRACE; }
"}"                   {  return CppTokenTypes.RBRACE; }
"[]"                   {  return CppTokenTypes.DOUBLE_BRACKET; }
"["                   {  return CppTokenTypes.LBRACKET; }
"]"                   {  return CppTokenTypes.RBRACKET; }
";"                   {  return CppTokenTypes.SEMICOLON; }
","                   {  return CppTokenTypes.COMMA; }
".*"                   {  return CppTokenTypes.MEMBER_DOT; }
"->*"                   {  return CppTokenTypes.MEMBER_ARROW; }
"."                   {  return CppTokenTypes.DOT; }
"->"                   {  return CppTokenTypes.ARROW; }

"="                   {  return CppTokenTypes.EQ; }
"!"                   {  return CppTokenTypes.EXCL; }
"~"                   {  return CppTokenTypes.TILDE; }
"?"                   {  return CppTokenTypes.QUEST; }
":"                   {  return CppTokenTypes.COLON; }
"+"                   {  return CppTokenTypes.PLUS; }
"-"                   {  return CppTokenTypes.MINUS; }
"*"                   {  return CppTokenTypes.MULT; }
"/"                   {  return CppTokenTypes.DIV; }
"^"                   {  return CppTokenTypes.XOR; }
"%"                   {  return CppTokenTypes.PERC; }
<YYINITIAL> "\\" {NEWLINE_SPACE_CHAR} { yybegin(CONTINUATION); yypushback(yylength()); }
<CONTINUATION> "\\" { yybegin(YYINITIAL); return CppTokenTypes.ESCAPING_SLASH; }

{IDENTIFIER}          {  return CppTokenTypes.IDENTIFIER; }

<PRE, DEFINE, PRAGMA> [^] {
  yybegin(YYINITIAL);
  yypushback(yylength());
}

<YYINITIAL> [^]                     {  return CppTokenTypes.BAD_CHARACTER; }