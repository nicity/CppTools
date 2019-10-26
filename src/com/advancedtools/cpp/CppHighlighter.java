// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.StringLiteralLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.StringEscapesTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.HashMap;
import java.awt.*;

/**
 * @author maxim
 * Date: 21.09.2006
 * Time: 5:17:25
 */
public class CppHighlighter extends SyntaxHighlighterBase {
  private static Map<IElementType, TextAttributesKey> keys1;
  private static Map<IElementType, TextAttributesKey> keys2;

  public static final TextAttributesKey CPP_KEYWORD = TextAttributesKey.createTextAttributesKey(
    "CPP.KEYWORD",
    SyntaxHighlighterColors.KEYWORD.getDefaultAttributes()
  );

  public static final TextAttributesKey C_KEYWORD = TextAttributesKey.createTextAttributesKey(
    "C.KEYWORD",
    SyntaxHighlighterColors.KEYWORD.getDefaultAttributes()
  );

  public static final TextAttributesKey PRE_KEYWORD = TextAttributesKey.createTextAttributesKey(
    "PRE.KEYWORD",
    SyntaxHighlighterColors.KEYWORD.getDefaultAttributes()
  );

  static final TextAttributesKey CPP_LINE_COMMENT = TextAttributesKey.createTextAttributesKey(
    "CPP.LINE_COMMENT",
    SyntaxHighlighterColors.LINE_COMMENT.getDefaultAttributes()
  );

  static final TextAttributesKey CPP_BLOCK_COMMENT = TextAttributesKey.createTextAttributesKey(
    "CPP.BLOCK_COMMENT",
    SyntaxHighlighterColors.JAVA_BLOCK_COMMENT.getDefaultAttributes()
  );
  static final TextAttributesKey CPP_STRING = TextAttributesKey.createTextAttributesKey(
    "CPP.STRING",
    SyntaxHighlighterColors.STRING.getDefaultAttributes()
  );

  static final TextAttributesKey CPP_NUMBER = TextAttributesKey.createTextAttributesKey(
    "CPP.NUMBER",
    SyntaxHighlighterColors.NUMBER.getDefaultAttributes()
  );

  static final TextAttributesKey CPP_OPERATION_SIGN = TextAttributesKey.createTextAttributesKey(
    "CPP.OPERATION_SIGN",
    SyntaxHighlighterColors.OPERATION_SIGN.getDefaultAttributes()
  );

  static final TextAttributesKey CPP_PARENTHS = TextAttributesKey.createTextAttributesKey(
    "CPP.PARENTHS",
    SyntaxHighlighterColors.PARENTHS.getDefaultAttributes()
  );

  static final TextAttributesKey CPP_BRACKETS = TextAttributesKey.createTextAttributesKey(
    "CPP.BRACKETS",
    SyntaxHighlighterColors.BRACKETS.getDefaultAttributes()
  );

  static final TextAttributesKey CPP_BRACES = TextAttributesKey.createTextAttributesKey(
    "CPP.BRACES",
    SyntaxHighlighterColors.BRACES.getDefaultAttributes()
  );

  static final TextAttributesKey CPP_COMMA = TextAttributesKey.createTextAttributesKey(
    "CPP.COMMA",
    SyntaxHighlighterColors.COMMA.getDefaultAttributes()
  );

  static final TextAttributesKey CPP_DOT = TextAttributesKey.createTextAttributesKey(
    "CPP.DOT",
    SyntaxHighlighterColors.DOT.getDefaultAttributes()
  );

  static final TextAttributesKey CPP_SEMICOLON = TextAttributesKey.createTextAttributesKey(
    "CPP.SEMICOLON",
    SyntaxHighlighterColors.JAVA_SEMICOLON.getDefaultAttributes()
  );

  public static final TextAttributesKey CPP_UNUSED = TextAttributesKey.createTextAttributesKey(
    "CPP.UNUSED",
    createUnusedAttributes()
  );

   public static final TextAttributesKey CPP_NAMESPACE = TextAttributesKey.createTextAttributesKey(
    "CPP.NAMESPACE",
    createNamespaceAttributes()
  );

  public static final TextAttributesKey CPP_FUNCTION = TextAttributesKey.createTextAttributesKey(
    "CPP.FUNCTION",
    createFunctionAttributes()
  );

  public static final TextAttributesKey CPP_STATIC_FUNCTION = TextAttributesKey.createTextAttributesKey(
    "CPP.STATIC_FUNCTION",
    createStaticFunctionAttributes()
  );

  public static final TextAttributesKey CPP_STATIC = TextAttributesKey.createTextAttributesKey(
    "CPP.STATIC",
    createStaticAttributes()
  );

  public static final TextAttributesKey CPP_FIELD = TextAttributesKey.createTextAttributesKey(
    "CPP.FIELD",
    createFieldAttributes()
  );

  public static final TextAttributesKey CPP_METHOD = TextAttributesKey.createTextAttributesKey(
    "CPP.METHOD",
    createMethodAttributes()
  );

  public static final TextAttributesKey CPP_PARAMETER = TextAttributesKey.createTextAttributesKey(
    "CPP.PARAMETER",
    createParameterAttributes()
  );

  public static final TextAttributesKey CPP_TYPE = TextAttributesKey.createTextAttributesKey(
    "CPP.TYPE",
    createTypeAttributes()
  );

  public static final TextAttributesKey CPP_MACROS = TextAttributesKey.createTextAttributesKey(
    "CPP.MACROS",
    createMacrosAttributes()
  );

  public static final TextAttributesKey CPP_LABEL = TextAttributesKey.createTextAttributesKey(
    "CPP.LABEL",
    createLabelAttributes()
  );

  public static final TextAttributesKey CPP_PP_ARG = TextAttributesKey.createTextAttributesKey(
    "CPP.PP_ARG",
    createPPArgAttributes()
  );

  public static final TextAttributesKey CPP_PP_SKIPPED = TextAttributesKey.createTextAttributesKey(
    "CPP.PP_SKIPPED",
    createPPSkippedAttributes()
  );

  public static final TextAttributesKey CPP_CONSTANT = TextAttributesKey.createTextAttributesKey(
    "CPP.CONSTANT",
    createConstantAttributes()
  );

  private static TextAttributes createPPArgAttributes() {
    TextAttributes attrs = new TextAttributes();
    attrs.setForegroundColor(new Color(0x80, 0, 0));
    return attrs;
  }

  private static TextAttributes createPPSkippedAttributes() {
    TextAttributes attrs = new TextAttributes();
    attrs.setForegroundColor(new Color(0x80, 0x80, 0x80));
    attrs.setFontType(Font.ITALIC);
    return attrs;
  }

  private static TextAttributes createLabelAttributes() {
    TextAttributes attrs = new TextAttributes();
    attrs.setForegroundColor(Color.magenta);
    return attrs;
  }

  private static TextAttributes createConstantAttributes() {
    TextAttributes attrs = new TextAttributes();

    attrs.setForegroundColor(Color.pink.darker().darker());
    attrs.setFontType(Font.ITALIC);
    return attrs;
  }

  private static TextAttributes createTypeAttributes() {
    TextAttributes attrs = new TextAttributes();
    attrs.setForegroundColor(Color.green.darker().darker());
    return attrs;
  }

  private static TextAttributes createMacrosAttributes() {
    TextAttributes attrs = new TextAttributes();
    attrs.setForegroundColor(new Color(0xa5, 0x2a, 0x2a));
    return attrs;
  }

  private static TextAttributes createFunctionAttributes() {
    TextAttributes attrs = new TextAttributes();
    attrs.setForegroundColor(Color.blue);
    return attrs;
  }

  private static TextAttributes createStaticFunctionAttributes() {
    TextAttributes attrs = createFunctionAttributes();
    attrs.setFontType(Font.BOLD | Font.ITALIC);
    return attrs;
  }

  private static TextAttributes createMethodAttributes() {
    TextAttributes attrs = createFunctionAttributes();
    attrs.setFontType(Font.ITALIC);
    return attrs;
  }

  private static TextAttributes createStaticAttributes() {
    TextAttributes attrs = new TextAttributes();
    attrs.setForegroundColor(Color.black.darker().darker());
    attrs.setFontType(Font.BOLD | Font.ITALIC);
    return attrs;
  }

  private static TextAttributes createFieldAttributes() {
    TextAttributes attrs = new TextAttributes();
    attrs.setFontType(Font.ITALIC);
    return attrs;
  }

  private static TextAttributes createParameterAttributes() {
    TextAttributes attrs = new TextAttributes();
    attrs.setEffectType(EffectType.LINE_UNDERSCORE);
    attrs.setEffectColor(Color.black);
    return attrs;
  }

  private static TextAttributes createNamespaceAttributes() {
    TextAttributes attrs = new TextAttributes();
    attrs.setForegroundColor(new Color(0x90, 0, 0x90));
    attrs.setFontType(Font.BOLD);
    return attrs;
  }

  private static TextAttributes createUnusedAttributes() {
    TextAttributes attrs = new TextAttributes();
    attrs.setForegroundColor(Color.darkGray);
    attrs.setEffectType(EffectType.STRIKEOUT);

    return attrs;
  }

  static final TextAttributesKey CPP_VALID_STRING_ESCAPE = TextAttributesKey.createTextAttributesKey(
                                                    "CPP.VALID_STRING_ESCAPE",
                                                    SyntaxHighlighterColors.VALID_STRING_ESCAPE.getDefaultAttributes()
                                                  );
  static final TextAttributesKey CPP_INVALID_STRING_ESCAPE = TextAttributesKey.createTextAttributesKey(
                                                    "CPP.INVALID_STRING_ESCAPE",
                                                    SyntaxHighlighterColors.INVALID_STRING_ESCAPE.getDefaultAttributes()
                                                  );
  static final TextAttributesKey CPP_BAD_CHARACTER = TextAttributesKey.createTextAttributesKey(
                                                  "CPP.BADCHARACTER",
                                                  HighlighterColors.BAD_CHARACTER.getDefaultAttributes()
                                                );

  @NotNull
  public Lexer getHighlightingLexer() {
    return new LayeredLexer( new FlexAdapter(new _CppLexer(true, false, true, true, true)) ) { // TODO: c/c++ dialects
      {
        registerSelfStoppingLayer(new StringLiteralLexer('\"', CppTokenTypes.STRING_LITERAL),
                              new IElementType[]{CppTokenTypes.STRING_LITERAL}, IElementType.EMPTY_ARRAY);

        registerSelfStoppingLayer(new StringLiteralLexer('\'', CppTokenTypes.SINGLE_QUOTE_STRING_LITERAL),
                              new IElementType[]{CppTokenTypes.SINGLE_QUOTE_STRING_LITERAL},
                              IElementType.EMPTY_ARRAY);
      }
    };
  }

  @NotNull
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    return pack(keys1.get(tokenType), keys2.get(tokenType));
  }

  static {
    keys1 = new HashMap<IElementType, TextAttributesKey>();
    keys2 = new HashMap<IElementType, TextAttributesKey>();

    fillMap(keys1, CppTokenTypes.CPP_KEYWORDS, CPP_KEYWORD);
    fillMap(keys1, CppTokenTypes.C_KEYWORDS, C_KEYWORD);
    keys1.put(CppTokenTypes.PRE_KEYWORD,PRE_KEYWORD);
    fillMap(keys1, CppTokenTypes.C_KEYWORDS, C_KEYWORD);
    fillMap(keys1, CppTokenTypes.OPERATIONS, CPP_OPERATION_SIGN);

    keys1.put(CppTokenTypes.C_STYLE_COMMENT, CPP_BLOCK_COMMENT);
    keys1.put(CppTokenTypes.END_OF_LINE_COMMENT, CPP_LINE_COMMENT);

    keys1.put(CppTokenTypes.NUMERIC_LITERAL, CPP_NUMBER);
    keys1.put(CppTokenTypes.STRING_LITERAL, CPP_STRING);
    keys1.put(CppTokenTypes.SINGLE_QUOTE_STRING_LITERAL, CPP_STRING);

    keys1.put(CppTokenTypes.LPAR, CPP_PARENTHS);
    keys1.put(CppTokenTypes.RPAR, CPP_PARENTHS);

    keys1.put(CppTokenTypes.LBRACE, CPP_BRACES);
    keys1.put(CppTokenTypes.RBRACE, CPP_BRACES);

    keys1.put(CppTokenTypes.LBRACKET, CPP_BRACKETS);
    keys1.put(CppTokenTypes.RBRACKET, CPP_BRACKETS);

    keys1.put(CppTokenTypes.COMMA, CPP_COMMA);
    keys1.put(CppTokenTypes.DOT, CPP_DOT);
    keys1.put(CppTokenTypes.ARROW, CPP_DOT);
    keys1.put(CppTokenTypes.SEMICOLON, CPP_SEMICOLON);

    keys1.put(StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN, CPP_VALID_STRING_ESCAPE);
    keys1.put(StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN, CPP_INVALID_STRING_ESCAPE);
    keys1.put(StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN, CPP_INVALID_STRING_ESCAPE);

    keys1.put(CppTokenTypes.BAD_CHARACTER, CPP_BAD_CHARACTER);
  }
}
