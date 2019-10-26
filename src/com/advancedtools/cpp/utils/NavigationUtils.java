// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.ide.DataManager;
import com.advancedtools.cpp.usages.FileUsageList;
import com.advancedtools.cpp.usages.FileUsage;
import com.advancedtools.cpp.usages.OurUsage;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.*;

/**
 * @author maxim
 * Date: Sep 24, 2006
 * Time: 5:35:56 PM
 */
public class NavigationUtils {
  public static void navigate(final Project project, final FileUsageList usagesList) {
    if (usagesList == null) {
      WindowManager.getInstance().getStatusBar(project).setInfo("No navigation resolved");
    } else {
      if (usagesList.files.size() == 1 && usagesList.files.get(0).usageList.size() == 1) {
        final FileUsage fileUsage = usagesList.files.get(0);
        final OurUsage ourUsage = fileUsage.usageList.get(0);

        doNavigate(fileUsage, ourUsage,project);
      } else {
        final List<OurUsage> ourUsages = new ArrayList<OurUsage>(5);

        for (FileUsage fileUsage : usagesList.files) {
          for (OurUsage usage : fileUsage.usageList) {
            ourUsages.add(usage);
          }
        }
        final JList list = new JList(new AbstractListModel() {
          public int getSize() {
            return ourUsages.size();
          }

          public Object getElementAt(int index) {
            return ourUsages.get(index);
          }
        });
        list.setCellRenderer(new DefaultListCellRenderer() {
          public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            final OurUsage usage = ((OurUsage) value);
            String text = usage.getContextText() + " in " + usage.fileUsage.getFileLocaton();
            return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
          }
        });

        DataContext dataContext = DataManager.getInstance().getDataContext();
        PopupChooserBuilder builder = JBPopupFactory.getInstance().createListPopupBuilder(list);
        builder.setTitle("Select Target").setItemChoosenCallback(new Runnable() {
              public void run() {
                int selectedIndex = list.getSelectedIndex();
                if (selectedIndex == -1) return;
                final OurUsage selectedUsage = (OurUsage) list.getSelectedValue();

                for (FileUsage fileUsage : usagesList.files) {
                  for (OurUsage usage : fileUsage.usageList) {
                    if (selectedUsage == usage) {
                      doNavigate(fileUsage, usage, project);
                      break;
                    }
                  }
                }
              }
            }).createPopup().showInBestPositionFor(dataContext);
      }
    }
  }

  public static void doNavigate(FileUsage fileUsage, OurUsage ourUsage, Project project) {
    final OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(
      project,
      fileUsage.findVirtualFile(),
      ourUsage.getStart()
    );
    openFileDescriptor.navigate(true);
  }
}
