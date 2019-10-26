// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.debugger;

import com.advancedtools.cpp.run.BaseCppConfiguration;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author maxim
 * Date: 28.03.2009
 * Time: 19:35:05
 */
public abstract class CppBaseDebugRunner<T extends BaseCppConfiguration> extends GenericProgramRunner {
  @Nullable
  @Override
  protected RunContentDescriptor doExecute(Project project, RunProfileState runProfileState, RunContentDescriptor runContentDescriptor, ExecutionEnvironment env) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();

    final RunProfile runProfile = env.getRunProfile();

    final XDebugSession debugSession =
        XDebuggerManager.getInstance(project).startSession(this, env, runContentDescriptor, new XDebugProcessStarter() {
          @NotNull
          public XDebugProcess start(@NotNull final XDebugSession session) {
            return new CppDebugProcess(session, CppBaseDebugRunner.this, (BaseCppConfiguration)runProfile);
          }
        });

    return debugSession.getRunContentDescriptor();
  }

  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) &&
      profile instanceof BaseCppConfiguration &&
      isSuitableConfiguration((BaseCppConfiguration)profile)
    ;
  }

  protected abstract boolean isSuitableConfiguration(BaseCppConfiguration configuration);


  public abstract @Nullable String getWorkingDirectory(T runConfiguration);

  public abstract String getStartupCommandText(T runConfiguration);

  public abstract String getQuitCommandText(T runConfiguration);

  public abstract String getRunCommandText(T configuration, CppDebugProcess<T> cppDebugProcess);
}