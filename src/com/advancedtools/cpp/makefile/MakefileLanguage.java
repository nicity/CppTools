// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.makefile;

import com.advancedtools.cpp.LanguageFeatureAware;
import com.intellij.ide.structureView.*;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.ide.util.EditSourceUtil;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.Grouper;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author maxim
 */
public class MakefileLanguage extends Language implements LanguageFeatureAware {
  public MakefileLanguage() {
    super("Makefile");
  }

  @NotNull
  public SyntaxHighlighter getSyntaxHighlighter(Project project, VirtualFile virtualFile) {
    return new MakefileSyntaxHighlighter();
  }

  @Nullable
  public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
    return new TreeBasedStructureViewBuilder() {
      @NotNull
      @Override
      public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
        return createStructureViewModel(); // todo, new api Idea 14.1
      }

      @NotNull
      public StructureViewModel createStructureViewModel() {
        return new TextEditorBasedStructureViewModel(psiFile) {
          protected PsiFile getPsiFile()   // TODO: change abstract method to constructor parameter?
          {
            return psiFile;
          }

          @NotNull
          public StructureViewTreeElement getRoot() {
            return new PsiTreeElementBase<PsiElement>(psiFile) {
              @NotNull
              public Collection<StructureViewTreeElement> getChildrenBase() {
                final List<StructureViewTreeElement> children = new ArrayList<StructureViewTreeElement>();
                for(PsiElement el:psiFile.getChildren()) {
                  final ASTNode node = el.getNode();

                  if(node.getElementType() == MakefileTokenTypes.STATEMENT) {
                    for(final ASTNode el2:node.getChildren(null)) {
                      if (el2.getElementType() == MakefileTokenTypes.TARGET_IDENTIFIER) {
                        children.add(new PsiTreeElementBase(el2.getPsi()) {
                          public void navigate(boolean b) {
                            final Navigatable descriptor = EditSourceUtil.getDescriptor(el2.getPsi());
                            if (descriptor != null) descriptor.navigate(b);
                          }

                          public boolean canNavigate() {
                            return true;
                          }

                          public boolean canNavigateToSource() {
                            return canNavigate();
                          }

                          public Collection getChildrenBase() {
                            return Collections.emptyList();
                          }

                          public String getPresentableText() {
                            return el2.getText();
                          }
                        });
                      }
                    }
                  }
                }
                return children;
              }

              public String getPresentableText() {
                return "root";
              }
            };
          }

          @NotNull
          public Grouper[] getGroupers() {
            return new Grouper[0];
          }

          @NotNull
          public Sorter[] getSorters() {
            return new Sorter[] {Sorter.ALPHA_SORTER};
          }

          @NotNull
          public Filter[] getFilters() {
            return new Filter[0];
          }

          @NotNull
          protected Class[] getSuitableClasses()
          {
            return new Class[0];
          }
        };
      }
    };
  }

}
