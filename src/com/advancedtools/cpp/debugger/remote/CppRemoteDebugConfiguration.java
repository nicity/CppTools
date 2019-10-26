// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.debugger.remote;

import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.Executor;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.project.Project;
import com.advancedtools.cpp.run.BaseCppConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jdom.Element;

import java.util.StringTokenizer;

/**
 * @author maxim
 * Date: Apr 5, 2009
 * Time: 11:22:12 AM
 */
public class CppRemoteDebugConfiguration extends BaseCppConfiguration<CppRemoteDebugParameters> implements RunConfiguration {
  private static final String HOST_NAME = "host";
  private static final String PORT_NAME = "port";
  private static final String PID_NAME = "pid";

  protected CppRemoteDebugConfiguration(Project project, ConfigurationFactory configurationFactory, String name) {
    super(project, configurationFactory, name);
  }

  @NotNull
  protected CppRemoteDebugParameters createRunnerParameters() {
    return new CppRemoteDebugParameters();
  }

  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new CppRemoteDebugSettingEditor(getProject());
  }

  public com.intellij.execution.configurations.ConfigurationPerRunnerSettings createRunnerSettings(ConfigurationInfoProvider configurationInfoProvider) {
    return null;
  }

  public SettingsEditor<com.intellij.execution.configurations.ConfigurationPerRunnerSettings> getRunnerSettingsEditor(ProgramRunner programRunner) {
    return null;
  }

  protected void fillStateCommandLine(GeneralCommandLine commandLine) {
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    super.checkConfiguration();

    String pid = myRunnerParameters.getPid();
    if (BaseCppConfiguration.isEmpty(pid)) {
      complainIfEmpty(myRunnerParameters.getHost(), "invalid host:");
      complainIfEmpty(myRunnerParameters.getPort(), "invalid port:");
    }

  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);

    String host = myRunnerParameters != null ? myRunnerParameters.getHost():null;
    if (host != null) element.setAttribute(HOST_NAME, host);
    String port = myRunnerParameters != null ? myRunnerParameters.getPort():null;
    if (port != null) element.setAttribute(PORT_NAME, port);

    String pid = myRunnerParameters != null ? myRunnerParameters.getPid():null;
    if (pid != null) element.setAttribute(PID_NAME, port);
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {
    super.readExternal(element);
    myRunnerParameters.setHost(element.getAttributeValue(HOST_NAME));
    myRunnerParameters.setPort(element.getAttributeValue(PORT_NAME));
    myRunnerParameters.setPid(element.getAttributeValue(PID_NAME));
  }
}
