// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.run;

import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.Executor;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author maxim
 * Date: 28.03.2009
 * Time: 19:35:05
 */
public class CppRunner extends GenericProgramRunner {
  @Nullable
  @Override
  protected RunContentDescriptor doExecute(Project project, RunProfileState runProfileState, RunContentDescriptor runContentDescriptor, ExecutionEnvironment executionEnvironment) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();
    Executor executor = executionEnvironment.getExecutor();

    ExecutionResult executionResult = runProfileState.execute(executor, this);
    if (executionResult == null) return null;
    final RunContentBuilder contentBuilder = new RunContentBuilder(executionResult, executionEnvironment);
    onProcessStarted(executionEnvironment.getRunnerSettings(), executionResult);

    return contentBuilder.showRunContent(runContentDescriptor);
  }

  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    return DefaultRunExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof CppRunConfiguration;
  }

  @NotNull
  public String getRunnerId() {
    return "CppRunner";
  }
}