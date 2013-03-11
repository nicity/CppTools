/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.communicator;

import com.intellij.openapi.project.Project;

/**
 * @author maxim
 */
public abstract class CommunicatorCommand {
  private int restartTimestamp;

  public boolean isCancellable() {
    return true;
  }

  public abstract void doExecute();
  public abstract void commandOutputString(String str);
  
  public void commandFinishedString(String str) {}

  public abstract String getCommand();

  public boolean doInvokeInDispatchThread() {
    return false;
  }

  public void doExecuteOnCancel() {}

  public void post(Project project) {
    if (project.isDisposed()) {
      return;
    }
    Communicator.getInstance(project).sendCommand(this);
  }

  public boolean acceptsEmptyResult() {
    return false;
  }

  public void setRestartTimestamp(int restartTimestamp) {
    this.restartTimestamp = restartTimestamp;
  }

  public int getRestartTimestamp() {
    return restartTimestamp;
  }
}
