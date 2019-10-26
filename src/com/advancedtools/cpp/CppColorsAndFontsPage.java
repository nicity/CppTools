// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.util.Map;

/**
 * @author maxim
 * Date: 21.09.2006
 * Time: 5:12:27
 */
public class CppColorsAndFontsPage implements ColorSettingsPage {
  private static final AttributesDescriptor[] ATTRS;

  static {
    ATTRS = new AttributesDescriptor[]{
      new AttributesDescriptor(CppBundle.message("cpp.keyword"), CppHighlighter.CPP_KEYWORD),
      new AttributesDescriptor(CppBundle.message("c.keyword"), CppHighlighter.C_KEYWORD),
      new AttributesDescriptor(CppBundle.message("pre.keyword"), CppHighlighter.PRE_KEYWORD),
      new AttributesDescriptor(CppBundle.message("cpp.blockcomment"), CppHighlighter.CPP_BLOCK_COMMENT),
      new AttributesDescriptor(CppBundle.message("cpp.linecomment"), CppHighlighter.CPP_LINE_COMMENT),
      new AttributesDescriptor(CppBundle.message("cpp.string"), CppHighlighter.CPP_STRING),
      new AttributesDescriptor(CppBundle.message("cpp.number"), CppHighlighter.CPP_NUMBER),
      new AttributesDescriptor(CppBundle.message("cpp.operations"), CppHighlighter.CPP_OPERATION_SIGN),
      new AttributesDescriptor(CppBundle.message("cpp.parens"), CppHighlighter.CPP_PARENTHS),
      new AttributesDescriptor(CppBundle.message("cpp.braces"), CppHighlighter.CPP_BRACES),
      new AttributesDescriptor(CppBundle.message("cpp.brackets"), CppHighlighter.CPP_BRACKETS),
      new AttributesDescriptor(CppBundle.message("cpp.dot"), CppHighlighter.CPP_DOT),
      new AttributesDescriptor(CppBundle.message("cpp.comma"), CppHighlighter.CPP_COMMA),
      new AttributesDescriptor(CppBundle.message("cpp.semicolon"), CppHighlighter.CPP_SEMICOLON),

      new AttributesDescriptor(CppBundle.message("cpp.type"), CppHighlighter.CPP_TYPE),
      new AttributesDescriptor(CppBundle.message("cpp.macros"), CppHighlighter.CPP_MACROS),
      new AttributesDescriptor(CppBundle.message("cpp.namespace"), CppHighlighter.CPP_NAMESPACE),

      new AttributesDescriptor(CppBundle.message("cpp.functions"), CppHighlighter.CPP_FUNCTION),
      new AttributesDescriptor(CppBundle.message("cpp.staticfunctions"), CppHighlighter.CPP_STATIC_FUNCTION),
      new AttributesDescriptor(CppBundle.message("cpp.static"), CppHighlighter.CPP_STATIC),
      new AttributesDescriptor(CppBundle.message("cpp.field"), CppHighlighter.CPP_FIELD),
      new AttributesDescriptor(CppBundle.message("cpp.method"), CppHighlighter.CPP_METHOD),
      new AttributesDescriptor(CppBundle.message("cpp.parameter"), CppHighlighter.CPP_PARAMETER),

      new AttributesDescriptor(CppBundle.message("cpp.constant"), CppHighlighter.CPP_CONSTANT),
      new AttributesDescriptor(CppBundle.message("cpp.pp_arg"), CppHighlighter.CPP_PP_ARG),
      new AttributesDescriptor(CppBundle.message("cpp.pp_skipped"), CppHighlighter.CPP_PP_SKIPPED),
      new AttributesDescriptor(CppBundle.message("cpp.label"), CppHighlighter.CPP_LABEL),
      new AttributesDescriptor(CppBundle.message("cpp.unused"), CppHighlighter.CPP_UNUSED),
    };
  }

  private static final ColorDescriptor[] COLORS = new ColorDescriptor[0];
  private static @NonNls Map<String, TextAttributesKey> ourTags = new HashMap<String, TextAttributesKey>();
  
  static {
    ourTags.put("namespace",CppHighlighter.CPP_NAMESPACE);
    ourTags.put("type",CppHighlighter.CPP_TYPE);
    ourTags.put("macros",CppHighlighter.CPP_MACROS);
    ourTags.put("function",CppHighlighter.CPP_FUNCTION);
    ourTags.put("static_method",CppHighlighter.CPP_STATIC_FUNCTION);
    ourTags.put("static",CppHighlighter.CPP_STATIC);
    ourTags.put("field",CppHighlighter.CPP_FIELD);
    ourTags.put("method",CppHighlighter.CPP_METHOD);
    ourTags.put("parameter",CppHighlighter.CPP_PARAMETER);
    ourTags.put("constant",CppHighlighter.CPP_CONSTANT);
    ourTags.put("label",CppHighlighter.CPP_LABEL);
    ourTags.put("pp_skipped",CppHighlighter.CPP_PP_SKIPPED);
    ourTags.put("macros_param",CppHighlighter.CPP_PP_ARG);
    ourTags.put("unused",CppHighlighter.CPP_UNUSED);
  }

  @NotNull
  public String getDisplayName() {
    return "C / C++";
  }

  @Nullable
  public Icon getIcon() {
    return CppSupportLoader.CPP_FILETYPE.getIcon();
  }

  @NotNull
  public AttributesDescriptor[] getAttributeDescriptors() {
    return ATTRS;
  }

  @NotNull
  public ColorDescriptor[] getColorDescriptors() {
    return COLORS;
  }

  @NotNull
  public SyntaxHighlighter getHighlighter() {
    return SyntaxHighlighter.PROVIDER.create(CppSupportLoader.CPP_FILETYPE, null, new LightVirtualFile("A.cpp"));
  }

  @NonNls
  @NotNull
  public String getDemoText() {
    return "#include \"AAA.h\";\n" +
      "#include <constant><cstdio></constant>;\n" +
      "# define <macros>MYMACROS</macros>(<macros_param>pp_arg</macros_param>) c = <macros_param>pp_arg</macros_param>;\n" +
      "using namespace <namespace>std</namespace>;\n" +
      "class C {\n" +
      "  int <field>field</field>;\n" +
      "  static int <static>staticField</static>;\n" +
      "  static int <static_method>staticMethod</static_method>() {}\n" +
      "  int <method>method</method>() {}\n" +
      "};\n" +
      "int <function>func</function>(int <parameter>param</parameter>) {\n" +
      "  /* block comment */\n" +
      "  int <unused>a</unused>;\n" +
      "  // line comment\n" +
      "  enum { <constant>NONE<constant> = 0 };\n" +
      "  <function>func</function>();\n" +
      "  <namespace>ns</namespace>::<type>T</type>* type = static_cast<T>(<macros>NULL</macros>);\n" +
      "  <label>AAA</label>:\n" +
      "  while(type-><function>func</function>(argv[0]) == <constant>NONE</constant>) { goto <label>AAA</label>; }\n" +
      "#if 0\n" +
      "  <pp_skipped>int a;</pp_skipped>\n" +
      "#endif\n" +
      "}";
  }

  @Nullable
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ourTags;
  }
}


