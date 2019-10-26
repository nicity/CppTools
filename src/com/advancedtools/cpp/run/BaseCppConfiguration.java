// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.run;

import com.intellij.execution.configurations.*;
import com.intellij.execution.Executor;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.InvalidDataException;

import java.io.File;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * @author maxim
 * Date: Apr 5, 2009
 * Time: 11:24:02 AM
 */
public abstract class BaseCppConfiguration<T extends BaseCppRunnerParameters> extends RunConfigurationBase {
  private static final String EXECUTABLE_NAME = "executable";
  protected T myRunnerParameters;
  private static final String INVALID_EXECUTABLE_FILE_NAME = "invalid executable file name:";

  protected BaseCppConfiguration(Project project, ConfigurationFactory configurationFactory, String s) {
    super(project, configurationFactory, s);
  }

  public void checkConfiguration() throws RuntimeConfigurationException {
    String executableName = "";

    if(myRunnerParameters == null ||
       complainIfEmpty(executableName = myRunnerParameters.getExecutableName(), INVALID_EXECUTABLE_FILE_NAME) ||
       !new File(executableName).exists()
      ) {
      throw new RuntimeConfigurationError(INVALID_EXECUTABLE_FILE_NAME +executableName);
    }
  }

  protected boolean complainIfEmpty(String value, String message) throws RuntimeConfigurationError {
    if(isEmpty(value)) {
      throw new RuntimeConfigurationError(message + value);
    }
    return false;
  }


  public static boolean isEmpty(String executableName) {
    return executableName == null || executableName.length() == 0;
  }

  public void setRunnerParameters(T runnerParameters) {
    myRunnerParameters = runnerParameters;
  }

  public T getRunnerParameters() {
    return myRunnerParameters;
  }

  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
    CommandLineState state = new CommandLineState(env) {
      protected GeneralCommandLine createCommandLine() throws ExecutionException {
        GeneralCommandLine commandLine = new GeneralCommandLine();

        String exeName = myRunnerParameters.getExecutableName();
        commandLine.setExePath(exeName != null ? exeName:"<unknown-exe-name>");
        fillStateCommandLine(commandLine);

        return commandLine;
      }

      protected OSProcessHandler startProcess() throws ExecutionException {
        GeneralCommandLine commandLine = createCommandLine();
        final OSProcessHandler processHandler = new OSProcessHandler(commandLine.createProcess(), commandLine.getCommandLineString(), commandLine.getCharset());
        ProcessTerminatedListener.attach(processHandler);
        return processHandler;
      }
    };
    state.setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(getProject()));
    return state;
  }

  protected abstract void fillStateCommandLine(GeneralCommandLine commandLine);

  public BaseCppConfiguration clone() {
    final BaseCppConfiguration clone = (BaseCppConfiguration)super.clone();
    clone.myRunnerParameters = null;
    return clone;
  }

@Override
  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);
    String executable = myRunnerParameters != null ? myRunnerParameters.getExecutableName():null;
    if (executable != null) element.setAttribute(EXECUTABLE_NAME, executable);
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {
    super.readExternal(element);
    myRunnerParameters = createRunnerParameters();
    myRunnerParameters.setExecutableName(element.getAttributeValue(EXECUTABLE_NAME));
  }

  protected abstract @NotNull T createRunnerParameters();
}
