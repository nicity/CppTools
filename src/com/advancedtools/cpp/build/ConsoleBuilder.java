// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.build;

import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.actions.CloseAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * @author maxim
*/
public class ConsoleBuilder {
  private final BuildState buildState;
  private final Runnable showOptionsCallBack;
  private final Runnable onEndAction;
  private final Runnable toRerunAction;
  private final String title;
  private OSProcessHandler processHandler;
  private final Project project;
  private Filter filter;

  public ConsoleBuilder(String _title,
                 BuildState _buildState,
                 Project _project,
                 Filter _filter,
                 Runnable _showOptionsCallBack,
                 Runnable _toRerunAction,
                 Runnable _onEndAction
                 ) {
    buildState = _buildState;
    title = _title;
    project = _project;
    filter = _filter;
    showOptionsCallBack = _showOptionsCallBack;
    onEndAction = _onEndAction;
    toRerunAction = _toRerunAction;
  }

  public OSProcessHandler getProcessHandler() {
    return processHandler;
  }

  public void start() {
    try {
      BuildState.saveDocuments();
      buildState.start();
    } catch (IOException e) {
      Messages.showErrorDialog(project, "Problem launching "+buildState.getRunCommand() + "\n" + e.getMessage(), "Problem Executing External Program");
      return;
    }

    processHandler = new OSProcessHandler(buildState.getProcess(), buildState.getRunCommand());

    final ProcessListener processListener = new ProcessListener() {
      public void startNotified(ProcessEvent event) {
        afterProcessStarted();
      }

      public void processTerminated(ProcessEvent event) {
        afterProcessFinished();
        processHandler.notifyTextAvailable("Done\n", ProcessOutputTypes.SYSTEM);
        processHandler.removeProcessListener(this);
      }

      public void processWillTerminate(ProcessEvent event, boolean willBeDestroyed) {
      }

      public void onTextAvailable(ProcessEvent event, Key outputType) {}
    };

    processHandler.addProcessListener(processListener);

    ConsoleView myConsoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
    //myConsoleView.setHelpId(handler.getName());
    if (filter != null) myConsoleView.addMessageFilter(filter);

    myConsoleView.attachToProcess(processHandler);

// Runner creating
    final Executor defaultRunner = DefaultRunExecutor.getRunExecutorInstance();
    final DefaultActionGroup toolbarActions = new DefaultActionGroup();

    JPanel content = new JPanel(new BorderLayout());
    content.add(myConsoleView.getComponent(), BorderLayout.CENTER);
    content.add(ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, toolbarActions, false).getComponent(), BorderLayout.WEST);

    final boolean[] errorDetected = new boolean[1];

    final RunContentDescriptor myDescriptor = new RunContentDescriptor(myConsoleView, processHandler,
      content, title);
// adding actions

    toolbarActions.add(new BuildUtils.RerunAction(myConsoleView, processHandler, new Runnable() {
      public void run() {
        if (toRerunAction != null) toRerunAction.run();
        else start();
      }
    }));

    final AnAction closeAction = new CloseAction(defaultRunner, myDescriptor, project);

    toolbarActions.add(new BuildUtils.RefreshAction(myConsoleView, processHandler, new Runnable() {
      public void run() {
        closeAction.actionPerformed(null);
        showOptionsCallBack.run();
      }
    }));

    toolbarActions.add(closeAction);

    ExecutionManager.getInstance(project).getContentManager().showRunContent(defaultRunner, myDescriptor);
    ProgressManager.getInstance().runProcessWithProgressAsynchronously(project, title, new Runnable() {

      public void run() {
        processHandler.startNotify();
        while(!processHandler.isProcessTerminated()) {
          synchronized(this) {
            try {
              wait(1000);
            } catch (InterruptedException e) {
              break;
            }

            try {
              ProgressManager.checkCanceled();
            }
            catch(RuntimeException e) {
              if (!processHandler.isProcessTerminated() && !processHandler.isProcessTerminating()) {
                processHandler.destroyProcess();
              }
              break;
            }
          }
        }

        if (!errorDetected[0] && onEndAction != null) {
          SwingUtilities.invokeLater(onEndAction);
        }
      }
    }, null, null, new PerformInBackgroundOption() {

      public boolean shouldStartInBackground() {
        return true;
      }

      public void processSentToBackground() {}
      public void processRestoredToForeground() {}
    });
  }

  public void afterProcessStarted() {}
  public void afterProcessFinished() {}

  public Process getProcess() { return buildState.getProcess(); }
}