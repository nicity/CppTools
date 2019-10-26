// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

/**
 * User: maxim
 * Date: 07.08.2008
 * Time: 4:09:29
 */
public class ErrorReporter extends ErrorReportSubmitter {
  public String getReportActionText() {
    return CppBundle.message("report.to.advancedtools.title");
  }

  public SubmittedReportInfo submit(IdeaLoggingEvent[] ideaLoggingEvents, Component component) {
    StringBuilder builder = new StringBuilder();
    for (IdeaLoggingEvent evt : ideaLoggingEvents) builder.append(evt.getMessage());
    final boolean b = reportBug(builder.toString(), component);
    return new SubmittedReportInfo(null, "email", b ? SubmittedReportInfo.SubmissionStatus.NEW_ISSUE: SubmittedReportInfo.SubmissionStatus.FAILED);
  }

  static boolean debug = false;
  static InputStream in;
  static OutputStream out;
  static byte[] buffer = new byte[500];
  static char[] buf = new char[500];

  static void readAndPrintResp() throws IOException {
    int len = in.read(buffer, 0, buffer.length);
    if (debug) System.out.println(new String(buffer, 0, len));
  }

  static void writeStringReply(String what) throws IOException {
    int len = what.length();
    if (len > buf.length) buf = new char[len];
    what.getChars(0, len, buf, 0);
    if (buf.length > buffer.length) buffer = new byte[buf.length];
    for (int i = 0; i < len; i++) buffer[i] = (byte) buf[i];
    out.write(buffer, 0, len);
    if (debug) System.out.print(what);
  }

  static final class BugReportModel {
    String to;
    String cc;
    String mailserver;
    String mailUser;
    String message;
  }

  static class BugReportForm extends DialogWrapper {
    private JTextPane bugReportText;
    private JTextField mailUser;
    private JTextField mailServer;
    private JPanel myPanel;

    BugReportForm(String _bugReport, Component parent) {
      super(parent, true);

      setOKButtonText("Send");
      setCancelButtonText("Do not send");
      setModal(true);

      if (_bugReport != null && _bugReport.length() > 0) {
        bugReportText.setText(_bugReport);
      } else {
        bugReportText.setVisible(false);
      }

      final CppSupportSettings settings = CppSupportSettings.getInstance();
      mailUser.setText(settings.getMailUser());
      mailServer.setText(settings.getMailServer());

      init();
    }

    @NonNls
    protected String getDimensionServiceKey() {
      return "com.advancedtools.cpp.BugReportForm";
    }

    protected void doOKAction() {
      super.doOKAction();

      final CppSupportSettings settings = CppSupportSettings.getInstance();
      settings.setMailUser(mailUser.getText());
      settings.setMailServer(mailServer.getText());
    }

    @Nullable
    protected JComponent createCenterPanel() {
      return myPanel;
    }
  }

  /**
   * Sends the information to mail server.
   *
   * @param model of bug report
   */
  private static synchronized void sendBugData(BugReportModel model) {
    String mailserver = model.mailserver;
    int mailport = 25;

    try {
      Socket sck = new Socket(mailserver, mailport);
      in = sck.getInputStream();
      out = sck.getOutputStream();

      LineNumberReader is = new LineNumberReader(new StringReader(model.message));
      String from = "CppToolsForIDEA", send, subj = "Bug report for cfserver";
      //System.out.println("'"+from+"'");

      readAndPrintResp();
      writeStringReply("HELO " + model.mailUser + "\n");
      readAndPrintResp();
      writeStringReply("MAIL From: " + from + "\n");
      readAndPrintResp();

      writeStringReply("RCPT To: " + model.to + "\n");
      readAndPrintResp();
      String cc = model.cc;
      if (cc != null && cc.length() > 0) {
        writeStringReply("RCPT TO: " + cc + "\n");
        readAndPrintResp();
      }
      writeStringReply("DATA\n");
      readAndPrintResp();

      writeStringReply("From: " + from + "\n");
      writeStringReply("To: " + model.to + "\n");
      if (cc != null && cc.length() > 0) {
        writeStringReply("Cc: " + cc + "\n");
      }

      writeStringReply("Subject: " + subj + "\n\n");

      while (true) {
        send = is.readLine();
        if (send == null) break;

        writeStringReply(send + "\n");
      }
      is.close();

      writeStringReply(".\n");
      writeStringReply(".\n");
      readAndPrintResp();
      writeStringReply("QUIT");
    } catch (Exception e) {
      if (debug) e.printStackTrace();
    }
  }

  /**
   * Reports a bug with given message
   *
   * @param message of bug description
   */
  public static boolean reportBug(String message, Component comp) {
    final String to = "msk@cpptools.com";

    StringBuffer buf = new StringBuffer(message.length() + 50);

    buf.append("Idea version:");
    buf.append(ApplicationInfo.getInstance().getBuildNumber());
    buf.append('\n');

    buf.append("Plugin version:");
    buf.append(CppSupportSettings.getPluginVersion());
    buf.append('\n');

    buf.append(message);
    BugReportForm form = new BugReportForm(buf.toString(), comp);

    form.show();
    if (form.getExitCode() != DialogWrapper.OK_EXIT_CODE) return false;

    final BugReportModel model = new BugReportModel();

    model.to = to;
    model.mailserver = form.mailServer.getText();
    model.mailUser = form.mailUser.getText();
    model.cc = "support@adv-tools.com";
    model.message = form.bugReportText.getText();

    sendBugData(model);

    return true;
  }
}
