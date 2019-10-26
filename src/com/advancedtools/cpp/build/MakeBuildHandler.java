// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.build;

import com.intellij.execution.filters.Filter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Map;

/**
 * @author maxim
*/
public class MakeBuildHandler extends BaseBuildHandler {
  private @NonNls Map<String, String> myItems;

  MakeBuildHandler(Project _project, VirtualFile _file) {
    super(_project, _file);

    myItems = BuildUtils.buildEnvironmentMap(_project, _file);
  }

  public List<String> getCommandLine(@NotNull BuildTarget buildTarget) {
    String buildAction = getBuildAction(buildTarget);
    return BuildUtils.buildGccToolCall(BuildUtils.MAKE_TOOL_NAME, StringUtil.isEmpty(buildAction) ? Collections.<String>emptyList() : Arrays.asList(buildAction));
  }

  private static @NotNull String getBuildAction(BuildTarget buildTarget) {
    if (buildTarget.buildAction != null && !BuildTarget.DEFAULT_BUILD_ACTION.equals(buildTarget.buildAction)) {
      return buildTarget.buildAction;
    }
    return "";
  }

  @NotNull
  public String getBuildTitle(BuildTarget target) {
    return "Make";
  }

  public @Nullable Filter getOutputFormatFilter() {
    return new MakeFormatFilter(file, project);
  }

  public void afterProcessStarted() {}
  public void afterProcessFinished() {}

  public static class MakeFormatFilter extends BasicFormatFilter {
    public MakeFormatFilter(VirtualFile file, Project project) {
      super(file, project, getMatchingPattern());
    }

    public static String getMatchingPattern() {
      return "^((?:\\w\\:)?[^\\:]+)(?:\\:([0-9]+)\\:(?:([0-9])+\\:)?)?";
    }
  }

  private static Pattern targetPattern = Pattern.compile("^(\\w+)\\:");

  public @Nullable String[] getAvailableBuildActions() {
    return BuildUtils.getTargetsFromMakeFile(file, targetPattern);
  }

  protected @Nullable Map<String, String> getEnvironmentVariables() {
    return myItems;
  }
}
