// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.build;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;

/**
 * @author maxim
 */
public class BuildTarget {
  public final @Nullable String buildConfiguration;
  public final @Nullable String buildAction;
  public final @Nullable String additionalCommandLineParameters;

  @NonNls
  public static final String DEFAULT_BUILD_ACTION = "Default";

  public BuildTarget(@Nullable String _buildConfiguration, @Nullable String _buildAction,
                     @Nullable String _additionalCommandLineParameters) {
    buildAction = _buildAction;
    buildConfiguration = _buildConfiguration;
    additionalCommandLineParameters = _additionalCommandLineParameters;
  }
}
