// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.facade;

import com.advancedtools.cpp.CppParserDefinition;
import com.advancedtools.cpp.psi.ICppCodeFragment;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.file.impl.FileManager;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

/**
* @author maxim
* Date: 2/2/12
* Time: 7:49 PM
*/
public class CppCodeFragment extends CppParserDefinition.CppFile implements ICppCodeFragment {
  private GlobalSearchScope myResolveScope;
  private boolean myPhysical = true;
  private SingleRootFileViewProvider myViewProvider;

  public CppCodeFragment(@NotNull FileViewProvider fileViewProvider) {
    super(fileViewProvider);
    ((SingleRootFileViewProvider)fileViewProvider).forceCachedPsi(this);
  }

  protected CppCodeFragment clone() {
    final CppCodeFragment clone = (CppCodeFragment)cloneImpl((FileElement)calcTreeElement().clone());
    clone.myPhysical = false;
    clone.myOriginalFile = this;
    FileManager fileManager = ((PsiManagerEx)getManager()).getFileManager();
    SingleRootFileViewProvider cloneViewProvider = (SingleRootFileViewProvider)fileManager.createFileViewProvider(new LightVirtualFile(getName(), getLanguage(), getText()), false);
    cloneViewProvider.forceCachedPsi(clone);
    clone.myViewProvider = cloneViewProvider;
    return clone;
  }

  @NotNull
  public FileViewProvider getViewProvider() {
    if(myViewProvider != null) return myViewProvider;
    return super.getViewProvider();
  }

  public void forceResolveScope(GlobalSearchScope globalSearchScope) {
    myResolveScope = globalSearchScope;
  }

  public GlobalSearchScope getForcedResolveScope() {
    return myResolveScope;
  }

  @Override
  public boolean isPhysical() {
    return myPhysical;
  }
}
