// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
