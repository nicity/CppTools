// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
