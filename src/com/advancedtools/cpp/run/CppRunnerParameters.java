package com.advancedtools.cpp.run;

/**
 * @author maxim
 * Date: 28.03.2009
 * Time: 20:14:54
 */
public class CppRunnerParameters extends BaseCppRunnerParameters {
  private String executableParameters;
  private String workingDir;

  public String getExecutableParameters() {
    return executableParameters;
  }

  public void setExecutableParameters(String executableParameters) {
    this.executableParameters = executableParameters;
  }

  public String getWorkingDir() {
    return workingDir;
  }

  public void setWorkingDir(String launchingPath) {
    this.workingDir = launchingPath;
  }
}
