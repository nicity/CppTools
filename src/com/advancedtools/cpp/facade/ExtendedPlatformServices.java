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
