// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.build;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.execution.filters.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author maxim
*/
public class NMakeBuildHandler extends BaseBuildHandler {
  NMakeBuildHandler(Project _project, VirtualFile _file) {
    super(_project, _file);
  }

  public List<String> getCommandLine(@NotNull BuildTarget buildTarget) {
    List<String> shellCommand = Arrays.asList("nmake", "-f", "\"" + file.getPath() + "\"", " %*");
    if (shellCommand == null) return null;

    if (buildTarget.buildConfiguration != null) shellCommand =BuildUtils.appendOptions(shellCommand, buildTarget.buildConfiguration);
    if (buildTarget.buildAction != null && !BuildTarget.DEFAULT_BUILD_ACTION.equals(buildTarget.buildAction)) {
      shellCommand = BuildUtils.appendOptions(shellCommand, buildTarget.buildAction);
    }
    return shellCommand;
  }

  @NotNull
  public String getBuildTitle(BuildTarget target) {
    return "NMake";
  }

  private static Pattern targetPattern = Pattern.compile("^(\\w+) \\:");

  public @Nullable String[] getAvailableBuildActions() {
    return BuildUtils.getTargetsFromMakeFile(file, targetPattern);
  }

  public @Nullable Filter getOutputFormatFilter() {
    return new MakeFormatFilter(file, project);
  }

  public void afterProcessStarted() {}
  public void afterProcessFinished() {}

  private static class MakeFormatFilter extends BasicFormatFilter {
    public MakeFormatFilter(VirtualFile file, Project project) {
      super(file, project, "^([^\\(]+)\\(([0-9]+)\\) \\: (?:warning|error|fatal error)");
    }
  }

  public String[] getAvailableConfigurations() {
    return new String[] {
      DEBUG_CONFIGURATION_NAME, RELEASE_CONFIGURATION_NAME
    };
  }
}
