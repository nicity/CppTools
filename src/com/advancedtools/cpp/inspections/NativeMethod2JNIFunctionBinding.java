/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.inspections;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.advancedtools.cpp.commands.FindSymbolsCommand;
import com.advancedtools.cpp.usages.FileUsageList;
import org.jetbrains.annotations.NotNull;

/**
 * @author maxim
 */
public class NativeMethod2JNIFunctionBinding {
  private final FileUsageList usagesList;

  public NativeMethod2JNIFunctionBinding(@NotNull PsiMethod method) {
    final StringBuilder b = new StringBuilder();
    b.append(JNIFunction2JavaMethodBinding.JAVA_NATIVE_PREFIX)
      .append(method.getContainingClass().getQualifiedName().replace('.','_'))
      .append("_").append(method.getName());
    final FindSymbolsCommand myNavigationCommand = new FindSymbolsCommand(b.toString(), FindSymbolsCommand.TargetTypes.SYMBOLS);
    myNavigationCommand.post(method.getProject());
    usagesList = myNavigationCommand.getUsagesList();
  }

  public FileUsageList getUsagesList() {
    return usagesList;
  }
}
