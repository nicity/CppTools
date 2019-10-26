// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.facade;

import com.advancedtools.cpp.psi.MyLookupItem;
import com.intellij.codeInsight.completion.BasicInsertHandler;
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

/**
 * @author maxim
 */
public class EnvironmentFacade {
  private static EnvironmentFacade instance;
  private static boolean javaIde;

  public static EnvironmentFacade getInstance() {
    if (instance == null) {
      ApplicationInfo applicationInfo = ApplicationInfo.getInstance();
      String versionName = applicationInfo.getVersionName();
      javaIde = versionName != null && versionName.indexOf("IDEA") != -1;

      instance = new EnvironmentFacade();
    }

    return instance;
  }

  public static VirtualFile getSdkHomeDirectory(Sdk projectJdk) {
    return VfsUtil.findRelativeFile(projectJdk.getHomePath(), null); // TODO
  }

  public Object createLookupElement(String s) {
    final MyLookupItem item = new MyLookupItem(s);
    LookupItem<MyLookupItem> lookupItem = new LookupItem<MyLookupItem>(item, item.getPresentation());
    lookupItem.setTypeText(item.getTypeHint()).setInsertHandler(new BasicInsertHandler<LookupElement>() {
      @Override
      public void handleInsert(InsertionContext insertionContext, LookupElement lookupElement) {
        super.handleInsert(insertionContext, lookupElement);
        MyLookupItem.insertBracesAndAdvanceCaretWhenCompletingFunction(item, insertionContext.getEditor());
      }
    }).setIcon(item.getIcon(0)).setPriority(item.getPriority());
    return lookupItem;
  }

  public static boolean isJavaIde() {
    if (instance == null) getInstance();
    return javaIde;
  }

  public static class TextExpression extends Expression {
    private final TextResult r;

    public TextExpression(String s) {
      r = new TextResult(s);
    }

    public Result calculateResult(ExpressionContext expressionContext) {
      return r;
    }

    public Result calculateQuickResult(ExpressionContext expressionContext) {
      return r;
    }

    public LookupItem[] calculateLookupItems(ExpressionContext expressionContext) {
      return new LookupItem[0];
    }
  }

  private boolean fileTypeRegistrationAtStartup;

  public void runWriteActionFromComponentInstantiation(final Runnable runnable) {
    final Runnable action = new Runnable() {
      public void run() {
        fileTypeRegistrationAtStartup = true;
        try {
          ApplicationManager.getApplication().runWriteAction(runnable);
        } finally {
          fileTypeRegistrationAtStartup = false;
        }
      }
    };
    if (ApplicationManager.getApplication().isUnitTestMode()) action.run();
    else {
      // TODO: this causes reindexing
      ApplicationManager.getApplication().invokeLater(action);
    }
  }

  public boolean isRealRootChangedEvent(ModuleRootEvent moduleRootEvent) {
    if (fileTypeRegistrationAtStartup) return false;
    return true;
  }
}
