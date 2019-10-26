// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.inspections;

import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.utils.IconableGutterNavigator;
import com.advancedtools.cpp.utils.NavigationUtils;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiReferenceExpression;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author maxim
*/
public class NativeJavaMethodsInspection extends BaseCppInspection {
  private static final Key<RangeHighlighter> myCurrentHighlighterKey = Key.create("cpp.rangehighlighter.implementing.native");

  @Nls
  @NotNull
  public String getDisplayName() {
    return "Native Java Methods Declarations";
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "NativeJavaMethods";
  }

  @Nullable
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, final boolean onTheFly) {
    return new PsiElementVisitor() {
      public void visitReferenceExpression(PsiReferenceExpression psiReferenceExpression) {}

      public void visitMethod(final PsiMethod psiMethod) {
        if (onTheFly && psiMethod.getModifierList().hasModifierProperty(PsiModifier.NATIVE)) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              if (!psiMethod.isValid()) return;
              final RangeHighlighter currentHighlighter = psiMethod.getUserData(myCurrentHighlighterKey);
              Editor editor = CppSupportLoader.findEditor(psiMethod.getProject(), psiMethod.getContainingFile().getVirtualFile());

              if (editor != null) {
                if (currentHighlighter != null) editor.getMarkupModel().removeHighlighter(currentHighlighter);

                int textOffset = psiMethod.getTextOffset();
                RangeHighlighter rangeHighlighter = editor.getMarkupModel().addRangeHighlighter(
                  textOffset,
                  textOffset + psiMethod.getName().length(),
                  1,
                  null,
                  HighlighterTargetArea.EXACT_RANGE
                );

                rangeHighlighter.setGutterIconRenderer(
                  new IconableGutterNavigator(IconLoader.getIcon("/gutter/implementedMethod.png"),"implemented in native code") {
                    protected void doNavigate(Project project) {
                      final NativeMethod2JNIFunctionBinding binding = new NativeMethod2JNIFunctionBinding(psiMethod);
                      NavigationUtils.navigate(project, binding.getUsagesList());
                    }
                  }
                );

                psiMethod.putUserData(myCurrentHighlighterKey, rangeHighlighter);
              }
            }
          });
        }
      }
    };
  }

}
