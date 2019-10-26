// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.makefile;

import com.advancedtools.cpp.CppBundle;
import com.advancedtools.cpp.CppHighlighter;
import com.advancedtools.cpp.CppSupportLoader;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

/**
 * @author maxim
 * Date: 1/29/13
 * Time: 11:22 AM
 */
public class MakefileColorsAndFontsPage implements ColorSettingsPage {
  private static final AttributesDescriptor[] ATTRS;

  static {
    ATTRS = new AttributesDescriptor[]{
      new AttributesDescriptor(CppBundle.message("make.keyword"), MakefileSyntaxHighlighter.Makefile_KEYWORD),
      new AttributesDescriptor(CppBundle.message("make.linecomment"), MakefileSyntaxHighlighter.Makefile_LINE_COMMENT),
      new AttributesDescriptor(CppBundle.message("make.templatedata"), MakefileSyntaxHighlighter.Makefile_TEMPLATE_DATA),
      new AttributesDescriptor(CppBundle.message("make.target"), MakefileSyntaxHighlighter.Makefile_TARGET),
      new AttributesDescriptor(CppBundle.message("make.definition"), MakefileSyntaxHighlighter.Makefile_DEFINITION),
    };
  }

  private static final ColorDescriptor[] COLORS = new ColorDescriptor[0];
  private static @NonNls
  Map<String, TextAttributesKey> ourTags = new HashMap<String, TextAttributesKey>();

  static {
  }

  @NotNull
  public String getDisplayName() {
    return "Makefile";
  }

  @Nullable
  public Icon getIcon() {
    return CppSupportLoader.MAKE_FILETYPE.getIcon();
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
    return SyntaxHighlighter.PROVIDER.create(CppSupportLoader.MAKE_FILETYPE, null, new LightVirtualFile("Makefile"));
  }

  @NonNls
  @NotNull
  public String getDemoText() {
    return "EXE = $(OUTPUT_PATH)${Executable}\n" +
      "ifeq ($(OS),Windows_NT)\n" +
      "  OBJ = obj\n" +
      "endif\n" +
      "# INCLUDES = -I../.includes\n" +
      "rebuild: clean";
  }

  @Nullable
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ourTags;
  }
}
