// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.debugger.remote;

import com.advancedtools.cpp.run.BaseCppRunSettingsEditor;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.TextFieldWithStoredHistory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author maxim
 * Date: Apr 5, 2009
 * Time: 12:06:02 PM
 */
class CppRemoteDebugSettingEditor extends BaseCppRunSettingsEditor<CppRemoteDebugConfiguration> {
  private JPanel myPanel;
  private ComboboxWithBrowseButton myExecutableName;
  private TextFieldWithStoredHistory myHost;
  private TextFieldWithStoredHistory myPort;
  private TextFieldWithStoredHistory myPid;

  CppRemoteDebugSettingEditor(Project project) {
    setupCommonUI(project);
  }

  protected ComboboxWithBrowseButton getExecutableName() {
    return myExecutableName;
  }

  @NotNull
  protected JComponent createEditor() {
    return myPanel;
  }

  protected void disposeEditor() {
    myPanel = null;
    myExecutableName = null;
    myHost = null;
    myPort = null;
    myPid = null;
  }

  @Override
  protected void resetEditorFrom(CppRemoteDebugConfiguration s) {
    super.resetEditorFrom(s);

    CppRemoteDebugParameters runnerParameters = s.getRunnerParameters();
    myHost.setText(runnerParameters != null && runnerParameters.getHost() != null ? runnerParameters.getHost():"");
    myPort.setText(runnerParameters != null && runnerParameters.getPort() != null ? runnerParameters.getPort():"");
    myPid.setText(runnerParameters != null && runnerParameters.getPid() != null ? runnerParameters.getPid():"");
  }

  @Override
  protected void applyEditorTo(CppRemoteDebugConfiguration s) throws ConfigurationException {
    super.applyEditorTo(s);

    CppRemoteDebugParameters runnerParameters = s.getRunnerParameters();
    runnerParameters.setPort(myPort.getText());
    runnerParameters.setHost(myHost.getText());
    runnerParameters.setPid(myPid.getText());
  }

  private void createUIComponents() {
    myHost = new TextFieldWithStoredHistory("cpp.remote.debug.host");
    myPort = new TextFieldWithStoredHistory("cpp.remote.debug.port");
    myPid = new TextFieldWithStoredHistory("cpp.remote.debug.pid");
  }
}
