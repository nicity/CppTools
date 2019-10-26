// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.debugger;

import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.project.Project;
import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.CppBundle;
import org.jetbrains.annotations.NotNull;

/**
 * @author maxim
 * Date: 28.03.2009
 * Time: 20:49:33
 */
public class CppBreakpointType extends XLineBreakpointType<XBreakpointProperties> {
  public CppBreakpointType() {
    super("Cpp", CppBundle.message("cpp.breakpoints"));
  }

  public XBreakpointProperties createBreakpointProperties(@NotNull VirtualFile file, int line) {
    return null;
  }

  @Override
  public boolean canPutAt(@NotNull VirtualFile file, int line, @NotNull Project project) {
    return file.getFileType() == CppSupportLoader.CPP_FILETYPE;
  }
}
