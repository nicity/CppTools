// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.sdk;

import com.advancedtools.cpp.CppSupportSettings;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ComboboxWithBrowseButton;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author maxim
 */
public class ChooseCppSdkStep extends ModuleWizardStep {
  private final CppModuleBuilder myModuleBuider;
  private JPanel myPanel;
  private ComboboxWithBrowseButton mySdkCombo;

  public ChooseCppSdkStep(CppModuleBuilder myModuleBuider, WizardContext wizardContext) {
    this.myModuleBuider = myModuleBuider;

    mySdkCombo.getComboBox().setEditable(true);
    final CppSupportSettings cppSupportSettings = CppSupportSettings.getInstance();

    final String gccDir = cppSupportSettings.getGccPath();
    final String mingWToolsDirectory = cppSupportSettings.getMingwToolsDir();
    final String msVcDirectory = cppSupportSettings.getVsCDir();

    List<String> model = new ArrayList<String>();
    if (gccDir != null) {
      final VirtualFile relativeFile = VfsUtil.findRelativeFile(gccDir, null);
      if (relativeFile != null) model.add(gccDir);
    }

    if (msVcDirectory != null) {
      final VirtualFile relativeFile = VfsUtil.findRelativeFile(msVcDirectory, null);
      if (relativeFile != null) model.add(msVcDirectory);
    }

    mySdkCombo.getComboBox().setModel(new DefaultComboBoxModel(model.toArray(new Object[model.size()])));

    mySdkCombo.addBrowseFolderListener(
      "Choose Cpp SDK",
      "For GCC select gcc executable, for MSVC select MSVC directory",
      wizardContext.getProject(),
      new FileChooserDescriptor(true, true, false, false, false, false) {
        public boolean isFileSelectable(VirtualFile virtualFile) {
          return super.isFileSelectable(virtualFile) &&
            (CppSupportSettings.isGccPath(virtualFile) || CppSupportSettings.isMsVcDirectory(virtualFile));
        }
      },
      TextComponentAccessor.STRING_COMBOBOX_WHOLE_TEXT
    );
  }

  public JComponent getPreferredFocusedComponent() {
    return mySdkCombo.getComboBox();
  }

  public JComponent getComponent() {
    return myPanel;
  }

  public boolean validate() {
    Object item = mySdkCombo.getComboBox().getEditor().getItem();
    if (item == null) {
      final int i = Messages.showYesNoDialog("Continue to work without SDK defined", "Confirm SDK version", Messages.getWarningIcon());
      return i == DialogWrapper.OK_EXIT_CODE;
    }
    return true;
  }

  public void updateDataModel() {
    final Object o = mySdkCombo.getComboBox().getEditor().getItem();

    if (o instanceof String) {
      final String s = (String) o;
      myModuleBuider.setSdk(CppSdkType.getInstance().createOrGetSdkByPath(s));
      final VirtualFile relativeFile = VfsUtil.findRelativeFile(s, null);

      if (relativeFile != null) {
        if (CppSupportSettings.isGccPath(relativeFile)) {
          CppSupportSettings.getInstance().setGccPath(s);
          final File file = new File(new File(s).getParentFile(), "gdb" + (SystemInfo.isWindows ? ".exe" : ""));
          final boolean gdbExists = file.exists();

          if (gdbExists) {
            CppSupportSettings.getInstance().setGdbPath(file.getPath());
          }
        } else if (CppSupportSettings.isMsVcDirectory(relativeFile)) {
          CppSupportSettings.getInstance().setVsCDir(s);
        }
      }
    }
  }
}
