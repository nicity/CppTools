// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.build;

import com.advancedtools.cpp.communicator.Communicator;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author maxim
*/
public abstract class BaseBuildHandler {
  @NonNls
  public static final String DEBUG_CONFIGURATION_NAME = "Debug";
  @NonNls
  public static final String RELEASE_CONFIGURATION_NAME = "Release";
  
  protected final Project project;
  protected final VirtualFile file;
  protected ConsoleBuilder consoleBuilder;

  protected BaseBuildHandler(final Project _project, final VirtualFile _file) {
    project = _project;
    file = _file;
  }

  public VirtualFile getFile() { return file; }
  public @Nullable abstract List<String> getCommandLine(@NotNull BuildTarget buildTarget);
  public abstract @NotNull @Nls String getBuildTitle(BuildTarget target);

  public abstract @Nullable Filter getOutputFormatFilter();

  public abstract void afterProcessStarted();
  public abstract void afterProcessFinished();

  protected final void addLineToOutput(final String line, final boolean error) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        consoleBuilder.getProcessHandler().notifyTextAvailable(line + "\n",error ?ProcessOutputTypes.STDERR:ProcessOutputTypes.STDOUT);
      }
    });
  }

  public @Nullable BuildState createBuildState(final @NotNull BuildTarget target) {
    List<String> runCommand = getCommandLine(target);

    if (runCommand == null) return null;

    if (target.additionalCommandLineParameters != null) {
      runCommand = BuildUtils.appendAllOptions(runCommand, target.additionalCommandLineParameters);
    }

    return new BuildState(runCommand,new File(file.getParent().getPath()), getEnvironmentVariables());
  }

  public void doBuild(final @NotNull BuildTarget target, final Runnable rerunCallBack) {
    BuildState buildState = createBuildState(target);
    if (buildState == null) return;
    consoleBuilder = new ConsoleBuilder(
      getBuildTitle(target),
      buildState,
      project,
      getOutputFormatFilter(),
      rerunCallBack,
      null,
      null
    ) {

      public void afterProcessStarted() {
        BaseBuildHandler.this.afterProcessStarted();
      }

      public void afterProcessFinished() {
        BaseBuildHandler.this.afterProcessFinished();
      }
    };

    consoleBuilder.start();
  }

  protected @Nullable Map<String, String> getEnvironmentVariables() {
    return null;
  }

  public static @Nullable BaseBuildHandler getBuildHandler(Project project, VirtualFile file) {
    if (file == null) return null;
    if (file.getName().equalsIgnoreCase(Communicator.MAKEFILE_FILE_NAME)) return new MakeBuildHandler(project, file);
    final String extension = file.getExtension();

    if (Communicator.VCPROJ_EXTENSION.equals(extension) || Communicator.DSP_EXTENSION.equals(extension) ||
        Communicator.DSW_EXTENSION.equals(extension) || Communicator.SLN_EXTENSION.equals(extension)
       ) {
      return new VisualStudioBuildHandler(project, file);
    }

    if (Communicator.MAK_EXTENSION.equals(extension)) {
      return new NMakeBuildHandler(project, file);
    }
    return null;
  }

  public @Nullable String[] getAvailableConfigurations() {
    return null;
  }

  public @Nullable String[] getAvailableBuildActions() {
    return null;
  }

}
