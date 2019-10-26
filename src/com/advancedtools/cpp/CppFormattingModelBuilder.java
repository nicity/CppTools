// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * User: maxim
 * Date: 15.09.2008
 * Time: 9:51:55
 */
public class CppFormattingModelBuilder implements FormattingModelBuilder {
  @NotNull
  public FormattingModel createModel(final PsiElement psiElement, final CodeStyleSettings codeStyleSettings) {
    return new FormattingModel() {
      private FormattingModel myModel;

      {
        myModel = FormattingModelProvider.createFormattingModelForPsiFile(psiElement.getContainingFile(), new CppBlock(psiElement), codeStyleSettings);
      }

      @NotNull
      public Block getRootBlock() {
        return myModel.getRootBlock();
      }

      @NotNull
      public FormattingDocumentModel getDocumentModel() {
        return myModel.getDocumentModel();
      }

      public TextRange replaceWhiteSpace(TextRange textRange, String whiteSpace) {
        return myModel.replaceWhiteSpace(textRange, whiteSpace);
      }

      public TextRange shiftIndentInsideRange(TextRange range, int indent) {
        return myModel.shiftIndentInsideRange(range, indent);
      }

      public void commitChanges() {
        myModel.commitChanges();
      }
    };
  }

  // IDEA8
  public TextRange getRangeAffectingIndent(PsiFile psiFile, int i, ASTNode astNode) {
    return null;
  }

  private static class CppBlock implements Block {
    private List<Block> myBlocks;
    private final ASTNode myNode;

    public CppBlock(PsiElement psiElement) {
      myNode = psiElement.getNode();
      psiElement.getFirstChild(); // expand chameleon
    }

    protected List<Block> buildChildren() {
      final List<Block> result = new ArrayList<Block>();

      myNode.getPsi().acceptChildren(new PsiRecursiveElementVisitor() {
        public void visitElement(PsiElement psiElement) {
          final ASTNode node = psiElement.getNode();

          if (node != null) {
            final IElementType nodeType = node.getElementType();
            if (nodeType == CppTokenTypes.BLOCK || nodeType == CppTokenTypes.LBRACE || nodeType == CppTokenTypes.RBRACE) {
              final CppBlock block = new CppBlock(psiElement);
              result.add(block);
            }
          }

          super.visitElement(psiElement);
        }
      });
      return result;
    }

    @Nullable
    public Spacing getSpacing(Block block, Block block1) {
      final IElementType type = ((CppBlock) block).myNode.getElementType();
      final IElementType type2 = ((CppBlock) block1).myNode.getElementType();
      if (type == CppTokenTypes.LBRACE || type2 == CppTokenTypes.RBRACE) {
        CodeStyleSettings mySettings = CodeStyleSettingsManager.getSettings((myNode.getPsi().getProject()));
        return Spacing.createSpacing(0, 0, 1, mySettings.KEEP_LINE_BREAKS, mySettings.KEEP_BLANK_LINES_IN_CODE);
      }
      return null;
    }

    @NotNull
    public ChildAttributes getChildAttributes(int i) {
      if (myNode.getElementType() == CppTokenTypes.BLOCK) return new ChildAttributes(Indent.getNormalIndent(), null);
      return new ChildAttributes(Indent.getNoneIndent(), null);
    }

    public boolean isIncomplete() {
      return false;
    }

    public boolean isLeaf() {
      return myNode.getFirstChildNode() == null;
    }

    @NotNull
    public TextRange getTextRange() {
      return myNode.getTextRange();
    }

    @NotNull
    public List<Block> getSubBlocks() {
      if (myBlocks == null) myBlocks = buildChildren();
      return myBlocks;
    }

    @Nullable
    public Wrap getWrap() {
      return null;
    }

    public Indent getIndent() {
      final IElementType type = myNode.getElementType();
      return Indent.getNormalIndent();
    }

    @Nullable
    public Alignment getAlignment() {
      return null;
    }
  }
}
