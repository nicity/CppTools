// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.psi;

import com.intellij.codeInsight.lookup.LookupValueWithUIHint;
import com.intellij.codeInsight.lookup.LookupValueWithPriority;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.editor.Editor;
import com.intellij.util.PlatformIcons;
import com.intellij.util.text.StringTokenizer;
import com.intellij.util.Icons;
import com.advancedtools.cpp.communicator.Communicator;

import javax.swing.*;
import java.util.NoSuchElementException;
import java.awt.*;

/**
 * @author maxim
*/
public class MyLookupItem implements LookupValueWithUIHint, Iconable, LookupValueWithPriority {
  String type, name, signature;
  Icon icon;
  private static final Icon macroIcon = PlatformIcons.ADVICE_ICON;

  public MyLookupItem(String s) {
    try {
      StringTokenizer tokenizer = new StringTokenizer(s, Communicator.DELIMITER_STRING);
      final String completionType = tokenizer.nextElement();

      name = tokenizer.nextToken();
      signature = tokenizer.nextToken();
      boolean hasFileNameInTypeSignature = false;
      boolean takeReturnOrDeclarationType = false;

      if ("func".equals(completionType)) {
        icon = Icons.METHOD_ICON;
        takeReturnOrDeclarationType = true;
      } else if ("var".equals(completionType)) {
        icon = Icons.VARIABLE_ICON;
        takeReturnOrDeclarationType = true;
      } else if ("type".equals(completionType)) {
        icon = Icons.CLASS_ICON;
      } else if ("macro".equals(completionType)) {
        icon = macroIcon;
        hasFileNameInTypeSignature = true;
      } else if ("macro-param".equals(completionType)) {
        icon = Icons.PARAMETER_ICON;
      } else if ("filename".equals(completionType)) {
        icon = Icons.FILE_ICON;
        hasFileNameInTypeSignature = true;
      }  else if ("dirname".equals(completionType)) {
        icon = Icons.DIRECTORY_OPEN_ICON;
        hasFileNameInTypeSignature = true;
      }

      if (!hasFileNameInTypeSignature && takeReturnOrDeclarationType) {
        final int i = signature.indexOf(name);
        
        if (i != -1) {
          type = signature.substring(0, i).trim();
        } else {
          final int spaceIndex = signature.indexOf(' ');
          type = signature.substring(0, spaceIndex != -1 ? spaceIndex:signature.length());
        }
      } else {
        type = signature;
      }
      
      if (type != null && type.length() > 50) {
        StringBuilder builder = new StringBuilder(50);
        builder.append(type.substring(0, 15));
        builder.append("...");
        builder.append(type.substring(type.length() - 35, type.length()));
        type = builder.toString();
      }
    } catch (NoSuchElementException e1) {
      throw e1;
    }
  }

  public String getPresentation() {
    return name;
  }

  public Icon getIcon(int i) {
    return icon;
  }

  public String getTypeHint() {
    return type;
  }

  public Color getColorHint() {
    return null;
  }

  public boolean isBold() {
    return false;
  }

  public String getSignature() {
    return signature;
  }

  public static void insertBracesAndAdvanceCaretWhenCompletingFunction(Object o, Editor editor) {
    if (!(o instanceof MyLookupItem) || ((MyLookupItem) o).getIcon(0) != Icons.METHOD_ICON) return;

    final int offset = editor.getCaretModel().getOffset();
    final String signature = ((MyLookupItem) o).getSignature();
    int s = signature.indexOf('(');
    int e = signature.indexOf(')', s);
    editor.getDocument().insertString(offset, "()");
    final boolean no_params = s < e && signature.substring(s, e - 1).trim().length() == 0;
    editor.getCaretModel().moveToOffset(offset + (no_params ? 2 : 1));
  }

  public int getPriority() {
    return icon == macroIcon ? NORMAL : HIGHER;
  }
}
