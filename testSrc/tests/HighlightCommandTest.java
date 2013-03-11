package tests;

import com.advancedtools.cpp.hilighting.AnalyzeProcessor;
import com.advancedtools.cpp.hilighting.Fix;
import com.advancedtools.cpp.hilighting.HighlightCommand;
import gnu.trove.TIntObjectHashMap;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim
 * Date: 16.08.2010
 * Time: 12:18:44
 */
public class HighlightCommandTest extends TestCase {
  static class MessageInfo {
    final int start;
    final int end;
    final AnalyzeProcessor.MessageType type;
    final Fix[] fixes;
    final String text;

    public MessageInfo(int start, int end, String text, AnalyzeProcessor.MessageType type, Fix[] fixes) {
      this.start = start;
      this.end = end;
      this.text = text;
      this.type = type;
      this.fixes = fixes;
    }
  }
  public void test() {
    HighlightCommand.initErrorsDataInTest("16|SEMANTIC|Undefined identifier `%0'|1", "112|MODE|void function cannot return value|0");
    final List<MessageInfo> messages = new ArrayList<MessageInfo>(2);
    HighlightCommand.processErrorInfoFromString("27483|27486|16|res|112|fix:24|fix:27", new AnalyzeProcessor() {
      public String getAnalizedFileName() {
        return "foo.cpp";
      }

      public void startedAnalyzedFileName(String fileName) {}

      public void addMessage(MessageType type, int start, int end, String message, Fix... fixes) {
        messages.add(new MessageInfo(start, end, message, type, fixes));
      }
    });

    assertEquals(2, messages.size());
    MessageInfo messageInfo = messages.get(0);
    assertEquals(messageInfo.start, 27483);
    assertEquals(messageInfo.end, 27486);
    assertEquals(messageInfo.text, "Undefined identifier `res'");
    assertEquals(messageInfo.type, AnalyzeProcessor.MessageType.Error);
    assertEquals(2, messageInfo.fixes.length);

    messageInfo = messages.get(1);
    assertEquals(messageInfo.start, 27483);
    assertEquals(messageInfo.end, 27486);
    assertEquals(messageInfo.text, "void function cannot return value");
    assertEquals(messageInfo.type, AnalyzeProcessor.MessageType.Error);
    assertEquals(0, messageInfo.fixes.length);
  }
}
