// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.sdk;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;

import javax.swing.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

/**
 * @author maxim
 * Date: 09.07.2008
 * Time: 9:52:24
 */
class CreateEntryCodeStep extends ModuleWizardStep {
  private CppModuleBuilder myModelBuilder;
  private JPanel myPanel;
  private JComboBox addEntryType;
  private JComboBox buildFileType;
  private JCheckBox myAddEntryPointSwitch;
  private JCheckBox myAddBuildFileSwitch;

  CreateEntryCodeStep(CppModuleBuilder moduleBuilder) {
    myModelBuilder = moduleBuilder;

    myAddBuildFileSwitch.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        buildFileType.setEnabled(myAddBuildFileSwitch.isSelected());
      }
    });
    buildFileType.setModel(new DefaultComboBoxModel(BuildFileType.values()));

    myAddEntryPointSwitch.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        addEntryType.setEnabled(myAddEntryPointSwitch.isSelected());
      }
    });
    addEntryType.setModel(new DefaultComboBoxModel(EntryPointType.values()));
  }

  public JComponent getComponent() {
    return myPanel;
  }

  public void updateDataModel() {
    if (myAddBuildFileSwitch.isSelected()) {
      myModelBuilder.setBuildFileType((BuildFileType) buildFileType.getSelectedItem());
    }
    if (myAddEntryPointSwitch.isSelected()) {
      myModelBuilder.setEntryPointType((EntryPointType) addEntryType.getSelectedItem());
    }
  }

}
