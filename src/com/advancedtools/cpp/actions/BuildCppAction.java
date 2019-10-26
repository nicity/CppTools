// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.actions;

import com.advancedtools.cpp.CppSupportLoader;
import com.advancedtools.cpp.build.BaseBuildHandler;
import com.advancedtools.cpp.build.BuildTarget;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.LocalFileSystem;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

/**
 * @author maxim
 */
public class BuildCppAction extends AnAction {
  public void actionPerformed(AnActionEvent anActionEvent) {
    final DataContext dataContext = anActionEvent.getDataContext();
    final Project project = (Project) dataContext.getData(DataConstants.PROJECT);
    final VirtualFile file = (VirtualFile) dataContext.getData(DataConstants.VIRTUAL_FILE);

    doRun(file, project);
  }

  private void doRun(final VirtualFile file, final Project project) {
    final BuildPropertiesDialog dialog = new BuildPropertiesDialog(file, project);
    dialog.show();

    if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
      final CppSupportLoader loader = CppSupportLoader.getInstance(project);

      final String configuration = dialog.getSelectedBuildConfiguration();
      if (configuration != null) loader.setActiveConfiguration(configuration);

      final String buildAction = dialog.getSelectedBuildAction();
      if (buildAction != null) loader.setLastBuildAction(buildAction);

      String commandLineBuildParameters = dialog.getAdditionalCommandLineBuildParameters();
      loader.setAdditionalCommandLineBuildParameters(commandLineBuildParameters);

      dialog.getBuildHandler().doBuild(
        new BuildTarget(configuration, buildAction, commandLineBuildParameters), new Runnable() {
          public void run() {
            doRun(dialog.getBuildHandler().getFile(), project);
          }
        }
      );
    }
  }

  private static class BuildPropertiesDialog extends DialogWrapper {
    private BaseBuildHandler myBuildHandler;
    private JPanel panel;
    private JComboBox myConfigurationsCombo;
    private JLabel myConfigurationsText;
    private JLabel myBuildActionsText;
    private JComboBox myBuildActionsCombo;
    private JLabel myAdditionalCommandLineBuildParametersText;
    private JTextField myAdditionalCommandLineBuildParameters;
    private JComboBox myBuildFileCombo;

    protected BuildPropertiesDialog(VirtualFile file, final Project project) {
      super(project, false);
      setModal(true);

      BaseBuildHandler buildHandler = file != null ? BaseBuildHandler.getBuildHandler(project, file):null;

      final CppSupportLoader loader = CppSupportLoader.getInstance(project);
      final Set<VirtualFile> files = loader.getProjectAndBuildFilesSet();
      final List<BaseBuildHandler> handlers = new ArrayList<BaseBuildHandler>();

      if (buildHandler == null) file = LocalFileSystem.getInstance().findFileByPath(loader.getProjectFile());
      for(VirtualFile bfile:files) {
        BaseBuildHandler mbuildHandler = BaseBuildHandler.getBuildHandler(project, bfile);
        if (mbuildHandler != null) {
          if (file == bfile) buildHandler = mbuildHandler;
          handlers.add(mbuildHandler);
        }
      }

      myBuildFileCombo.setModel(new DefaultComboBoxModel(handlers.toArray(new Object[handlers.size()])));
      if (buildHandler == null && handlers.size() > 0) buildHandler = handlers.get(0);
      myBuildFileCombo.setSelectedItem(buildHandler);

      myBuildFileCombo.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          return super.getListCellRendererComponent(
            list,
            value instanceof BaseBuildHandler? ((BaseBuildHandler)value).getFile().getPath():"",
            index,
            isSelected,
            cellHasFocus
          );
        }
      });

      myBuildFileCombo.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          final BaseBuildHandler baseBuildHandler = (BaseBuildHandler) myBuildFileCombo.getSelectedItem();
          if (baseBuildHandler != null) setBuildHanler(project, baseBuildHandler);
        }
      });

      if (buildHandler != null) setBuildHanler(project, buildHandler);

      setTitle("Set Build Parameters");
      init();
    }

    private void setBuildHanler(Project project, BaseBuildHandler buildHandler) {
      myBuildHandler = buildHandler;

      final CppSupportLoader loader = CppSupportLoader.getInstance(project);

      initBuildTargetField(
        myConfigurationsCombo,
        myConfigurationsText,
        buildHandler.getAvailableConfigurations(),
        loader.getActiveConfiguration()
      );

      initBuildTargetField(
        myBuildActionsCombo,
        myBuildActionsText,
        buildHandler.getAvailableBuildActions(),
        loader.getLastBuildAction()
      );

      String commandLineBuildParameters = loader.getAdditionalCommandLineBuildParameters();
      if (commandLineBuildParameters != null) myAdditionalCommandLineBuildParameters.setText(commandLineBuildParameters);

      pack();
    }

    private static void initBuildTargetField(JComboBox configurationsCombo, JLabel configurationsText, String[] availableConfigurations, String activeConfiguration) {
      if (availableConfigurations != null) {
        configurationsCombo.setVisible(true);
        configurationsText.setVisible(true);
        configurationsCombo.setModel(new DefaultComboBoxModel(availableConfigurations));

        for(String configuration:availableConfigurations) {
          if (configuration.equals(activeConfiguration)) {
            configurationsCombo.setSelectedItem(activeConfiguration);
            break;
          }
        }
      } else {
        configurationsCombo.setVisible(false);
        configurationsText.setVisible(false);
      }
    }

    @Nullable
    protected JComponent createCenterPanel() {
      return panel;
    }

    public String getSelectedBuildConfiguration() {
      return (String) myConfigurationsCombo.getSelectedItem();
    }

    public String getSelectedBuildAction() {
      return (String) myBuildActionsCombo.getSelectedItem();
    }

    public String getAdditionalCommandLineBuildParameters() {
      return myAdditionalCommandLineBuildParameters.getText();
    }

    public BaseBuildHandler getBuildHandler() {
      return myBuildHandler;
    }
  }

  public void update(AnActionEvent e) {
    super.update(e);

    final DataContext dataContext = e.getDataContext();
    final Project project = (Project) dataContext.getData(DataConstants.PROJECT);
    final VirtualFile file = (VirtualFile) dataContext.getData(DataConstants.VIRTUAL_FILE);

    BaseBuildHandler buildHandler = null;

    if (project != null && file != null &&
        !file.isDirectory()
       ) {
       buildHandler = BaseBuildHandler.getBuildHandler(project, file);
    }
    final boolean enabled = buildHandler != null;
    
    e.getPresentation().setEnabled(enabled);
    e.getPresentation().setVisible(enabled);

    final String s = "Do &build for " + (buildHandler != null ? file.getName():"makefile/dsp/dsw file");
    e.getPresentation().setText(s);
    e.getPresentation().setDescription(s);
  }

}
