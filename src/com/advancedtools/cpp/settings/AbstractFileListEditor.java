// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author maxim
 */
public abstract class AbstractFileListEditor extends StringListEditor {
  AbstractFileListEditor(String title, @NotNull String pathesList) {
    super(title, pathesList);

    final TableColumn column = stringListTable.getColumnModel().getColumn(0);
    final DefaultTableCellRenderer cellRenderer = new ValidatingFilePathCellRenderer() {
      protected boolean isValidDir(Object value) {
        return isValidFile(value);
      }
    };

    column.setCellEditor(new DefaultCellEditor(new JTextField()) {
      public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        final Component cellEditorComponent = super.getTableCellEditorComponent(table, value, isSelected, row, column);
        boolean validDir = isValidFile(value);
        cellEditorComponent.setForeground(validDir ? Color.black : Color.red);
        return cellEditorComponent;
      }
    });
    column.setCellRenderer(cellRenderer);

    editButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int index = getActiveRowIndex();
        if (index == -1) return;

        Project project = findProject();

        // TODO!
        final FileChooserDescriptor fileChooserDescriptor = configureFileChooserDescriptor();
        fileChooserDescriptor.setTitle(getChooseTitle());
        FileChooserDialog fileChooser = FileChooserFactory.getInstance().createFileChooser(
          fileChooserDescriptor,
          project,
          WindowManagerEx.getInstanceEx().suggestParentWindow(project)
        );

        TableModel model = stringListTable.getModel();
        String lastPathToMSVS = index < model.getRowCount() ? (String) model.getValueAt(index, 0):null;
        VirtualFile file = lastPathToMSVS != null && lastPathToMSVS.length() > 0 ? VfsUtil.findRelativeFile(lastPathToMSVS, null) : null;

        final VirtualFile[] virtualFiles = fileChooser.choose(file, project);
        if (virtualFiles != null && virtualFiles.length > 0) {
          final String value = virtualFiles[0].getPresentableUrl();

          if (index == stringListTable.getSelectedRow()) {
            model.setValueAt(value, index, 0);
          } else {
            ((JTextField) stringListTable.getEditorComponent()).setText(value);
          }
        }
      }
    });
  }

  protected abstract FileChooserDescriptor configureFileChooserDescriptor();

  protected abstract String getChooseTitle();

  protected void insertRowWithDefaultValues(int i) {
    ((DefaultTableModel) stringListTable.getModel()).insertRow(i, new Object[]{""});
  }

  protected void swapRows(int i, int i1) {
    String s = (String) stringListTable.getValueAt(i, 0);
    stringListTable.setValueAt(stringListTable.getValueAt(i1, 0), i, 0);
    stringListTable.setValueAt(s, i1, 0);
  }

  protected Object[] getColumnNames() {
    return new Object[] { "path"};
  }

  protected String[][] buildResultArrayForGivenElementsCount(int i) {
    return new String[i][1];
  }

  protected void addResultToArray(String[][] result, int i, String s) {
    result[i] = new String[] {s};
  }

  protected Object getStringAtIndex(TableModel tableModel, int i) {
    return tableModel.getValueAt(i, 0);
  }

  protected boolean isValidFile(Object value) {
    VirtualFile byPath = getFile(value);
    return byPath != null;
  }

  protected static VirtualFile getFile(Object value) {
    return value != null ? LocalFileSystem.getInstance().findFileByPath(((String) value).replace(File.separatorChar, '/')):null;
  }

}