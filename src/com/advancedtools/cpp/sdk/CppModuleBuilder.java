// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.sdk;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.SourcePathsBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.advancedtools.cpp.CppSupportSettings;
import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.facade.EnvironmentFacade;
import com.advancedtools.cpp.utils.TemplateUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author maxim
 */
public class CppModuleBuilder extends ModuleBuilder implements SourcePathsBuilder {
  private Sdk mySdk;
  private String myContentRootPath;
  private List<Pair<String, String>> mySourcePaths;
  private EntryPointType entryPointType;
  private BuildFileType buildFileType;

  public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException {
    if (mySdk == null) mySdk = CppSdkType.getInstance().getCppSdks().get(0);
    modifiableRootModel.setSdk(mySdk);

    CppSupportSettings.CompilerSelectOptions options = CppSupportSettings.CompilerSelectOptions.AUTO;

    final VirtualFile directory = EnvironmentFacade.getSdkHomeDirectory(mySdk);
    if (CppSupportSettings.isGccPath(directory)) {
      options = CppSupportSettings.CompilerSelectOptions.GCC;
    } else if (CppSupportSettings.isMsVcDirectory(directory)) {
      options = CppSupportSettings.CompilerSelectOptions.MSVC;
    }

    // TODO: support per module configuration
    final CppSupportLoader projectSettings = CppSupportLoader.getInstance(modifiableRootModel.getModule().getProject());
    projectSettings.setCompilerOptions(options);

    final String moduleRootPath = getContentEntryPath();
    if (moduleRootPath != null) {
      LocalFileSystem lfs = LocalFileSystem.getInstance();
      VirtualFile moduleContentRoot = lfs.refreshAndFindFileByPath(FileUtil.toSystemIndependentName(moduleRootPath));
      if (moduleContentRoot != null) {
        final ContentEntry contentEntry = modifiableRootModel.addContentEntry(moduleContentRoot);

        if (mySourcePaths != null) {
          for (Pair<String, String> p : mySourcePaths) {
            final VirtualFile sourcePathDir = VfsUtil.findRelativeFile(p.first, null);
            if (sourcePathDir != null) {
              contentEntry.addSourceFolder(sourcePathDir, false);

              boolean cppStyle = entryPointType == EntryPointType.CPPSTYLE;

              if (entryPointType != null) {
                final String fileName =
                  cppStyle ?
                    TemplateUtils.CPP_MAIN_TEMPLATE_NAME :
                    TemplateUtils.C_MAIN_TEMPLATE_NAME;
                String name = modifiableRootModel.getModule().getName();
                if (cppStyle) name+=".cpp";
                else name += ".c";
                TemplateUtils.createOrResetFileContentFromTemplate(sourcePathDir, name,fileName);
              }

              if (buildFileType != null) {
                final String fileName = /*buildFileType == BuildFileType.MAKEFILE ?*/ TemplateUtils.MAKEFILE_TEMPLATE_NAME;

                String relativeOutputPath = "out/production/"+getName();
                String relativeSourcePath = VfsUtil.getRelativePath(sourcePathDir, moduleContentRoot, '/');
                
                TemplateUtils.createOrResetFileContentFromTemplate(
                  moduleContentRoot, fileName, fileName,
                  "Extension", cppStyle ? "cpp":"c",
                  "Executable", getName() + (SystemInfo.isWindows ? ".exe":""),
                  "SourcePath", (relativeSourcePath + '/'),
                  "OutputPath", (relativeOutputPath + "/")
                );

                VirtualFile child = moduleContentRoot.findChild(fileName);
                CppSupportLoader.getInstance(modifiableRootModel.getModule().getProject()).setProjectFile(child != null ? child.getPath():null);
              }
            }
          }
        }
      }
    }
  }

  public ModuleType getModuleType() {
    return CppModuleType.getInstance();
  }

  @Nullable
  public String getContentEntryPath() {
    return myContentRootPath;
  }

  public void setContentEntryPath(final String moduleRootPath) {
    myContentRootPath = moduleRootPath;
  }

  public void setSourcePaths(final List<Pair<String, String>> paths) {
    mySourcePaths = paths;
  }

  public List<Pair<String, String>> getSourcePaths() {
    return mySourcePaths;
  }

  public void addSourcePath(final Pair<String, String> sourcePathInfo) {
    if (mySourcePaths == null) {
      mySourcePaths = new ArrayList<Pair<String, String>>();
    }
    mySourcePaths.add(sourcePathInfo);
  }

  public void setSdk(Sdk mySdk) {
    this.mySdk = mySdk;
  }

  public void setEntryPointType(EntryPointType entryPointType) {
    this.entryPointType = entryPointType;
  }

  public void setBuildFileType(BuildFileType buildFileType) {
    this.buildFileType = buildFileType;
  }

  public Sdk getSdk() {
    return mySdk;
  }
}
