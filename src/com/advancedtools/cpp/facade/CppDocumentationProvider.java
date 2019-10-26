// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.facade;

import com.advancedtools.cpp.CppSupportLoader;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;

import java.util.Collections;

/**
* @author maxim
* Date: 2/3/12
* Time: 1:40 PM
*/
public class CppDocumentationProvider implements DocumentationProvider {
  public String getQuickNavigateInfo(PsiElement psiElement, PsiElement psiElement1) {
    return CppSupportLoader.getQuickDoc(psiElement);
  }

  public java.util.List<String> getUrlFor(PsiElement psiElement, PsiElement psiElement1) {
    return Collections.emptyList();
  }

  public String generateDoc(PsiElement psiElement, PsiElement psiElement1) {
    return null;
  }

  public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object o, PsiElement psiElement) {
    return null;
  }

  public PsiElement getDocumentationElementForLink(PsiManager psiManager, String s, PsiElement psiElement) {
    return null;
  }
}
