// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.psi;

import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.CppTokenTypes;
import com.advancedtools.cpp.usages.FileUsage;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.StringTokenizer;

/**
 * @author maxim
 */
public class CppElement extends ASTWrapperPsiElement implements ICppElement {
  private PsiReference[] myCachedRefs;
  private long myStamp;

  public CppElement(ASTNode astNode) {
    super(astNode);
  }

  @NotNull
  public PsiReference[] getReferences() {
    final long count = manager(this).getModificationTracker().getModificationCount();
    if (myCachedRefs == null || myStamp != count) {
      IElementType elementType = getNode().getElementType();
      PsiReference[] refs;

      if (elementType == CppTokenTypes.STRING_LITERAL ||
          elementType == CppTokenTypes.SINGLE_QUOTE_STRING_LITERAL
         ) {
        final String text = getNode().getText();
        StringTokenizer tokenizer = new StringTokenizer(StringUtil.stripQuotesAroundValue(text),"/\\");
        refs = new PsiReference[tokenizer.countTokens()];
        int i = 0;
        while(tokenizer.hasMoreElements()) {
          final String token = tokenizer.nextToken();
          final int index = text.indexOf(token);
          refs[i++] = new MyPsiPolyVariantReference(this, new TextRange(index, index + token.length()));
        }
      } else
      if (elementType == CppTokenTypes.BLOCK ||
          elementType == CppTokenTypes.STATEMENT ||
          elementType == CppTokenTypes.PARENS ||
          // Do not give refs for following element types since it causes problems with overwriting during completion
          // in such situation f(aaa|)
          elementType == CppTokenTypes.RPAR ||
          elementType == CppTokenTypes.RBRACKET ||
          elementType == CppTokenTypes.GT
         ) {
        refs = PsiReference.EMPTY_ARRAY;
      } else {
        refs = new PsiReference[] {
          new MyPsiPolyVariantReference(this)
        };
      }

      myCachedRefs = refs;
      myStamp = count;
    }
    return myCachedRefs;
  }

  private static PsiManager manager(PsiElement cppElement) {
    return cppElement.getManager();
  }

  public String getName() {
    return getText();
  }

  public PsiElement setName(@NonNls @NotNull String string) throws IncorrectOperationException {
    getNode().replaceChild(getFirstChild().getNode(), createNameIdentifier(getProject(), string));
    return this;
  }
  
  static class MyResolver implements ResolveCache.PolyVariantResolver {
    static ResolveCache.PolyVariantResolver INSTANCE = new MyResolver();

    public Object resolve(PsiReference psiReference, boolean b) {
      return ((MyPsiPolyVariantReference)psiReference).resolveInner();
    }

    @NotNull
    public ResolveResult[] resolve(@NotNull PsiPolyVariantReference psiReference, boolean b) {
      return ((MyPsiPolyVariantReference) psiReference).resolveInner();
    }
  }
    
  public String toString() {
    return getNode().getElementType().toString();
  }

  public PsiReference getReference() {
    final PsiReference[] references = getReferences();
    return references.length != 0 ? references[0]:null;
  }

  @NotNull
  public Language getLanguage() {
    return CppSupportLoader.CPP_FILETYPE.getLanguage();
  }

  public static ASTNode createNameIdentifier(Project project, String name) {
    final PsiFile dummyFile = PsiFileFactory.getInstance(project).createFileFromText("A.cpp", name);
    final CppElement element = (CppElement)dummyFile.getFirstChild();

    return element.getNode().getFirstChildNode();
  }

  public ItemPresentation getPresentation() {
    return new MyItemPresentation();
  }

  public void accept(@NotNull PsiElementVisitor psiElementVisitor) {
    if (psiElementVisitor instanceof CppElementVisitor) {
      ((CppElementVisitor)psiElementVisitor).visitCppElement(this);
    } else {
      psiElementVisitor.visitElement(this);
    }
  }

  private class MyItemPresentation implements ItemPresentation {
    public String getPresentableText() {
      return getText();
    }

    @Nullable
    public String getLocationString() {
      VirtualFile file = getContainingFile().getViewProvider().getVirtualFile();
      return FileUsage.formatFile(file);
    }

    @Nullable
    public Icon getIcon(boolean b) {
      return null;
    }
  }
}
