/* AdvancedTools, 2007, all rights reserved */
package com.advancedtools.cpp.hilighting;

/**
* User: maxim
* Date: 17.06.2008
* Time: 21:29:21
*/
public interface AnalyzeProcessor {
  String getAnalizedFileName();
  void startedAnalyzedFileName(String fileName);

  enum MessageType { Error, Warning, Intention, Info }

  void addMessage(MessageType type, int start, int end, String message, Fix ... fixes);
}
