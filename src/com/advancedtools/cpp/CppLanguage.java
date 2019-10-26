// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.lang.*;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.lang.surroundWith.SurroundDescriptor;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author maxim
 */
public class CppLanguage extends Language implements LanguageFeatureAware {
  public CppLanguage() {
    super("C/C++");
  }

  @NotNull
  public SyntaxHighlighter getSyntaxHighlighter(Project project, VirtualFile file) {
    return new CppHighlighter();
  }

  @Nullable
  public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
    return new TreeBasedStructureViewBuilder() {
      @NotNull
      @Override
      public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
        return createStructureViewModel(); // todo, new api Idea 14.1
      }

      public StructureViewModel createStructureViewModel() {
        return new CppStructureViewBuilder(psiFile);
      }
    };
  }

  static Lexer createLexerStatic(Project project) {
    return new FlexAdapter(new _CppLexer(false, false, true, true, true)); // TODO: different lexers needed!
  }

  @Nullable
  public FormattingModelBuilder getFormattingModelBuilder() {
    if (true) return null;
    return new CppFormattingModelBuilder();
  }

}