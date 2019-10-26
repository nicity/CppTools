// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.TokenType;

/**
 * @author maxim
 * Date: 21.09.2006
 * Time: 5:28:30
 */
public interface CppTokenTypes {
  IElementType BLOCK = new CppElementType("BLOCK");
  IElementType PARENS = new CppElementType("PARENS");
  IElementType STATEMENT = new CppElementType("STATEMENT");

  IElementType IDENTIFIER = new CppElementType("IDENTIFIER");
  IElementType WHITE_SPACE = TokenType.WHITE_SPACE;

  IElementType BAD_CHARACTER = TokenType.BAD_CHARACTER;

  IElementType END_OF_LINE_COMMENT = new CppElementType("END_OF_LINE_COMMENT");
  IElementType C_STYLE_COMMENT = new CppElementType("C_STYLE_COMMENT");

  // Keywords:
  IElementType BREAK_KEYWORD = new CppElementType("BREAK_KEYWORD");
  IElementType STATIC_KEYWORD = new CppElementType("STATIC_KEYWORD");
  IElementType VIRTUAL_KEYWORD = new CppElementType("VIRTUAL_KEYWORD");
  IElementType EXTERN_KEYWORD = new CppElementType("EXTERN_KEYWORD");
  IElementType TEMPLATE_KEYWORD = new CppElementType("TEMPLATE_KEYWORD");
  IElementType TYPENAME_KEYWORD = new CppElementType("TYPENAME_KEYWORD");
  IElementType EXPLICIT_KEYWORD = new CppElementType("EXPLICIT_KEYWORD");
  IElementType MUTABLE_KEYWORD = new CppElementType("MUTABLE_KEYWORD");

  IElementType INLINE_KEYWORD = new CppElementType("INLINE_KEYWORD");
  IElementType PRE_KEYWORD = new CppElementType("PRE_KEYWORD");
  IElementType CASE_KEYWORD = new CppElementType("CASE_KEYWORD");
  IElementType CATCH_KEYWORD = new CppElementType("CATCH_KEYWORD");
  IElementType CHAR_KEYWORD = new CppElementType("CHAR_KEYWORD");
  IElementType OPERATOR_KEYWORD = new CppElementType("OPERATOR_KEYWORD");

  IElementType CONST_KEYWORD = new CppElementType("CONST_KEYWORD");
  IElementType TYPEDEF_KEYWORD = new CppElementType("TYPEDEF_KEYWORD");
  IElementType QUAL = new CppElementType("::");
  IElementType CONTINUE_KEYWORD = new CppElementType("CONTINUE_KEYWORD");
  IElementType DELETE_KEYWORD = new CppElementType("DELETE_KEYWORD");
  IElementType DEFAULT_KEYWORD = new CppElementType("DEFAULT_KEYWORD");
  IElementType DO_KEYWORD = new CppElementType("DO_KEYWORD");
  IElementType ELSE_KEYWORD = new CppElementType("ELSE_KEYWORD");

  IElementType FOR_KEYWORD = new CppElementType("FOR_KEYWORD");
  IElementType IF_KEYWORD = new CppElementType("IF_KEYWORD");

  IElementType NEW_KEYWORD = new CppElementType("NEW_KEYWORD");
  IElementType RETURN_KEYWORD = new CppElementType("RETURN_KEYWORD");
  IElementType SWITCH_KEYWORD = new CppElementType("SWITCH_KEYWORD");
  IElementType THIS_KEYWORD = new CppElementType("THIS_KEYWORD");
  IElementType THROW_KEYWORD = new CppElementType("THROW_KEYWORD");
  IElementType TRY_KEYWORD = new CppElementType("TRY_KEYWORD");

  IElementType VOID_KEYWORD = new CppElementType("VOID_KEYWORD");
  IElementType WHILE_KEYWORD = new CppElementType("WHILE_KEYWORD");
  IElementType CONSTEXPR_KEYWORD = new CppElementType("CONSTEXPR_KEYWORD");
  IElementType NULLPTR_KEYWORD = new CppElementType("NULLPTR_KEYWORD");
  IElementType STATIC_ASSERT_KEYWORD = new CppElementType("STATIC_ASSERT_KEYWORD");

  // Hardcoded literals
  IElementType TRUE_KEYWORD = new CppElementType("TRUE_KEYWORD");
  IElementType FALSE_KEYWORD = new CppElementType("FALSE_KEYWORD");

  // Literals
  IElementType NUMERIC_LITERAL = new CppElementType("NUMERIC_LITERAL");
  IElementType STRING_LITERAL = new CppElementType("STRING_LITERAL");
  IElementType SINGLE_QUOTE_STRING_LITERAL = new CppElementType("SINGLE_QUOTE_STRING_LITERAL");

  // Types
  IElementType INT_KEYWORD = new CppElementType("INT_KEYWORD");
  IElementType LONG_KEYWORD = new CppElementType("LONG_KEYWORD");
  IElementType UNSIGNED_KEYWORD = new CppElementType("UNSIGNED_KEYWORD");

  IElementType WCHART_KEYWORD = new CppElementType("WCHAR_T_KEYWORD");
  IElementType BOOL_KEYWORD = new CppElementType("BOOL_KEYWORD");
  IElementType CHAR16T_KEYWORD = new CppElementType("CHAR16T_KEYWORD");
  IElementType CHAR32T_KEYWORD = new CppElementType("CHAR32T_KEYWORD");
  IElementType COMPLEX_KEYWORD = new CppElementType("COMPLEX_KEYWORD");
  IElementType IMAGINARY_KEYWORD = new CppElementType("IMAGINARY_KEYWORD");
  IElementType ATOMIC_KEYWORD = new CppElementType("ATOMIC_KEYWORD");
  IElementType GENERIC_KEYWORD = new CppElementType("GENERIC_KEYWORD");

  // Spec
  IElementType ALIGNOF_KEYWORD = new CppElementType("ALIGNOF_KEYWORD");
  IElementType ALIGNAS_KEYWORD = new CppElementType("ALIGNAS_KEYWORD");
  IElementType THREAD_LOCAL_KEYWORD = new CppElementType("THREAD_LOCAL_KEYWORD");
  IElementType DECLTYPE_KEYWORD = new CppElementType("DECLTYPE_KEYWORD");
  IElementType NOEXCEPT_KEYWORD = new CppElementType("NOEXCEPT_KEYWORD");
  IElementType NORETURN_KEYWORD = new CppElementType("NORETURN_KEYWORD");
  IElementType RESTRICT_KEYWORD = new CppElementType("RESTRICT_KEYWORD");

  // Punctuators
  IElementType LBRACE = new CppElementType("LBRACE");// {
  IElementType RBRACE = new CppElementType("RBRACE");// }
  IElementType LPAR = new CppElementType("LPAR");// (
  IElementType RPAR = new CppElementType("RPAR");// )
  IElementType LBRACKET = new CppElementType("LBRACKET");// [
  IElementType DOUBLE_BRACKET = new CppElementType("LBRACKET");// [
  IElementType RBRACKET = new CppElementType("RBRACKET");// ]
  IElementType DOT = new CppElementType("DOT");// .
  IElementType MEMBER_DOT = new CppElementType("MEMBER_DOT");// .*
  IElementType SEMICOLON = new CppElementType("SEMICOLON");// ;
  IElementType COMMA = new CppElementType("COMMA");// ,

  IElementType LT = new CppElementType("LT");// <
  IElementType GT = new CppElementType("GT");// >
  IElementType LE = new CppElementType("LE");// <=
  IElementType GE = new CppElementType("GE");// >=
  IElementType EQEQ = new CppElementType("EQEQ");// ==
  IElementType NE = new CppElementType("NE");// !=

  IElementType PLUS = new CppElementType("PLUS");// +
  IElementType MINUS = new CppElementType("MINUS");// -
  IElementType MULT = new CppElementType("MULT");// *
  IElementType PERC = new CppElementType("PERC");// %
  IElementType PLUSPLUS = new CppElementType("PLUSPLUS");// ++
  IElementType MINUSMINUS = new CppElementType("MINUSMINUS");// --
  IElementType LTLT = new CppElementType("LTLT");// <<
  IElementType GTGT = new CppElementType("GTGT");// >>

  IElementType AND = new CppElementType("AND");// &
  IElementType OR = new CppElementType("OR");// |
  IElementType XOR = new CppElementType("XOR");// ^
  IElementType EXCL = new CppElementType("EXCL");// !
  IElementType TILDE = new CppElementType("TILDE");// ~
  IElementType ANDAND = new CppElementType("ANDAND");// &&
  IElementType OROR = new CppElementType("OROR");// ||
  IElementType QUEST = new CppElementType("QUEST");// ?
  IElementType COLON = new CppElementType("COLON");// :
  IElementType EQ = new CppElementType("EQ");// =
  IElementType PLUSEQ = new CppElementType("PLUSEQ");// +=
  IElementType MINUSEQ = new CppElementType("MINUSEQ");// -=
  IElementType MULTEQ = new CppElementType("MULTEQ");// *=
  IElementType PERCEQ = new CppElementType("PERCEQ");// %=
  IElementType LTLTEQ = new CppElementType("LTLTEQ");// <<=
  IElementType GTGTEQ = new CppElementType("GTGTEQ");// >>=
  IElementType ANDEQ = new CppElementType("ANDEQ");// &=
  IElementType OREQ = new CppElementType("OREQ");// |=
  IElementType XOREQ = new CppElementType("XOREQ");// ^=
  IElementType DIV = new CppElementType("DIV"); // /
  IElementType DIVEQ = new CppElementType("DIVEQ"); // /=

  IElementType ARROW = new CppElementType("ARROW"); // ->
  IElementType MEMBER_ARROW = new CppElementType("MEMBER_ARROW"); // ->*
  IElementType PUBLIC_KEYWORD = new CppElementType("PUBLIC_KEYWORD"); // ->
  IElementType PROTECTED_KEYWORD = new CppElementType("PROTECTED_KEYWORD"); // ->
  IElementType PRIVATE_KEYWORD = new CppElementType("PRIVATE_KEYWORD"); // ->
  IElementType FRIEND_KEYWORD = new CppElementType("FRIEND_KEYWORD"); // ->
  IElementType STRUCT_KEYWORD = new CppElementType("STRUCT_KEYWORD"); // ->
  IElementType CLASS_KEYWORD = new CppElementType("CLASS_KEYWORD"); // ->
  IElementType ENUM_KEYWORD = new CppElementType("ENUM_KEYWORD"); // ->
  IElementType USING_KEYWORD = new CppElementType("USING_KEYWORD"); // ->
  IElementType NAMESPACE_KEYWORD = new CppElementType("NAMESPACE_KEYWORD"); // ->
  IElementType SIZEOF_KEYWORD = new CppElementType("NAMESPACE_KEYWORD"); // ->
  IElementType EXPORT_KEYWORD = new CppElementType("EXPORT_KEYWORD"); // ->

  IElementType REINTERPRET_CAST_KEYWORD = new CppElementType("reinterpret_cast_keyword");
  IElementType CONST_CAST_KEYWORD = new CppElementType("const_cast_keyword");
  IElementType STATIC_CAST_KEYWORD = new CppElementType("static_cast_keyword");
  IElementType DYNAMIC_CAST_KEYWORD = new CppElementType("dynamic_cast_keyword");

  IElementType ASM_KEYWORD = new CppElementType("ASM_KEYWORD");
  IElementType TYPEID_KEYWORD = new CppElementType("TYPEID_KEYWORD");

  IElementType REGISTER_KEYWORD = new CppElementType("register_keyword");
  IElementType AUTO_KEYWORD = new CppElementType("auto_keyword");
  IElementType DOUBLE_KEYWORD = new CppElementType("double_keyword");
  IElementType SHORT_KEYWORD = new CppElementType("short_keyword");
  IElementType FLOAT_KEYWORD = new CppElementType("float_keyword");
  IElementType VOLATILE_KEYWORD = new CppElementType("volatile_keyword");
  IElementType GOTO_KEYWORD = new CppElementType("goto_keyword");
  IElementType SIGNED_KEYWORD = new CppElementType("signed_keyword");
  IElementType UNION_KEYWORD = new CppElementType("union_keyword");
  IElementType ESCAPING_SLASH = new CppElementType("escaping_slash");

  TokenSet C_KEYWORDS = TokenSet.create(
    BREAK_KEYWORD, SIZEOF_KEYWORD, CASE_KEYWORD, CONST_KEYWORD, CONTINUE_KEYWORD,  DEFAULT_KEYWORD,
    DO_KEYWORD, ELSE_KEYWORD, FOR_KEYWORD, IF_KEYWORD,  RETURN_KEYWORD, SWITCH_KEYWORD,
    VOID_KEYWORD, WHILE_KEYWORD, STATIC_KEYWORD, EXTERN_KEYWORD,
    CHAR_KEYWORD, INT_KEYWORD, LONG_KEYWORD, UNSIGNED_KEYWORD, UNION_KEYWORD,
    STRUCT_KEYWORD, ENUM_KEYWORD, TYPEDEF_KEYWORD, REGISTER_KEYWORD, AUTO_KEYWORD,
    DOUBLE_KEYWORD, FLOAT_KEYWORD, SHORT_KEYWORD, SIGNED_KEYWORD, VOLATILE_KEYWORD, GOTO_KEYWORD, RESTRICT_KEYWORD,
    INLINE_KEYWORD, ALIGNAS_KEYWORD, ALIGNOF_KEYWORD, BOOL_KEYWORD, COMPLEX_KEYWORD, IMAGINARY_KEYWORD, ATOMIC_KEYWORD,
    GENERIC_KEYWORD, NORETURN_KEYWORD, STATIC_ASSERT_KEYWORD, THREAD_LOCAL_KEYWORD
  );

  TokenSet CPP_KEYWORDS = TokenSet.create(
    CATCH_KEYWORD, DELETE_KEYWORD, NEW_KEYWORD, THIS_KEYWORD,
    THROW_KEYWORD, TRY_KEYWORD, TRUE_KEYWORD, FALSE_KEYWORD,
    VIRTUAL_KEYWORD, TEMPLATE_KEYWORD, TYPENAME_KEYWORD, EXPLICIT_KEYWORD, MUTABLE_KEYWORD,
    INLINE_KEYWORD, EXPORT_KEYWORD, WCHART_KEYWORD,
    PUBLIC_KEYWORD, PROTECTED_KEYWORD, PRIVATE_KEYWORD, FRIEND_KEYWORD, CLASS_KEYWORD,
    USING_KEYWORD, NAMESPACE_KEYWORD, BOOL_KEYWORD, OPERATOR_KEYWORD, CONST_CAST_KEYWORD, STATIC_CAST_KEYWORD,
    DYNAMIC_CAST_KEYWORD, REINTERPRET_CAST_KEYWORD, ASM_KEYWORD, TYPEID_KEYWORD, ALIGNOF_KEYWORD, ALIGNAS_KEYWORD,
    CHAR16T_KEYWORD, CHAR32T_KEYWORD, CONSTEXPR_KEYWORD, DECLTYPE_KEYWORD, NOEXCEPT_KEYWORD, STATIC_ASSERT_KEYWORD,
    THREAD_LOCAL_KEYWORD, NULLPTR_KEYWORD
  );
  TokenSet KEYWORDS = TokenSet.orSet(C_KEYWORDS,CPP_KEYWORDS);

  TokenSet WHITE_SPACES = TokenSet.create(WHITE_SPACE);
  TokenSet COMMENTS = TokenSet.create(C_STYLE_COMMENT, END_OF_LINE_COMMENT);
  TokenSet LITERALS = TokenSet.create(STRING_LITERAL, SINGLE_QUOTE_STRING_LITERAL);

  TokenSet OPERATIONS = TokenSet.create(
    LT, GT, LE, GE, EQEQ, NE, PLUS, MINUS, MULT, PERC, PLUSPLUS, MINUSMINUS, LTLT, GTGT, AND, OR,
    XOR, EXCL, TILDE, ANDAND, OROR, QUEST, COLON, EQ, PLUSEQ, MINUSEQ, MULTEQ, PERCEQ, LTLTEQ, GTGTEQ, ANDEQ,
    OREQ, XOREQ, DIV, DIVEQ, COMMA, QUAL
  );

  TokenSet OVERRIDABLE_OPERATIONS = TokenSet.create(
    LT, GT, LE, GE, EQEQ, NE, PLUS, MINUS, MULT, PERC, PLUSPLUS, MINUSMINUS, LTLT, GTGT, AND, OR,
    XOR, EXCL, ANDAND, OROR, EQ, PLUSEQ, MINUSEQ, MULTEQ, PERCEQ, LTLTEQ, GTGTEQ, ANDEQ,
    OREQ, XOREQ, DIV, DIVEQ, MEMBER_ARROW, MEMBER_DOT, LBRACKET, RBRACKET, LPAR, RPAR, ARROW, OPERATOR_KEYWORD,
    TILDE
  );
}
