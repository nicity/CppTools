// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.advancedtools.cpp.commands.NavigationCommand;
import com.advancedtools.cpp.usages.FileUsage;
import com.advancedtools.cpp.usages.OurUsage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import org.jetbrains.annotations.NonNls;

/**
 * @author maxim
 */
public class CppDefinitionSearcher implements QueryExecutor<PsiElement, PsiElement> {
  public boolean execute(final PsiElement sourceElement, final Processor<PsiElement> consumer) {
    if (sourceElement instanceof PsiNamedElement &&
      sourceElement.getLanguage() == CppSupportLoader.CPP_LANGUAGE) {
      // todo before write action listener to cancel long read action
      ApplicationManager.getApplication().runReadAction(new Runnable() {
        public void run() {
          final PsiReference reference = sourceElement.getReference();
          final ResolveResult[] resolveResults = reference instanceof PsiPolyVariantReference ?
            ((PsiPolyVariantReference) reference).multiResolve(false):
            ResolveResult.EMPTY_ARRAY;

          for (ResolveResult r : resolveResults) {
            consumer.process(r.getElement());
          }

          Project project = sourceElement.getProject();
          final NavigationCommand navigationCommand = new NavigationCommand(sourceElement.getContainingFile(),
            sourceElement.getTextOffset()) {
            @NonNls
            @Override protected String getCommandText() {
              return "find-inheritors";
            }
          };
          navigationCommand.post(project);

          if (!navigationCommand.hasReadyResult() || navigationCommand.getUsageCount() == 0) return;

          final PsiManager psiManager = PsiManager.getInstance(project);

          for(FileUsage fu:navigationCommand.getUsagesList().files) {
            final VirtualFile file = fu.findVirtualFile();
            if (file == null) continue;

            final PsiFile usageFile = psiManager.findFile( file );
            if (usageFile == null) continue;

            for(final OurUsage u:fu.usageList) {
              final PsiElement psiElement = usageFile.findElementAt(u.getStart());
              if (psiElement != null) {
                PsiElement element = psiElement.getParent();
                element.putUserData(CppSupportLoader.ourUsageKey, u);
                consumer.process(element);
              }
            }
          }

        }
      });
    }
    return true;
  }
}
