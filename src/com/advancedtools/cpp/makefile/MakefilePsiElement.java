// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.makefile;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

/**
 * User: maxim
 * Date: 01.04.2010
 * Time: 0:09:05
 */
public class MakefilePsiElement extends ASTWrapperPsiElement {
  public MakefilePsiElement(ASTNode astNode) {
    super(astNode);
  }

  public String toString() {
    return "Composite:" + getNode().getElementType();
  }

  @NotNull
  @Override
  public PsiReference[] getReferences() {
    if (MakefileParserDefinition.MakefileParser.shouldProduceComposite(getNode().getElementType()))
      return new PsiReference[] {new MakefileIdentifierReference(this) };
    return super.getReferences();
  }
}
