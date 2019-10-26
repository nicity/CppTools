// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.commands;

import com.advancedtools.cpp.communicator.BlockingCommand;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.advancedtools.cpp.communicator.Communicator;
import com.advancedtools.cpp.usages.FileUsageList;
import com.advancedtools.cpp.usages.FileUsage;
import com.advancedtools.cpp.usages.OurUsage;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;

public class FindUsagesCommand extends BlockingCommand {
  protected final String filePath;
  protected final long offset;
  protected int lineCount;
  protected FileUsage currentFileUsage;
  protected FileUsageList usagesList;
  protected int usageCount;
  private final boolean renameAction;
  public static final int MAGIC_FILE_OFFSET = -1;
  @NonNls
  private static final String FIND_USAGES_COMMAND_NAME = "find-usages ";

  public FindUsagesCommand(String _filePath, int _offset) {
    this(_filePath, _offset, false);
  }

  public FindUsagesCommand(String _filePath, int _offset, boolean _renameAction) {
    filePath = BuildingCommandHelper.fixVirtualFileName(_filePath);
    offset = _offset;
    renameAction = _renameAction;
  }

  public void commandOutputString(String str) {
    if (str.startsWith("AT:|")) {
      int offset = str.indexOf(Communicator.DELIMITER, 4) + 1;

      String fileName = str.substring(4,offset - 1);
      initFileUsage(fileName);

      int offset2 = str.indexOf(Communicator.DELIMITER, offset) + 1;
      int offset3 = str.indexOf(Communicator.DELIMITER, offset2) + 1;

      doAddUsage(str, offset, offset2, offset3);

    } else {
      try {
        if (lineCount > 0) {
          int offset = str.indexOf(Communicator.DELIMITER, lineCount == 0 ? 4:0) + 1;

          if (offset == 0) {
            return;
          }
          String fileName = str.substring(0,offset - 1);
          if (fileName.length() == 0) return;
          initFileUsage(fileName);

          int offset2 = str.indexOf(Communicator.DELIMITER, offset) + 1;
          int offset3 = Communicator.findDelimiter(str,offset2);
          int offset4 = Communicator.findDelimiter(str, offset3);

          final OurUsage ourUsage = doAddUsage(str, offset, offset2, offset3);

          if (offset4 != str.length() && ourUsage != null) {
            int offset5 = Communicator.findDelimiter(str, offset4);
            int offset6 = Communicator.findDelimiter(str, offset5);
            int offset7 = Communicator.findDelimiter(str, offset6);
            ourUsage.text = str.substring(offset3, offset4 - 1);
            ourUsage.line = Integer.parseInt(str.substring(offset4,offset5 - 1));
            ourUsage.startColumn = Integer.parseInt(str.substring(offset5,offset6 - 1));
            ourUsage.endColumn = Integer.parseInt(str.substring(offset6,offset7 != str.length() ? offset7 - 1:str.length()));
            if (offset7 != str.length()) ourUsage.context = str.substring(offset7).replace("::",".");
          }
        } else {
          // skip one-of or name of the navigation
        }
      } catch (NumberFormatException e) {
        int a = 1;
      }
    }

    ++lineCount;
  }

  private void initFileUsage(String fileName) {
    if (usagesList == null) usagesList = new FileUsageList();
    if (!Character.isDigit(fileName.charAt(0))) {
      currentFileUsage = findFileUsage(fileName);
      if (currentFileUsage == null) {
        currentFileUsage = new FileUsage(fileName);
        doAddFileUsage(currentFileUsage);
      }
    }
  }

  protected void doAddFileUsage(FileUsage _fileUsage) {
    usagesList.files.add(_fileUsage);
  }

  protected FileUsage findFileUsage(String fileName) {
    return null;
  }

  private @Nullable OurUsage doAddUsage(String str, int offset, int offset2, int offset3) {
    if (currentFileUsage == null) return null;
    int start = Integer.parseInt(str.substring(offset, offset2 - 1));
    int end = Integer.parseInt(str.substring(offset2, offset3 - 1));

    OurUsage usage = findUsage(currentFileUsage, start,end);
    if (usage != null) return usage;

    ++usageCount;
    final OurUsage ourUsage = new OurUsage( start, end, currentFileUsage );
    doAddUsage(currentFileUsage, ourUsage);
    return ourUsage;
  }

  protected OurUsage findUsage(FileUsage currentFileUsage, int start, int end) {
    return null;
  }

  protected void doAddUsage(FileUsage fileUsage, OurUsage ourUsage) {
    fileUsage.usageList.add(
      ourUsage
    );
  }

  public String getCommand() {
    final String s = BuildingCommandHelper.quote(filePath);
    if (offset == MAGIC_FILE_OFFSET) return FIND_USAGES_COMMAND_NAME + s;
    return (renameAction ? "rename ": FIND_USAGES_COMMAND_NAME) + s + " " + offset;
  }

  public FileUsageList getUsagesList() {
    return usagesList;
  }

  public int getUsageCount() {
    return usageCount;
  }
}
