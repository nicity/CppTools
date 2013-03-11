/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.facade;

import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.advancedtools.cpp.psi.MyLookupItem;
import com.advancedtools.cpp.sdk.CppSdkType;
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
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.BuildNumber;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;

import javax.swing.*;

/**
 * @author maxim
 */
public abstract class EnvironmentFacade {
  private static EnvironmentFacade instance;

  private static boolean warnedOnUnsupportedPlatform;
  private static boolean javaIde;

  public static EnvironmentFacade getInstance() {
    if (instance == null) {
      ApplicationInfo applicationInfo = ApplicationInfo.getInstance();
      String majorVersion = applicationInfo.getMajorVersion();
      BuildNumber build = applicationInfo.getBuild();

      String versionName = applicationInfo.getVersionName();
      javaIde = versionName != null && versionName.indexOf("IDEA") != -1;

      int baselineVersion = build.getBaselineVersion();
      int idea11BaseLineVersionStart = 111;
      int idea12BaseLineVersionStart = 123;
      final boolean is11 = "11".equals(majorVersion) || baselineVersion >= idea11BaseLineVersionStart;
      final boolean is12 = "12".equals(majorVersion) || baselineVersion >= idea12BaseLineVersionStart;
      String shortClassName = is12 ? "Leda":is11 ? "Nika":null;

      if (shortClassName == null) {
        if (!warnedOnUnsupportedPlatform) {
          BuildingCommandHelper.executeOnEdt(new Runnable() {
            public void run() {
              if (warnedOnUnsupportedPlatform) return;
              warnedOnUnsupportedPlatform = true;
              Messages.showErrorDialog("C++ plugin does not support this IDEA version, please uninstall the plugin for stable work", "C++ Plugin Problem");
            }
          });
        }
      }
      String className = "com.advancedtools.cpp.facade." + shortClassName + "Facade";

      try {
        instance = (EnvironmentFacade) Class.forName(className).newInstance();
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }

    return instance;
  }

  public static VirtualFile getSdkHomeDirectory(Sdk projectJdk) {
    return VfsUtil.findRelativeFile(projectJdk.getHomePath(), null); // TODO
  }

  public void invokeCodeCompletionHandler(Project project, Editor selectedEditor, PsiFile psiFile) {
    new CodeCompletionHandlerBase(CompletionType.BASIC).invokeCompletion(project, selectedEditor);
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

  public abstract Icon getVerifiedBreakpointIcon();

  public abstract Object addModuleRootListener(Project project, ModuleRootListener moduleRootListener);

  public abstract void removeModuleRootListener(Object rootListenerConnectionData, ModuleRootListener rootListener);

  public abstract Icon getStackFrameIcon();

  public abstract Icon getAntMetaTargetIcon();

  public abstract boolean isSdkOfType(Sdk jdk, SdkType sdkType);

  public abstract Sdk createSdk(String name, SdkType sdkType);

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
