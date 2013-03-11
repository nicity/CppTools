package tests;

import junit.framework.TestCase;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.advancedtools.cpp.build.MakeBuildHandler;
import org.jetbrains.annotations.NotNull;

/**
 * @author maxim
 */
public class ToolOutputParsingTest extends TestCase {
  public void testMakeFormat() {
    PatternTester tester = new PatternTester(MakeBuildHandler.MakeFormatFilter.getMatchingPattern());

    tester
      .message("t1.cpp:2: error: redefinition of `float t'")
      .fileExpected("t1.cpp")
      .lineExpected("2")
      .test();

    tester
      .message("Makefile:310: dep/cfserver.outline.d: No such file or directory")
      .fileExpected("Makefile")
      .lineExpected("310")
      .test();

    tester
      .message("pp_tree.cpp: In member function `void PP::Node::dispose() const':")
      .fileExpected("pp_tree.cpp")
      .test();

    tester
      .message("/home/maxim/CF-C/t1.cpp:1:11: error: invalid suffix f")
      .fileExpected("/home/maxim/CF-C/t1.cpp")
      .lineExpected("1")
      .columnExpected("11")
      .test();

    tester
      .message("C:/Cygwin/home/Maxim/CF-C/t1.cpp:1:11: invalid suffix \"fz\" on floating constant")
      .fileExpected("C:/Cygwin/home/Maxim/CF-C/t1.cpp")
      .lineExpected("1")
      .columnExpected("11")
      .test();

  }

  static class PatternTester {
    private final Pattern pattern;
    private String text;
    private String file;
    private String line;
    private String column;

    PatternTester(@NotNull String patternText) {
      pattern = Pattern.compile(patternText);
    }

    PatternTester message(String _text) {
      text = _text;
      line = column = file = null;
      return this;
    }

    PatternTester fileExpected(String _file) {
      file = _file;
      return this;
    }

    PatternTester lineExpected(String _line) {
      line = _line;
      return this;
    }

    PatternTester columnExpected(String _column) {
      column = _column;
      return this;
    }

    PatternTester test() {
      assertNotNull(text);
      Matcher matcher = pattern.matcher(text);

      assertTrue("should match pattern",matcher.find());
      assertEquals(file, matcher.group(1));

      if (line != null) {
        assertTrue(matcher.groupCount() > 1);
        assertEquals(line, matcher.group(2));
      } else {
        assertTrue(matcher.groupCount() <= 1 || matcher.group(2) == null);
      }

      if (column != null) {
        assertTrue(matcher.groupCount() > 2);
        assertEquals(line, matcher.group(3));
      } else {
        assertTrue(matcher.groupCount() <= 2 || matcher.group(3) == null);
      }

      return this;
    }
  }
}
