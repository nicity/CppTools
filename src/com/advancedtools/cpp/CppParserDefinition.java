// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.advancedtools.cpp.psi.CppElement;
import com.advancedtools.cpp.psi.CppKeyword;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

/**
* @author maxim
* Date: 2/3/12
* Time: 1:24 PM
*/
public class CppParserDefinition implements ParserDefinition {
  @NotNull
  public Lexer createLexer(Project project) {
    return CppLanguage.createLexerStatic(project);
  }

  @NotNull
      public PsiParser createParser(Project project) {
    return new CppParser();
  }

  public IFileElementType getFileNodeType() {
    return CppSupportLoader.CPP_FILE;
  }

  @NotNull
      public TokenSet getWhitespaceTokens() {
    return CppTokenTypes.WHITE_SPACES;
  }

  @NotNull
      public TokenSet getCommentTokens() {
    return CppTokenTypes.COMMENTS;
  }

  // IDEA8
  @NotNull
  public TokenSet getStringLiteralElements() {
    return CppTokenTypes.LITERALS;
  }

  @NotNull
      public PsiElement createElement(ASTNode node) {
    if (CppTokenTypes.KEYWORDS.contains(node.getElementType())) {
      return new CppKeyword(node);
    }

    return new CppElement(node);
  }

  public PsiFile createFile(FileViewProvider fileViewProvider) {
    return new CppFile(fileViewProvider);
  }

  public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode astNode, ASTNode astNode1) {
    return SpaceRequirements.MAY;
  }

  static class BlockInfo {
    static enum BlockType {
      BLOCK, STATEMENT, FUNC
    }

    final PsiBuilder.Marker block;
    LinkedList<PsiBuilder.Marker> parensList;
    final BlockType blockType;

    BlockInfo(PsiBuilder.Marker _block, BlockType _blockType) {
      block = _block;
      blockType = _blockType;
    }

    void done() {
      if (parensList != null) {
        while(parensList.size() > 0) doneParens();
      }
      block.done(
        blockType == BlockType.BLOCK ? CppTokenTypes.BLOCK:
          blockType == BlockType.FUNC ? CppTokenTypes.BLOCK:
            CppTokenTypes.STATEMENT);
    }

    public void addParens(PsiBuilder.Marker marker) {
      if (parensList == null) parensList = new LinkedList<PsiBuilder.Marker>();
      parensList.add(marker);
    }

    public void doneParens() {
      if (parensList != null && parensList.size() > 0) parensList.removeLast().done(CppTokenTypes.PARENS);
    }

    public boolean hasParens() {
      return parensList != null && parensList.size() > 0;
    }
  }

  private static class CppParser implements PsiParser {
    @NotNull
    public ASTNode parse(IElementType iElementType, PsiBuilder psiBuilder) {
      final PsiBuilder.Marker rootMarker = psiBuilder.mark();
      final LinkedList<BlockInfo> blocks = new LinkedList<BlockInfo>();
      boolean openBlock = true;

      while (psiBuilder.getTokenType() != null) {
        if (openBlock) {
          final IElementType type = psiBuilder.getTokenType();
          if (type != CppTokenTypes.RBRACE && type != CppTokenTypes.SEMICOLON && type != CppTokenTypes.COMMA &&
            type != CppTokenTypes.LBRACE && (type != CppTokenTypes.PRE_KEYWORD || !psiBuilder.getTokenText().equals("\\"))) {
            openBlock = false;
            blocks.add(new BlockInfo(psiBuilder.mark(), blocks.size() > 0 ? BlockInfo.BlockType.STATEMENT : BlockInfo.BlockType.FUNC));
          }
        }

        final IElementType tokenType = psiBuilder.getTokenType();

        if (tokenType == CppTokenTypes.LBRACE) {
          blocks.add(new BlockInfo(psiBuilder.mark(), BlockInfo.BlockType.BLOCK));
        } else if (tokenType == CppTokenTypes.LPAR || tokenType == CppTokenTypes.LBRACKET) {
          if (blocks.size() > 0) blocks.getLast().addParens(psiBuilder.mark());
        }

        final PsiBuilder.Marker tokenMarker = requiresComposite(tokenType) ? psiBuilder.mark():null;
        psiBuilder.advanceLexer();
        if (tokenMarker != null) tokenMarker.done(tokenType);

        if (tokenType == CppTokenTypes.LBRACE) {
          openBlock = true;
        } else if (tokenType == CppTokenTypes.RPAR || tokenType == CppTokenTypes.RBRACKET) {
          if (blocks.size() > 0) blocks.getLast().doneParens();
        } else if (tokenType == CppTokenTypes.RBRACE && blocks.size() > 0) {
          blocks.removeLast().done();
          if (blocks.size() > 0) blocks.removeLast().done();
          openBlock = true;
        }
        else if (tokenType == CppTokenTypes.SEMICOLON && blocks.size() > 0 && !blocks.getLast().hasParens() && !openBlock) {
          blocks.removeLast().done();
          openBlock = true;
        }
      }

      while(blocks.size() > 0) {
        blocks.removeLast().done();
      }
      rootMarker.done(iElementType);
      return psiBuilder.getTreeBuilt();
    }
  }

  private static boolean requiresComposite(IElementType tokenType) {
    return
      tokenType == CppTokenTypes.IDENTIFIER ||
        tokenType == CppTokenTypes.STRING_LITERAL ||
        CppTokenTypes.OVERRIDABLE_OPERATIONS.contains(tokenType);
  }

  public static class CppFile extends PsiFileBase implements com.advancedtools.cpp.psi.CppFile {
    public CppFile(@NotNull FileViewProvider fileViewProvider) {
      super(fileViewProvider, CppSupportLoader.CPP_LANGUAGE);
    }

    @NotNull
    public FileType getFileType() {
      return CppSupportLoader.CPP_FILETYPE;
    }

    public void accept(@NotNull PsiElementVisitor psiElementVisitor) {
      psiElementVisitor.visitFile(this);
    }

    @Override
    public String toString() {
      return "CppFile:" + getName();
    }
  }
}
