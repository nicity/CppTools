// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.facade;

import com.advancedtools.cpp.LanguageFeatureAware;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.psi.PsiFile;

/**
* @author maxim
* Date: 2/3/12
* Time: 1:38 PM
*/
public class LanguageAwareStructureViewFactory implements PsiStructureViewFactory {
  public StructureViewBuilder getStructureViewBuilder(PsiFile psiFile) {
    return ((LanguageFeatureAware)psiFile.getLanguage()).getStructureViewBuilder(psiFile);
  }
}
