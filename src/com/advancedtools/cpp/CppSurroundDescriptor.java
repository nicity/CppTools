// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.intellij.lang.surroundWith.SurroundDescriptor;
import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.editor.Editor;
import com.intellij.util.IncorrectOperationException;
import com.intellij.codeInsight.template.*;
import com.intellij.codeInsight.lookup.LookupItem;
import com.advancedtools.cpp.psi.ICppElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;

/**
 * @author maxim
 */
public class CppSurroundDescriptor implements SurroundDescriptor {
  @NotNull
  public PsiElement[] getElementsToSurround(PsiFile psiFile, int i, int i1) {
    PsiElement psiElement = psiFile.findElementAt(i);
    PsiElement psiElement2 = psiFile.findElementAt(i1 - 1);
    PsiElement commonParent = PsiTreeUtil.findCommonParent(psiElement, psiElement2);

    PsiElement firstStatementStart = findStamentNodeFromParent(psiElement, commonParent);
    PsiElement lastStatementStart = findStamentNodeFromParent(psiElement2, commonParent);
    if (firstStatementStart != null && lastStatementStart != null) {

      List<PsiElement> result = new ArrayList<PsiElement>();
      for(PsiElement e = firstStatementStart; e != null ; e = e.getNextSibling()) {
        result.add(e);
        if (e == lastStatementStart) break;
      }

      if (result.get(result.size() - 1) == lastStatementStart) {
        return result.toArray(new PsiElement[result.size()]);
      }
    }

    System.out.println(firstStatementStart);
    return new PsiElement[] {commonParent};
  }

  private static PsiElement findStamentNodeFromParent(PsiElement psiElement, PsiElement commonParent) {
    PsiElement firstStatementStart = null;

    for(PsiElement el = psiElement.getParent(); el != null; el = el.getParent()) {
      if(el instanceof ICppElement && el.getNode().getElementType() == CppTokenTypes.STATEMENT &&
         el.getParent() == commonParent
        ) {
        firstStatementStart = el;
        break;
      }
    }
    return firstStatementStart;
  }

  @NotNull
  public Surrounder[] getSurrounders() {
    return new Surrounder[] {
      new IfSurrounder(),
      new ParensSurrounder(),
      new CStyleCastSurrounder(),
      new ConstCastSurrounder(),
      new DynamicCastSurrounder(),
      new ReinterpretCastSurrounder(),
      new StaticCastSurrounder()
    };
  }

  public boolean isExclusive() {
    return false;
  }

  private static abstract class BaseSurrounder implements Surrounder {
    protected static final String SELECTION_VAR = "SELECTION";

    public abstract void configureTemplate(Template t);
    
    @Nullable
    public TextRange surroundElements(@NotNull Project project, @NotNull Editor editor, @NotNull PsiElement[] psiElements) throws IncorrectOperationException {
      Template t = TemplateManager.getInstance(project).createTemplate("","");

      configureTemplate(t);

      editor.getCaretModel().moveToOffset(psiElements[0].getTextRange().getStartOffset());
      StringBuilder selection = new StringBuilder();
      for(PsiElement p:psiElements) selection.append(p.getText());
      TemplateManager.getInstance(project).startTemplate(editor, selection.toString(), t);

      return null;
    }

    public boolean isApplicable(@NotNull PsiElement[] psiElements) {
      return true;
    }

    protected Expression buildExpr(final String name) {
      return new Expression() {
        final Result textResult = new TextResult(name);

        public Result calculateResult(ExpressionContext expressionContext) {
          return textResult;
        }

        public Result calculateQuickResult(ExpressionContext expressionContext) {
          return textResult;
        }

        public LookupItem[] calculateLookupItems(ExpressionContext expressionContext) {
          return new LookupItem[0];
        }
      };
    }

  }

  private static class StaticCastSurrounder extends BaseSurrounder {
    public void configureTemplate(Template t) {
      t.addTextSegment("static_cast<");
      final Expression expression = buildExpr("type");
      t.addVariable("type", expression, expression, true);
      t.addTextSegment(">(");
      t.addVariableSegment(SELECTION_VAR);
      t.addTextSegment(")");
      t.addEndVariable();
    }

    public String getTemplateDescription() {
      return "static__cast";
    }
  }

  private static class DynamicCastSurrounder extends BaseSurrounder {
    public void configureTemplate(Template t) {
      t.addTextSegment("dynamic_cast<");
      final Expression expression = buildExpr("type");
      t.addVariable("type", expression, expression, true);
      t.addTextSegment(">(");
      t.addVariableSegment(SELECTION_VAR);
      t.addTextSegment(")");
      t.addEndVariable();
    }

    public String getTemplateDescription() {
      return "dynamic__cast";
    }
  }

  private static class ConstCastSurrounder extends BaseSurrounder {
    public void configureTemplate(Template t) {
      t.addTextSegment("const_cast<");
      final Expression expression = buildExpr("type");
      t.addVariable("type", expression, expression, true);
      t.addTextSegment(">(");
      t.addVariableSegment(SELECTION_VAR);
      t.addTextSegment(")");
      t.addEndVariable();
    }

    public String getTemplateDescription() {
      return "const__cast";
    }
  }

  private static class ReinterpretCastSurrounder extends BaseSurrounder {
    public void configureTemplate(Template t) {
      t.addTextSegment("reinterpret_cast<");
      final Expression expression = buildExpr("type");
      t.addVariable("type", expression, expression, true);
      t.addTextSegment(">(");
      t.addVariableSegment(SELECTION_VAR);
      t.addTextSegment(")");
      t.addEndVariable();
    }

    public String getTemplateDescription() {
      return "reinterpret__cast";
    }
  }

  private static class CStyleCastSurrounder extends BaseSurrounder {
    public void configureTemplate(Template t) {
      t.addTextSegment("((");
      final Expression expression = buildExpr("type");
      t.addVariable("type", expression, expression, true);
      t.addTextSegment(")");
      t.addVariableSegment(SELECTION_VAR);
      t.addTextSegment(")");
      t.addEndVariable();
    }

    public String getTemplateDescription() {
      return "c__style__cast";
    }
  }

  private static class ParensSurrounder extends BaseSurrounder {
    public void configureTemplate(Template t) {
      t.addTextSegment("(");
      t.addVariableSegment(SELECTION_VAR);
      t.addTextSegment(")");
      t.addEndVariable();
    }

    public String getTemplateDescription() {
      return "parens";
    }
  }

  private static class IfSurrounder extends BaseSurrounder {
    public String getTemplateDescription() {
      return "if";
    }

    public void configureTemplate(Template t) {
      t.addTextSegment("if(");
      final Expression expression = buildExpr("condition");

      t.addVariable("condition", expression, expression, true);
      t.addTextSegment(") {\n");
      t.addVariableSegment("SELECTION");
      t.addTextSegment("\n}");
      t.addEndVariable();
    }
  }
}
