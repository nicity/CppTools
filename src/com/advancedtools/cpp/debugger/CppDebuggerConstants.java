package com.advancedtools.cpp.debugger;

import com.intellij.openapi.util.SystemInfo;

/**
 * @author maxim
 * Date: 05.04.2009
 * Time: 0:04:51
 */
public class CppDebuggerConstants {
  public static final boolean gdbCanNotRedirectOutputOfDebuggedCommand = SystemInfo.isWindows;
}
