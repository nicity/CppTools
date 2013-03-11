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

/**
 * @author maxim
 * Date: 28.03.2009
 * Time: 19:35:05
 */
public class CppRunner extends GenericProgramRunner {
  protected RunContentDescriptor doExecute(final Project project,
                                           final Executor executor,
                                           final RunProfileState state,
                                           final RunContentDescriptor contentToReuse,
                                           final ExecutionEnvironment env) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();
    final RunContentBuilder contentBuilder = new RunContentBuilder(project, this, executor);
    contentBuilder.setEnvironment(env);
    ExecutionResult executionResult = state.execute(executor, this);
    if (executionResult == null) return null;
    onProcessStarted(env.getRunnerSettings(), executionResult);
    contentBuilder.setExecutionResult(executionResult);
    RunContentDescriptor contentDescriptor = contentBuilder.showRunContent(contentToReuse);
    return contentDescriptor;
  }

  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    return DefaultRunExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof CppRunConfiguration;
  }

  @NotNull
  public String getRunnerId() {
    return "CppRunner";
  }
}