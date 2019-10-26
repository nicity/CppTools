// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.debugger.remote;

import com.advancedtools.cpp.debugger.CppBaseDebugRunner;
import com.advancedtools.cpp.debugger.CppDebugProcess;
import com.advancedtools.cpp.run.BaseCppConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * @author maxim
 * Date: Apr 5, 2009
 * Time: 11:13:29 AM
 */
public class CppRemoteDebugRunner extends CppBaseDebugRunner<CppRemoteDebugConfiguration> {
  @NotNull
  public String getRunnerId() {
    return "CppRemoteDebugRunner";
  }

  protected boolean isSuitableConfiguration(BaseCppConfiguration configuration) {
    return configuration instanceof CppRemoteDebugConfiguration;
  }

  public String getWorkingDirectory(CppRemoteDebugConfiguration runConfiguration) {
    return null;
  }

  public String getStartupCommandText(CppRemoteDebugConfiguration runConfiguration) {
    CppRemoteDebugParameters debugParameters = runConfiguration.getRunnerParameters();
    if (debugParameters.getPid() != null) {
      return "attach " + debugParameters.getPid();
    }
    return "target remote "+ debugParameters.getHost() + ":" + debugParameters.getPort();
  }

  @Override
  public String getQuitCommandText(CppRemoteDebugConfiguration runConfiguration) {
    return "continue\ndetach\nquit\ny"; // TODO: gdb forks process and may be we need to close copy
  }

  public String getRunCommandText(CppRemoteDebugConfiguration configuration, CppDebugProcess<CppRemoteDebugConfiguration> cppRemoteDebugConfigurationCppDebugProcess) {
    return "run";
  }
}
