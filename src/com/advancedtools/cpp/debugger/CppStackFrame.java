package com.advancedtools.cpp.debugger;

import com.advancedtools.cpp.debugger.commands.CppDebuggerContext;
import com.advancedtools.cpp.debugger.commands.DebuggerCommand;
import com.advancedtools.cpp.facade.EnvironmentFacade;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.*;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Icons;
import com.intellij.util.text.CharArrayUtil;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
* User: maxim
* Date: 30.03.2009
* Time: 1:12:06
*/
public class CppStackFrame extends XStackFrame {
  private String myScope;
  private int myFrameIndex;
  private final XSourcePosition sourcePosition;
  private final CppDebuggerContext context;
  private static final String EQ_MARKER = " = ";
  private CppThreadStackInfo threadStackInfo;

  public CppStackFrame(String scope, XSourcePosition _sourcePosition, CppDebuggerContext _context, int frameIndex) {
    sourcePosition = _sourcePosition;
    myScope = scope;
    context = _context;
    myFrameIndex = frameIndex;
  }

  @Override
  public XSourcePosition getSourcePosition() {
    return sourcePosition;
  }

  @Override
  public void customizePresentation(SimpleColoredComponent component) {
    XSourcePosition position = getSourcePosition();
    component.append(myScope, SimpleTextAttributes.REGULAR_ATTRIBUTES);
    component.append(" in ", SimpleTextAttributes.REGULAR_ATTRIBUTES);

    if (position != null) {
      component.append(position.getFile().getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
      component.append(":" + position.getLine(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }
    else {
      component.append("<file name is not available>", SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }
    component.setIcon(EnvironmentFacade.getInstance().getStackFrameIcon());
  }

  @Override
  public void computeChildren(@NotNull final XCompositeNode xCompositeNode) {
    context.sendCommand(
      new MyDumpValuesCommand("info args", this, xCompositeNode, false, Icons.PARAMETER_ICON)
    );

    context.sendCommand(
      new MyDumpValuesCommand("info locals", this, xCompositeNode, true, Icons.VARIABLE_ICON)
    );
  }

  @Override
  public Object getEqualityObject() {
    return myScope; // TODO: myScope for not top frame is filled in async way
  }

  @Override
  public XDebuggerEvaluator getEvaluator() {
    return new XDebuggerEvaluator() {
      @Override
      public boolean evaluateCondition(@NotNull String s) {
        return false;
      }

      @Override
      public String evaluateMessage(@NotNull String s) {
        return null;
      }

      @Override
      public void evaluate(@NotNull final String evaluated, final XEvaluationCallback xEvaluationCallback) {
        context.sendCommand(new StackFrameBasedDebuggerCommand("print "+evaluated, CppStackFrame.this) {
          @Override
          protected void processToken(String token, CppDebuggerContext context) {
            final String marker = EQ_MARKER;
            final int markerPos = token.indexOf(marker);

            int valueStartIndex = markerPos != -1 ? markerPos + marker.length() : 0; // markerPos == -1 iff e.g. we failed to evaluate
            xEvaluationCallback.evaluated(
              new CppValueNode(
                evaluated,
                evaluated,
                token.substring(valueStartIndex),
                null,
                CppStackFrame.this
              )
            );
          }
        });
      }

      @Override
      public TextRange getExpressionRangeAtOffset(Project project, Document document, int i) {
        return null; // TODO:
      }
    };
  }

  public static CppStackFrame parseStackFrame(String token, CppThreadStackInfo info, final CppDebuggerContext context) {
    int frameIndex;
    try {
      frameIndex = Integer.parseInt(token.substring(1, token.indexOf(' ')));
    } catch (NumberFormatException ex) {
      return null;
    }

    // #0  main (argc=1, argv=0x3d2478 "�#=") at src/untitled.c:4
    //#0  0x9335a3ae in __semwait_signal ()
    //#0  0x7c87556d in KERNEL32!GetConsoleCharType ()   from C:\WINDOWS\system32\kernel32.dll
    final int atMarker = token.indexOf(CppBreakpointManager.AT_MARKER);
    String scope = token.substring(token.indexOf(' ')+2, atMarker != -1 ? atMarker:token.length());
    final int semicolonPos = token.lastIndexOf(':');
    int line = atMarker != -1 && semicolonPos != -1 ? Integer.parseInt(token.substring(semicolonPos + 1)):-1;

    VirtualFile file;
    if (atMarker != -1) {
      final String uri = token.substring(atMarker + CppBreakpointManager.AT_MARKER.length(), semicolonPos);
      file = ApplicationManager.getApplication().runReadAction(new Computable<VirtualFile>() {
        public VirtualFile compute() {
          Project project = context.getSession().getProject();
          VirtualFile file = VfsUtil.findRelativeFile(
            uri,
            ProjectRootManager.getInstance(project).getContentRoots()[0]
          );
          if (file == null) {
            PsiFile[] files = FilenameIndex.getFilesByName(project, uri, GlobalSearchScopes.projectProductionScope(project));
            if (files.length == 0) {
              files = FilenameIndex.getFilesByName(project, uri, GlobalSearchScope.allScope(project));
            }

            if (files.length > 0) file = files[0].getVirtualFile();
          }
          return file;
        }
      });
    } else {
      file = null;
    }
    final XSourcePosition position = file != null ? XDebuggerUtil.getInstance().createPosition(file, line - 1):null;

    CppStackFrame cppStackFrame = new CppStackFrame(scope, position, context, frameIndex);
    cppStackFrame.setThreadStackInfo(info);
    return cppStackFrame;
  }

  public void setThreadStackInfo(CppThreadStackInfo threadStackInfo) {
    this.threadStackInfo = threadStackInfo;
  }

  static class CppValueNode extends XValue {
    private final String name;
    private final String path;
    private final String value;
    private final Icon icon;
    private final CppStackFrame frame;

    CppValueNode(String _name, String _path, String _value, Icon _icon, CppStackFrame _frame) {
      name = _name;
      value = _value;
      frame = _frame;
      icon = _icon;
      path = _path;
    }

    public void computePresentation(@NotNull XValueNode node) {
      // (    const ProgressStep &) @0x7f0dd10: {<T> = {<gc> = {<No data fields>}, <No data fields>}, val = 1.11401715e-038}
      int typeStart = value.indexOf('(');
      int typeEnd = typeStart != -1 ? value.indexOf(')', typeStart):-1;
      String type = null;
      String value = this.value;

      if (typeEnd != -1) {
        type = value.substring(typeStart + 1, typeEnd);
        value = value.substring(typeEnd + 1);
      }

      boolean structValue = value.endsWith("}");
      if (structValue) {
        int index = value.indexOf('{');
        if (index != -1) value = value.substring(0, index);
      }
      boolean hasChildren = type != null ? type.indexOf('*') != -1 || type.indexOf('&') != -1 || type.indexOf("@0x") != -1: structValue;

      node.setPresentation(name, structValue || "this".equals(name)? Icons.CLASS_ICON:icon, type, value, hasChildren);
    }

    @Override
    public XValueModifier getModifier() {
      return new XValueModifier() {
        @Override
        public void setValue(@NotNull String newValue, final @NotNull XModificationCallback callback) {
          frame.context.sendCommand(new StackFrameBasedDebuggerCommand("set " +path + EQ_MARKER + newValue, frame) {
            @Override
            protected boolean processResponse(String s, CppDebuggerContext context) {
              final boolean b = super.processResponse(s, context);
              callback.valueModified();
              return b;
            }
          });
        }
      };
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode xCompositeNode) {
      if (value.endsWith("}")) {
        List<XValue> nodes = new ArrayList<XValue>();
        parse(null, path, value, nodes, frame);

        xCompositeNode.addChildren(nodes, true);
      } else {
        final String newPath = "(*"+path+")";
        frame.context.sendCommand(new MyDumpValuesCommand("print "+newPath, frame, xCompositeNode, true, null) {
          @Override
          protected void addNode(List<XValue> values, String name, String value) {
            parse(null, newPath, value,  values, frame);
          }
        });
      }
    }

    private static void parse(String curName, String path, String curValue, List<XValue> nodes, CppStackFrame frame) {
      if (curValue.endsWith("}")) {
        curValue = curValue.substring(curValue.indexOf('{') + 1, curValue.length() - 1);
        int pos = 0;

        while(true) {
          int i = curValue.indexOf(EQ_MARKER, pos);
          if (i == -1) return;
          String name = curValue.substring(CharArrayUtil.shiftBackwardUntil(curValue, i -1, " ")+1, i);
          int valueEnd = extractValue(curValue, i + EQ_MARKER.length());
          String value = curValue.substring(i + EQ_MARKER.length(), valueEnd);
          parse(name, path, value, nodes, frame);
          pos = valueEnd + 1;
        }
      } else {
        String curPath;
        if (curName != null) {
          curPath = path + "." + curName;
        } else {
          curPath = path;
          curName = path;
        }
        nodes.add(new CppValueNode(curName, curPath, curValue, null,frame));
      }
    }

    private static int extractValue(String curValue, int i) {
      if (curValue.charAt(i) == '{') {
        ++i;
        int braceCount = 1;
        while(braceCount != 0 && i < curValue.length()) {
          char ch = curValue.charAt(i);
          if (ch == '{') ++braceCount;
          else if (ch == '}') --braceCount;
          ++i;
        }

        if (braceCount == 0) return i;
        return curValue.length();
      }

      int commaPos = curValue.indexOf(',', i);
      return commaPos != -1 ? commaPos:curValue.length();
    }

    @Override
    public String getEvaluationExpression() {
      return name;
    }
  }

  private static class StackFrameBasedDebuggerCommand extends DebuggerCommand {
    protected final CppStackFrame myFrame;
    private int responseCount;

    StackFrameBasedDebuggerCommand(String command, CppStackFrame frame) {
      super(command);
      myFrame = frame;
    }

    @Override
    public String getCommandText() {
      final String s = super.getCommandText();
      final int frameIndex = myFrame.myFrameIndex;
      return "frame "+frameIndex + "\n" + s;
    }

    @Override
    protected boolean processResponse(String s, CppDebuggerContext context) {
      ++responseCount;
      if (responseCount == 1) return true; // frame command
      return super.processResponse(s, context);
    }
  }

  private static class MyDumpValuesCommand extends StackFrameBasedDebuggerCommand {
    private final List<XValue> values;
    private final XCompositeNode xCompositeNode;
    private final boolean last;
    private final Icon defaultIcon;

    public MyDumpValuesCommand(String val, CppStackFrame frame, XCompositeNode _xCompositeNode, boolean _last, Icon _defaultIcon) {
      super(val, frame);
      xCompositeNode = _xCompositeNode;
      values = new ArrayList<XValue>();
      last = _last;
      defaultIcon = _defaultIcon;
    }

    @Override
    protected void processToken(String token, CppDebuggerContext context) {
      final String marker = EQ_MARKER;
      final int markerPos = token.indexOf(marker);
      if (markerPos == -1) return;

      String name = token.substring(0, markerPos);
      String value = token.substring(markerPos + marker.length());
      addNode(values, name, value);
    }

    protected void addNode(List<XValue> values, String name, String value) {
      values.add(new CppValueNode(name, name, value, defaultIcon, myFrame));
    }

    @Override
    protected boolean processResponse(String s, CppDebuggerContext context) {
      final boolean b = super.processResponse(s, context);
      if (!b) xCompositeNode.addChildren(values, last);
      return b;
    }
  }
}
