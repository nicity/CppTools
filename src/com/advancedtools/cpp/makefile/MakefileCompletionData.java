/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.makefile;

import com.advancedtools.cpp.psi.MyLookupItem;
import com.advancedtools.cpp.CppSupportSettings;
import com.advancedtools.cpp.CppTokenTypes;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.filters.*;
import com.intellij.psi.filters.position.LeftNeighbour;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.Icons;

/**
 * @author maxim
 * Date: Sep 25, 2006
 * Time: 12:54:19 AM
 */
public class MakefileCompletionData extends CompletionData {
  public MakefileCompletionData() {
    CompletionVariant completionVariant = new CompletionVariant(TrueFilter.INSTANCE);
    completionVariant.includeScopeClass(LeafPsiElement.class);

    completionVariant.addCompletion("ifdef");
    completionVariant.addCompletion("ifndef");
    completionVariant.addCompletion("ifeq");
    completionVariant.addCompletion("else");
    completionVariant.addCompletion("endif");
    completionVariant.addCompletion("error");
    completionVariant.addCompletion("include");

    completionVariant.addCompletionFilterOnElement(TrueFilter.INSTANCE);
    completionVariant.setInsertHandler(new MakefileInsertHandler());
    registerVariant(completionVariant);
  }

  static class MakefileInsertHandler extends BasicInsertHandler {
    public void handleInsert(CompletionContext completionContext, int i, LookupData lookupData, LookupItem lookupItem, boolean b, char c) {
      super.handleInsert(completionContext, i, lookupData, lookupItem, b, c);
    }
  }

}