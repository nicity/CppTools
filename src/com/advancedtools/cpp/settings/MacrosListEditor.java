// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.settings;

import org.jetbrains.annotations.NotNull;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.advancedtools.cpp.settings.StringListEditor;

/**
 * @author maxim
 */
public class MacrosListEditor extends StringListEditor {
  public MacrosListEditor(String title, @NotNull String pathesList) {
    super(title, pathesList);

    editButton.setVisible(false);

    init();
  }

  protected void insertRowWithDefaultValues(int i) {
    ((DefaultTableModel) stringListTable.getModel()).insertRow(i, new Object[]{"", ""});
  }

  protected void swapRows(int i, int i1) {
    String s = (String) stringListTable.getValueAt(i, 0);
    String s2 = (String) stringListTable.getValueAt(i, 1);
    stringListTable.setValueAt(stringListTable.getValueAt(i1, 0), i, 0);
    stringListTable.setValueAt(stringListTable.getValueAt(i1, 1), i, 1);
    stringListTable.setValueAt(s, i1, 0);
    stringListTable.setValueAt(s2, i1, 1);
  }

  protected Object[] getColumnNames() {
    return new Object[] { "define", "value" };
  }

  protected String[][] buildResultArrayForGivenElementsCount(int i) {
    return new String[i][2];
  }

  protected void addResultToArray(String[][] result, int i, String s) {
    int index = s.indexOf(' ');
    result[i] = new String[] {s.substring(0, index != -1 ? index:s.length()),index != -1 ? s.substring(index + 1):""};
  }

  protected Object getStringAtIndex(TableModel tableModel, int i) {
    String name = (String) tableModel.getValueAt(i, 0);
    String value = (String) tableModel.getValueAt(i, 1);
    if (name.length() == 0) return null;
    if (value.length() == 0) return name;
    return name + " " + value;
  }
}
