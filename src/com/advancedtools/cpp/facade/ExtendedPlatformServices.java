// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.facade;

import com.advancedtools.cpp.CppSupportLoader;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.project.Project;

/**
 * @author maxim
 * Date: 2/24/12
 * Time: 11:47 AM
 */
public class ExtendedPlatformServices {

  public static void registerCompilerStuff(Project project) {
    CompilerManager.getInstance(project).addCompilableFileType(CppSupportLoader.CPP_FILETYPE);
    CompilerManager.getInstance(project).addCompiler(new CppCompiler());
  }
}
