/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.inspections;

import com.advancedtools.cpp.CppBundle;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author maxim
 */
public abstract class BaseCppInspection extends LocalInspectionTool {
  protected static final ProblemDescriptor[] EMPTY = new ProblemDescriptor[0];

  @Nls
  @NotNull
  public String getGroupDisplayName() {
    return CppBundle.message("cpp.inspections.group.name");
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }
}