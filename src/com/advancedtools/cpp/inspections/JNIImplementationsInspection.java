// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.inspections;

import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.CppTokenTypes;
import com.advancedtools.cpp.psi.CppElement;
import com.advancedtools.cpp.psi.CppElementVisitor;
import com.advancedtools.cpp.utils.IconableGutterNavigator;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author maxim
*/
public class JNIImplementationsInspection extends BaseCppInspection {
  private static final Key<Map<String,RangeHighlighter>> ourNativeMarkersMapKey = Key.create("cpp.rangehighlighters.implementing.native");

  @Nls
  @NotNull
  public String getDisplayName() {
    return "JNI Implementations";
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "JNIImplementations";
  }

  @Nullable
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, final boolean onTheFly) {
    return new CppElementVisitor() {
      public void visitReferenceExpression(PsiReferenceExpression psiReferenceExpression) {}

      public void visitCppElement(final CppElement element) {
        if (!onTheFly) return;
        final ASTNode node = element.getNode();
        if (node.getElementType() != CppTokenTypes.IDENTIFIER) return;
        final ASTNode firstChildNode = node.getFirstChildNode();
        if (firstChildNode == null) return;
        final String s = firstChildNode.getText();

        if (s.startsWith(JNIFunction2JavaMethodBinding.JAVA_NATIVE_PREFIX)) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              final PsiFile psiFile = element.getContainingFile();
              Map<String, RangeHighlighter> map = psiFile.getUserData(ourNativeMarkersMapKey);
              if (map == null) {
                map = new HashMap<String, RangeHighlighter>();
                psiFile.putUserData(ourNativeMarkersMapKey, map);
              }

              Editor editor = CppSupportLoader.findEditor(element.getProject(), psiFile.getVirtualFile());

              if (editor != null) {
                final RangeHighlighter currentHighlighter = map.get(s);
                if (currentHighlighter != null) editor.getMarkupModel().removeHighlighter(currentHighlighter);

                int textOffset = element.getTextOffset();
                final RangeHighlighter rangeHighlighter = editor.getMarkupModel().addRangeHighlighter(
                  textOffset,
                  textOffset + s.length(),
                  1,
                  null,
                  HighlighterTargetArea.EXACT_RANGE
                );

                rangeHighlighter.setGutterIconRenderer(
                  new IconableGutterNavigator(
                    IconLoader.getIcon("/gutter/implementingMethod.png"),
                    "declared in java code"
                  ) {
                  protected void doNavigate(Project project) {
                    JNIFunction2JavaMethodBinding method2JavaBinding = new JNIFunction2JavaMethodBinding(s, PsiManager.getInstance(project));
                    PsiMethod m = method2JavaBinding.getMethod();
                    if (m != null) m.navigate(true);
                  }
                });

                map.put(s, rangeHighlighter);
              }
            }
          });
        }
      }
    };
  }
}
