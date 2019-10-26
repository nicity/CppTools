// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.inspections;

import com.advancedtools.cpp.CppBundle;
import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.hilighting.HighlightCommand;
import com.advancedtools.cpp.hilighting.HighlightUtils;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author maxim
 */
public class UnusedCppSymbolInspection extends BaseCppInspection {
  @Nls
  @NotNull
  public String getDisplayName() {
    return CppBundle.message("unused.cpp.symbol.inspection.name");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "UnusedCppSymbol";
  }

  @Override
  @Nullable
  public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
    if (file.getFileType() != CppSupportLoader.CPP_FILETYPE) return EMPTY;
    if (HighlightUtils.debug) {
      HighlightUtils.trace(file, null, "Inspections about to start:");
    }
    
    final HighlightCommand command = HighlightUtils.getUpToDateHighlightCommand(file, file.getProject());

    if (command.isUpToDate()) command.awaitInspections(file.getProject());

    if (HighlightUtils.debug) {
      HighlightUtils.trace(file, null, "Adding inspection errors:");
    }
    final ProblemDescriptor[] problemDescriptors = command.addInspectionErrors(manager);
//    System.out.println("Finished inspections--:" + getClass() + "," + (System.currentTimeMillis() - command.started));
    return problemDescriptors;
  }
}
