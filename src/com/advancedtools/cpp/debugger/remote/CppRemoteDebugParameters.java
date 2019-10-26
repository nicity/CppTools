// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.debugger.remote;

import com.advancedtools.cpp.run.BaseCppRunnerParameters;

/**
 * @author maxim
 * Date: Apr 5, 2009
 * Time: 12:04:09 PM
 */
public class CppRemoteDebugParameters extends BaseCppRunnerParameters {
  private String host;
  private String port;
  private String pid;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getPid() {
    return pid;
  }

  public void setPid(String pid) {
    this.pid = pid;
  }
}
