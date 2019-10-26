// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.makefile;

import com.advancedtools.cpp.CppSupportLoader;
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

/**
* @author maxim
* Date: 2/3/12
* Time: 1:28 PM
*/
public class MakefileParserDefinition implements ParserDefinition {
  @NotNull
    public Lexer createLexer(Project project) {
    return new MakefileParsingLexer();
  }

  public PsiParser createParser(Project project) {
    return new MakefileParser();
  }

  public IFileElementType getFileNodeType() {
    return MakefileTokenTypes.MAKE_FILE;
  }

  @NotNull
    public TokenSet getWhitespaceTokens() {
    return MakefileTokenTypes.WHITE_SPACES;
  }

  @NotNull
    public TokenSet getCommentTokens() {
    return MakefileTokenTypes.COMMENTS;
  }

  // IDEA8
  @NotNull
  public TokenSet getStringLiteralElements() {
    return MakefileTokenTypes.LITERALS;
  }

  @NotNull
  public PsiElement createElement(ASTNode astNode) {
    if (MakefileParser.shouldProduceDefinition(astNode.getElementType())) {
      return new MakefileNamedElement(astNode);
    }
    return new MakefilePsiElement(astNode);
  }

  public PsiFile createFile(FileViewProvider fileViewProvider) {
    return new MakeFile(fileViewProvider);
  }

  public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode astNode, ASTNode astNode1) {
    return SpaceRequirements.MAY;
  }

  public static class MakefileParser implements PsiParser {
    @NotNull
    public ASTNode parse(IElementType iElementType, PsiBuilder psiBuilder) {
      final PsiBuilder.Marker marker = psiBuilder.mark();
      PsiBuilder.Marker statement = psiBuilder.mark();
      IElementType statementType = null;

      while(!psiBuilder.eof()) {
        final IElementType tokenType = psiBuilder.getTokenType();

        if (tokenType == MakefileTokenTypes.SEMANTIC_WHITESPACE) {
          psiBuilder.advanceLexer();
          statement.done(statementType != null ? statementType:MakefileTokenTypes.STATEMENT);
          statementType = null;
          statement = psiBuilder.mark();
          continue;
        } else if (shouldProduceComposite(tokenType)) {
          PsiBuilder.Marker identifier = psiBuilder.mark();
          psiBuilder.advanceLexer();
          identifier.done(tokenType);
          continue;
        } else if (tokenType == MakefileTokenTypes.VAR_DEFINITION) {
          statementType = tokenType;
        } else if (tokenType == MakefileTokenTypes.IDENTIFIER_START) {
          readIdentifier(psiBuilder, MakefileTokenTypes.IDENTIFIER_END, MakefileTokenTypes.IDENTIFIER);
        }
        else if (tokenType == MakefileTokenTypes.TARGET_IDENTIFIER_START) {
          readIdentifier(psiBuilder, MakefileTokenTypes.TARGET_IDENTIFIER_END, MakefileTokenTypes.TARGET_IDENTIFIER);
          if (!psiBuilder.eof()) psiBuilder.advanceLexer();

          statementType = MakefileTokenTypes.TARGET_DECLARATION;
          continue;
        }
        psiBuilder.advanceLexer();
      }

      statement.done(MakefileTokenTypes.STATEMENT);
      marker.done(iElementType);
      return psiBuilder.getTreeBuilt();
    }

    private static void readIdentifier(PsiBuilder psiBuilder, IElementType identifierEnd, IElementType identifierType) {
      PsiBuilder.Marker identifier = psiBuilder.mark();
      psiBuilder.advanceLexer();
      IElementType currentType;

      while(!psiBuilder.eof() && (currentType = psiBuilder.getTokenType()) != identifierEnd) {
        PsiBuilder.Marker marker = shouldProduceComposite(currentType) ? psiBuilder.mark() : null;
        psiBuilder.advanceLexer();
        if (marker != null) marker.done(currentType);
      }
      identifier.done(identifierType);
    }

    public static boolean shouldProduceComposite(IElementType tokenType) {
      return tokenType == MakefileTokenTypes.IDENTIFIER ||
        tokenType == MakefileTokenTypes.VAR_REFERENCE;
    }

    public static boolean shouldProduceDefinition(IElementType elementType) {
      return elementType == MakefileTokenTypes.TARGET_DECLARATION ||
        elementType == MakefileTokenTypes.VAR_DEFINITION;
    }
  }

  static class MakefileParsingLexer extends MakefileLexer {
    private CharSequence sequence;
    private final int ON_COMMENT_OR_CONTINUE_STATEMENT = _MakefileLexer.CONTINUE + 1;
    private final int ON_SEMANTIC_LF = ON_COMMENT_OR_CONTINUE_STATEMENT + 1;

    boolean hasPreviousCommentOrContinue;
    boolean onSemanticLineFeed;

    public MakefileParsingLexer() {
      super(false);
    }

    public void advance() {
      if (!onSemanticLineFeed) {
        super.advance();

        IElementType tokenType = getTokenType();
        if (!hasPreviousCommentOrContinue && tokenType == MakefileTokenTypes.WHITE_SPACE) {
          if (sequence == null) sequence = getBufferSequence();
          final int tokenEnd = getTokenEnd();

          for(int i = getTokenStart(); i < tokenEnd; ++i) {
            if (sequence.charAt(i) == '\n') {
              onSemanticLineFeed = true;
              break;
            }
          }
        }

        hasPreviousCommentOrContinue = tokenType == MakefileTokenTypes.END_OF_LINE_COMMENT ||
          tokenType == MakefileTokenTypes.CONTINUE_STATEMENT;
      } else {
        onSemanticLineFeed = false;
        hasPreviousCommentOrContinue = false;
      }
    }

    public IElementType getTokenType() {
      return onSemanticLineFeed ? MakefileTokenTypes.SEMANTIC_WHITESPACE : super.getTokenType();
    }

    public int getTokenEnd() {
      return onSemanticLineFeed ? getTokenStart():super.getTokenEnd();
    }

    public int getState() {
      if (onSemanticLineFeed) return ON_SEMANTIC_LF;
      if (hasPreviousCommentOrContinue) return ON_COMMENT_OR_CONTINUE_STATEMENT;
      return super.getState();
    }
  }

  /**
  * @author maxim
  * Date: 2/7/12
  * Time: 12:53 PM
  */
  public static class MakeFile extends PsiFileBase {
    public MakeFile(@NotNull FileViewProvider fileViewProvider) {
      super(fileViewProvider, CppSupportLoader.MAKEFILE_LANGUAGE);
    }

    @NotNull
    public FileType getFileType() {
      return CppSupportLoader.MAKE_FILETYPE;
    }

    public void accept(@NotNull PsiElementVisitor psiElementVisitor) {
      psiElementVisitor.visitFile(this);
    }

    @Override
    public String toString() {
      return "MakeFile:" + getName();
    }
  }
}
