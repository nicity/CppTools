// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.advancedtools.cpp.commands.FindUsagesCommand;
import com.advancedtools.cpp.facade.EnvironmentFacade;
import com.advancedtools.cpp.inspections.JNIFunction2JavaMethodBinding;
import com.advancedtools.cpp.inspections.NativeMethod2JNIFunctionBinding;
import com.advancedtools.cpp.psi.CppElement;
import com.advancedtools.cpp.psi.CppFile;
import com.advancedtools.cpp.psi.CppKeyword;
import com.advancedtools.cpp.usages.FileUsage;
import com.advancedtools.cpp.usages.OurUsage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ex.ActionManagerEx;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NonNls;

import java.util.Set;

/**
 * @author maxim
 */
public class CppReferencesSearcher implements QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
  private Key<String> ourKey = Key.create("Last Reference Action");
  private Key<PsiFile> ourFileKey = Key.create("Last Reference Action File");
  private Key<Integer> ourOffsetKey = Key.create("Last Reference Action Editor Offse");
  private static final @NonNls String RENAME_ACTION_TEXT = "Rename...";
  private static final @NonNls String FIND_USAGES_COMMAND_TEXT = "Find Usages...";

  public CppReferencesSearcher() {
    ActionManagerEx.getInstanceEx().addAnActionListener(new AnActionListener() {
      public void beforeActionPerformed(AnAction anAction, DataContext dataContext) {
        final String s = anAction.getTemplatePresentation().getText();

        if (RENAME_ACTION_TEXT.equals(s) ||
            FIND_USAGES_COMMAND_TEXT.equals(s) ||
            (s != null && s.indexOf("Usages") != -1)
           ) {
          final Project project = (Project) dataContext.getData(DataConstants.PROJECT);
          if (project != null) {
            project.putUserData(ourKey,s);
            final Editor editor = (Editor) dataContext.getData(DataConstants.EDITOR);

            if (editor != null) {
              project.putUserData(ourFileKey, PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument()));
              project.putUserData(ourOffsetKey, editor.getCaretModel().getOffset());
            } else { // Recent Find Usages from usage view
              project.putUserData(ourFileKey, null);
              project.putUserData(ourOffsetKey, null);
            }
          }
        }
      }

      public void afterActionPerformed(AnAction anAction, DataContext dataContext) {
      }

      public void beforeEditorTyping(char c, DataContext dataContext) {
      }

      public void beforeActionPerformed(AnAction anAction, DataContext dataContext, AnActionEvent anActionEvent) {
        beforeActionPerformed(anAction, dataContext);
      }

      public void afterActionPerformed(AnAction anAction, DataContext dataContext, AnActionEvent anActionEvent) {
        afterActionPerformed(anAction, dataContext);
      }
    });
  }

  public boolean execute(final ReferencesSearch.SearchParameters params, final Processor<PsiReference> processor) {
    final PsiElement target = params.getElementToSearch();

    if (target instanceof CppElement || target instanceof CppKeyword /*operator*/) {
      final GlobalSearchScope globalScope = params.getScope() instanceof GlobalSearchScope ? (GlobalSearchScope) params.getScope():null;
      final PsiFile psiFile = target.getContainingFile();

      if (doFindRefsInCppCode(psiFile, target, params, globalScope, processor)) return true;

      final String text = ApplicationManager.getApplication().runReadAction(new Computable<String>() {
        public String compute() {
          return target.getText();
        }
      });
      if (text.startsWith(JNIFunction2JavaMethodBinding.JAVA_NATIVE_PREFIX) &&
        EnvironmentFacade.getInstance().isJavaIde()) {
        ApplicationManager.getApplication().runReadAction(new Runnable() {

          public void run() {
            final JNIFunction2JavaMethodBinding binding = new JNIFunction2JavaMethodBinding(text, psiFile.getManager());
            if (binding.getMethod() != null) {
              ReferencesSearch.search(binding.getMethod(), globalScope).forEach(processor);
            }
          }
        }
        );
      }
    } else if (target instanceof CppFile) {
      final GlobalSearchScope globalScope = params.getScope() instanceof GlobalSearchScope ? (GlobalSearchScope) params.getScope():null;
      if (doFindRefsInCppCode((PsiFile) target, target, params, globalScope, processor)) return true;
    } else if (EnvironmentFacade.getInstance().isJavaIde() && target instanceof PsiMethod) {
      final GlobalSearchScope globalScope = params.getScope() instanceof GlobalSearchScope ? (GlobalSearchScope) params.getScope() : null;
      if (globalScope == null) return true;
      ApplicationManager.getApplication().runReadAction(new Runnable() {

        public void run() {
          PsiMethod method = (PsiMethod) target;

          if (method.getModifierList().hasModifierProperty(PsiModifier.NATIVE)) {
            final NativeMethod2JNIFunctionBinding binding = new NativeMethod2JNIFunctionBinding(method);

            if (binding.getUsagesList() != null) {
              final FileUsage usage = binding.getUsagesList().files.get(0);
              final VirtualFile virtualFile = usage.findVirtualFile();

              if (globalScope.contains(virtualFile)) {
                PsiFile psiFile = PsiManager.getInstance(method.getProject()).findFile(virtualFile);
                if (psiFile != null) {
                  final PsiElement psiElement = psiFile.findElementAt(usage.usageList.get(0).start);
                  if (psiElement != null && psiElement.getParent() instanceof CppElement) {
                    doFindRefsInCppCode(psiFile, psiElement, params, globalScope, processor);
                  }
                }
              }
            }
          }
        }
      });
    }

    return true;
  }

  private boolean doFindRefsInCppCode(final PsiFile psiFile, PsiElement target, ReferencesSearch.SearchParameters params,
                                      GlobalSearchScope globalScope, final Processor<PsiReference> processor) {
    final Project project = psiFile.getProject();
    final String commandName = project.getUserData(ourKey);
    final int offset;
    VirtualFile file;

    if (target instanceof CppFile) {
      offset = FindUsagesCommand.MAGIC_FILE_OFFSET;
      file = ((CppFile)target).getVirtualFile();
    } else {
      VirtualFile actionStartFile = null;
      int actionStartOffset = -1;
      
      final PsiFile actionStartPsiFile = project.getUserData(ourFileKey);
      if (actionStartPsiFile != null) {
        actionStartFile = actionStartPsiFile.getVirtualFile();
        final Integer integer = project.getUserData(ourOffsetKey);
        if (integer != null) actionStartOffset = integer.intValue();
      }
      
      if (actionStartOffset != -1) {
        offset = actionStartOffset;
        file = actionStartFile;
      } else {
        file = psiFile.getVirtualFile();
        offset = target.getTextOffset();
      }
    }

    final FindUsagesCommand findUsagesCommand = new FindUsagesCommand(
      file.getPath(),
      offset,
      RENAME_ACTION_TEXT.equals(commandName)
    );
    findUsagesCommand.setDoInfiniteBlockingWithCancelledCheck(true);

    findUsagesCommand.post(psiFile.getProject());

    if (!findUsagesCommand.hasReadyResult()) return true;

    final int count = findUsagesCommand.getUsageCount();
    if (count == 0) return true;

    final boolean scopeIsLocal = params.getScope() instanceof LocalSearchScope;
    final Set<VirtualFile> localScope = scopeIsLocal ? new HashSet<VirtualFile>() : null;

    if (scopeIsLocal) {
      for(PsiElement e: ((LocalSearchScope)params.getScope()).getScope()) {
        localScope.add(e.getContainingFile().getVirtualFile());
      }
    }

    for(final FileUsage fu:findUsagesCommand.getUsagesList().files) {
      final VirtualFile usagefile = fu.findVirtualFile();
      if ((globalScope != null && !globalScope.contains(usagefile)) ||
          localScope != null && !localScope.contains(usagefile)
        ) {
        continue;
      }


      Runnable runnable = new Runnable() {
        public void run() {
          final PsiFile usageFile = psiFile.getManager().findFile( usagefile );

          for(final OurUsage u:fu.usageList) {
            final PsiElement psiElement = usageFile.findElementAt(u.getStart());

            if (psiElement != null) {
              final PsiElement parentElement = psiElement.getParent();
              if (parentElement instanceof CppElement || parentElement instanceof CppKeyword /*operator*/) {
                final PsiReference reference = parentElement.getReference();
                if (reference != null) processor.process( reference );
              }
            }
          }
        }
      };

      ApplicationManager.getApplication().runReadAction(runnable);
    }
    return false;
  }
}
