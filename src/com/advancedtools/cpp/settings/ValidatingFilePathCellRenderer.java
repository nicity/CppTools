// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.settings;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.*;
import java.awt.*;

/**
* @author maxim
* Date: 31.05.2009
* Time: 15:40:50
*/
public class ValidatingFilePathCellRenderer extends DefaultTableCellRenderer {
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    boolean validDir = isValidDir(value);
    setForeground(validDir ? Color.black : Color.red);
    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
  }

  protected boolean isValidDir(Object value) {
    return AbstractFileListEditor.getFile(value) != null;
  }
}
