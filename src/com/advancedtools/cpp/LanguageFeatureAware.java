package com.advancedtools.cpp;

import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.psi.PsiFile;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.lang.Commenter;
import com.intellij.lang.ParserDefinition;

/**
 * User: maxim
 * Date: Jan 11, 2009
 * Time: 9:22:35 PM
 */
public interface LanguageFeatureAware {
  StructureViewBuilder getStructureViewBuilder(PsiFile psiFile);

  SyntaxHighlighter getSyntaxHighlighter(Project project, VirtualFile virtualFile);
}
