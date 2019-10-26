// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.utils;

import com.advancedtools.cpp.CppSupportSettings;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * User: maxim
 * Date: 20.03.2010
 * Time: 20:04:46
 */
public class LM {
  private static Data data = retrieveData();

  private static Data retrieveData() {
    return new Data();
  }

  private static boolean registered;

  public static void showAboutDialog(DataContext dataContext) {
    new LMDialog((Project) dataContext.getData(DataConstants.PROJECT)).show();
  }

  private static class Data {
    String key = "";
    String user = "";
    String company = "";
  }

  private static class LMDialog extends DialogWrapper {
    private JLabel myLicensedToText;
    private JLabel myLicensedToInfo;
    private JLabel myAboutCppInfo;
    private JPanel myPanel;
    private Project myProject;

    protected LMDialog(Project project) {
      super(project, true);
      myProject = project;
      setTitle("About C/C++ plugin");
      String text = "C/C++ plugin version: ";
      text += CppSupportSettings.getPluginVersion();
      myAboutCppInfo.setText(text);

      updateRegisteredStatus();
      init();
    }

    @Override
    protected Action[] createActions() {
      Action[] actions = new Action[2];
      Action[] superActions = super.createActions();
      actions[0] = superActions[0];
      actions[1] = new AbstractAction("&Register") {
        public void actionPerformed(ActionEvent e) {
          new LMDialog2(myProject).show();
          updateRegisteredStatus();
        }
      };
      return actions;
    }

    private void updateRegisteredStatus() {
      String text;
      if (LM.isRegistered()) {
        text = "";
        if (!StringUtil.isEmpty(data.user)) text += data.user;
        if (!StringUtil.isEmpty(data.company)) {
          if (text.length() != 0) text += ", ";
          text += data.company;
        }
        myLicensedToInfo.setText(text);
        myLicensedToText.setVisible(true);
      } else {
        myLicensedToText.setVisible(false);
        myLicensedToInfo.setText("*Unregistered*");
      }

      pack();
    }

    @Override
    protected JComponent createCenterPanel() {
      return myPanel;
    }
  }

  private static class LMDialog2 extends DialogWrapper {
    private JPanel myPanel;
    private JTextField nameTextField;
    private JTextField companyNameTextField;
    private JTextField licenseKeyTextField;
    private JLabel myLinkText;

    protected LMDialog2(Project project) {
      super(project, true);
      setTitle("Enter License Data");
      licenseKeyTextField.setText(data.key);
      companyNameTextField.setText(data.company);
      nameTextField.setText(data.user);
            
      myLinkText.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          BrowserUtil.launchBrowser("http://www.adv-tools.com/buy");
        }
      });
      init();
    }

    @Override
    protected JComponent createCenterPanel() {
      return myPanel;
    }

    @Override
    protected void doOKAction() {
      String newCompanyName = companyNameTextField.getText();
      String newName = nameTextField.getText();
      String newKey = licenseKeyTextField.getText();

      if (StringUtil.isEmpty(newCompanyName) && StringUtil.isEmpty(newName)) {
        registered = false;
        data.key = "";
        data.company = "";
        data.user = "";
      } else {
        registered = true;
        data.key = newKey;
        data.company = newCompanyName;
        data.user = newName;
      }

      super.doOKAction();
    }
  }

  public static boolean isRegistered() {
    return registered;
  }
}
