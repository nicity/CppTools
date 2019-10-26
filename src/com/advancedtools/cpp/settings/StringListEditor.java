// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.settings;

import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.advancedtools.cpp.CppSupportSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

/**
 * @author maxim
 */
public abstract class StringListEditor extends DialogWrapper {
  private JButton moveUpButton;
  private JButton moveDownButton;
  protected JTable stringListTable;
  protected JButton editButton;
  private JPanel panel;
  private JButton newButton;
  private JButton removeButton;

  StringListEditor(String title, @NotNull String pathesList) {
    super(findProject(), true);

    stringListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    setTitle(title);

    removeButton.setEnabled(false);
    moveDownButton.setEnabled(false);
    moveUpButton.setEnabled(false);
    editButton.setEnabled(false);

    stringListTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        int selectedRow = stringListTable.getSelectedRow();
        final boolean status = selectedRow != -1;
        editButton.setEnabled(status);
        removeButton.setEnabled(status);
        moveDownButton.setEnabled(status && selectedRow + 1 < stringListTable.getRowCount());
        moveUpButton.setEnabled(status && selectedRow > 0);
      }
    });

    moveDownButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final int i = getActiveRowIndex();

        if (i + 1 < stringListTable.getRowCount() && i >= 0) {
          if (stringListTable.isEditing()) stringListTable.getCellEditor().stopCellEditing();
          swapRows(i,i + 1);
          setActiveRowIndex(i + 1);
        }
      }
    });

    moveUpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final int i = getActiveRowIndex();

        if (i > 0) {
          if (stringListTable.isEditing()) stringListTable.getCellEditor().stopCellEditing();
          swapRows(i-1,i);
          setActiveRowIndex(i - 1);
        }
      }
    });

    newButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int i = getActiveRowIndex() - 1;

        if (i < 0) i = 0;
        if (stringListTable.isEditing()) stringListTable.getCellEditor().stopCellEditing();
        insertRowWithDefaultValues(i);
        stringListTable.getSelectionModel().clearSelection();
        stringListTable.editCellAt(i, 0);
        stringListTable.requestFocusInWindow();
        editButton.setEnabled(true);
      }
    });

    removeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int activeRowIndex = getActiveRowIndex();
        TableModel model = stringListTable.getModel();
        if (activeRowIndex < 0 || activeRowIndex >= model.getRowCount()) return;
        ((DefaultTableModel) model).removeRow(activeRowIndex);
      }
    });

    stringListTable.setModel(
      new DefaultTableModel(
        getObjectsFromSemiColonSeparatedList(pathesList), 
        getColumnNames() 
      )
    );
  }

  private void setActiveRowIndex(int i) {
    stringListTable.setRowSelectionInterval(i,i);
  }

  protected abstract void insertRowWithDefaultValues(int i);
  protected abstract void swapRows(int i, int i1);

  protected void doOKAction() {
    if (stringListTable.isEditing()) stringListTable.getCellEditor().stopCellEditing();
    super.doOKAction();
  }

  protected int getActiveRowIndex() {
    int i = stringListTable.getSelectedRow();
    if (i == -1) i = stringListTable.getEditingRow();
    return i;
  }

  protected static Project findProject() {
    Window mostRecentFocusedWindow = WindowManagerEx.getInstanceEx().getMostRecentFocusedWindow();
    Project project = null;
    while (mostRecentFocusedWindow != null) {
      if (mostRecentFocusedWindow instanceof DataProvider) {
        project = (Project) ((DataProvider) mostRecentFocusedWindow).getData(DataConstants.PROJECT);
        if (project != null) break;
      }
      mostRecentFocusedWindow = (Window) mostRecentFocusedWindow.getParent();
    }
    return project;
  }

  protected String[][] getObjectsFromSemiColonSeparatedList(String dirs) {
    StringTokenizer tokenizer = new StringTokenizer(dirs, CppSupportSettings.PATH_SEPARATOR);
    String[][] result = buildResultArrayForGivenElementsCount(tokenizer.countTokens());
    for (int i = 0; i < result.length; ++i) addResultToArray(result, i, tokenizer.nextToken());
    return result;
  }

  protected abstract Object[] getColumnNames();
  protected abstract String[][] buildResultArrayForGivenElementsCount(int i);

  protected abstract void addResultToArray(String[][] result, int i, String s);
  protected abstract Object getStringAtIndex(TableModel tableModel, int i);

  public String getText() {
    final TableModel tableModel = stringListTable.getModel();
    final int rowCount = tableModel.getRowCount();
    final StringBuilder result = new StringBuilder(rowCount * 50);

    for (int i = 0; i < rowCount; ++i) {
      final Object o = getStringAtIndex(tableModel, i); //tableModel.getValueAt(i, 0);
      if (!(o instanceof String) || ((String)o).length() == 0) continue;
      if (result.length() > 0) result.append(CppSupportSettings.PATH_SEPARATOR);
      result.append(o);
    }
    return result.toString();
  }

  @Nullable
  protected JComponent createCenterPanel() {
    return panel;
  }
}
