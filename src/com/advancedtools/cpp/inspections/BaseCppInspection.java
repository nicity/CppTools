// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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