// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.hilighting;

import com.advancedtools.cpp.commands.StringCommand;
import com.advancedtools.cpp.communicator.BlockingCommand;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.advancedtools.cpp.communicator.Communicator;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixAction;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import gnu.trove.THashSet;
import gnu.trove.TIntObjectHashMap;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.List;

/**
 * @author maxim
 */
public class HighlightCommand extends BlockingCommand {
  private final String fileName;
  private final long stamp;
  private boolean serverCommandWasInvalidated;

  private List<String> highlighterStringList = new ArrayList<String>(50);
  private List<String> overridenStringList = new ArrayList<String>(50);
  private List<String> errorsStringList = new ArrayList<String>(50);
  private List<String> inspectionsStringList = new ArrayList<String>(50);

  private static Key<THashSet<RangeHighlighter>> myHighlightersKey = Key.create("cpp.highlighters");
  private static Key<THashSet<RangeHighlighter>> myOverridenHighlightersKey = Key.create("cpp.overriden.highlighters");
  private static Key<List<String>> myErrorsKey = Key.create("cpp.errors");

  private Communicator communicator;
  private PsiFile file;
  private static final @NonNls String ANALYZE_COMMAND_NAME = "analyze";
  private static final @NonNls String FONTIFY_COMMAND_NAME = "fontify";
  private static final @NonNls String LIST_PARENTS_COMMAND_NAME = "list-parents";

  private volatile boolean parentsReady;
  private volatile boolean errorsReady;
  private volatile boolean markersReady;

  private BlockingCommand.PollingConditional errorsReadyPoller = new PollingConditional() {
    public boolean isReady() {
      return errorsReady;
    }
  };

  private BlockingCommand.PollingConditional markersReadyPoller = new PollingConditional() {
    public boolean isReady() {
      return markersReady;
    }
  };

  private BlockingCommand.PollingConditional parentsReadyPoller = new PollingConditional() {
    public boolean isReady() {
      return parentsReady;
    }
  };

  private static final TextAttributes emptyTextAttrs = new TextAttributes() {
    @Override
    public boolean isEmpty() {
      return false;
    }
  };
  private static final String STARTED_MARKER = "Started:";
  private final Computable<PsiFile> fileProvider = new Computable<PsiFile>() {
    public PsiFile compute() {
      return file;
    }
  };

  public HighlightCommand(PsiFile _file) {
    file = _file;
    fileName = BuildingCommandHelper.fixVirtualFileName(_file.getVirtualFile().getPath());
    communicator = Communicator.getInstance(_file.getProject());
    stamp = communicator.getModificationCount();

    doInfiniteBlockingWithCancelledCheck = true;
    started = System.currentTimeMillis();
  }

  public final long started;
  
  public void commandFinishedString(String str) {
    super.commandFinishedString(str);
    final long okCommandIdFromServer = Long.parseLong(
      str.substring(str.lastIndexOf(':') + 1,str.lastIndexOf('>'))
    );

    //if (str.indexOf(HighlightUtils.OK_ANALIZE_COMMAND_RESPONSE) == -1) {
    serverCommandWasInvalidated |= okCommandIdFromServer != stamp;
    //}

//    System.out.println("Done:"+str+" " + (System.currentTimeMillis() - started));
    if (str.indexOf(LIST_PARENTS_COMMAND_NAME) != -1) {
      parentsReady = true;
    }

    if (str.indexOf(ANALYZE_COMMAND_NAME) != -1) {
      errorsReady = true;
    }

    if (str.indexOf(FONTIFY_COMMAND_NAME) != -1) {
      markersReady = true;
    }
  }

  public void doExecute() {
//    System.out.println(System.currentTimeMillis() - started);
    
    super.doExecute();

    synchronized(this) {
      if (serverCommandWasInvalidated || failed) {
        requestRestartDaemon(file);

        return;
      }
    }

    file.putUserData(myErrorsKey, errorsStringList);
  }

  public void addRangeMarkers(Editor editor) {
    doAddHighlighters(editor, highlighterStringList, false);
  }

  private void doAddHighlighters(Editor editor, List<String> highlighterStringList, boolean overriden) {
    if (stamp != communicator.getModificationCount() || failed) return;

    final long started = System.currentTimeMillis();
    Key<THashSet<RangeHighlighter>> highlightersKey = overriden ? myOverridenHighlightersKey : myHighlightersKey;
    THashSet<RangeHighlighter> currentHighlighters = editor.getUserData(highlightersKey);
    final THashSet<RangeHighlighter> invalidMarkers;    

    if (currentHighlighters == null) {
      invalidMarkers = new THashSet<RangeHighlighter>(10000);
    } else {
      invalidMarkers = currentHighlighters;
    }

    currentHighlighters = new THashSet<RangeHighlighter>();
    editor.putUserData(highlightersKey, currentHighlighters);

    final TIntObjectHashMap<RangeHighlighter> lastOffsetToHighlightersMap = new TIntObjectHashMap<RangeHighlighter>();
    final TIntObjectHashMap<RangeHighlighter> overridenMap = new TIntObjectHashMap<RangeHighlighter>();
    final TIntObjectHashMap<RangeHighlighter> overriddingMap = new TIntObjectHashMap<RangeHighlighter>();

    for(RangeHighlighter h:invalidMarkers) {
      if (!h.isValid()) continue;

      final GutterIconRenderer gutterIconRenderer = h.getGutterIconRenderer();
      final int offset = h.getStartOffset();

      if ((!overriden && gutterIconRenderer == null)) {
        lastOffsetToHighlightersMap.put(offset, h);
      } else if (overriden && gutterIconRenderer != null) {
        final HighlightUtils.MyGutterIconRenderer myGutterIconRenderer = (HighlightUtils.MyGutterIconRenderer) gutterIconRenderer;
        final boolean overrides = myGutterIconRenderer.isOverrides();
        
        ((HighlightUtils.MyGutterIconRenderer) gutterIconRenderer).clearData();
        ((overrides)?overriddingMap:overridenMap).put(offset, h);
      }
    }

    final EditorColorsScheme colorsScheme = editor.getColorsScheme();

    for(String s: highlighterStringList) {
      processHighlighterString(editor, colorsScheme, s, overriddingMap, overridenMap, lastOffsetToHighlightersMap,
        currentHighlighters, invalidMarkers);
    }

    //System.out.println("Updated highlighters 2/5 for "+ (System.currentTimeMillis() - started));
    for(RangeHighlighter rangeHighlighter:invalidMarkers) {
      editor.getMarkupModel().removeHighlighter(rangeHighlighter);
      currentHighlighters.remove(rangeHighlighter);
    }

    long doneFor = System.currentTimeMillis() - started;
    //System.out.println("Updated highlighters 2 for "+ doneFor);
    Communicator.debug("Updated highlighters 2 for "+ doneFor);
  }

  private static void requestRestartDaemon(final PsiFile file) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        Project project = file.getProject();
        if (project == null || project.isDisposed()) return;
        DaemonCodeAnalyzer.getInstance(project).restart();
      }
    }, ModalityState.NON_MODAL);
  }

  public void doExecuteOnCancel() {
    super.doExecuteOnCancel();
    requestRestartDaemon(file);
    if (errorsReady) {
      file.putUserData(myErrorsKey, errorsStringList);
    }
  }

  public void addErrors(final HighlightInfoHolder highlightHolder) {
    final AnalyzeProcessor processor = new AnalyzeProcessor() {
      public String getAnalizedFileName() {
        return fileName;
      }

      public void startedAnalyzedFileName(String fileName) {}

      public void addMessage(MessageType type, int start, int end, String message, Fix ... fixes) {
        if (type == MessageType.Intention) return;
        final TextRange range = new TextRange(start, end);
        final HighlightInfoType highlightInfoType = type == MessageType.Info ? HighlightInfoType.INFORMATION :
          type == MessageType.Warning ? HighlightInfoType.WARNING : HighlightInfoType.ERROR;

        HighlightInfo highlightInfo = HighlightInfo.newHighlightInfo(
          highlightInfoType).range(range).description(message).create();

        if (highlightInfo == null) {
          highlightInfo = HighlightInfo.newHighlightInfo(highlightInfoType).range(range).
            description(message).textAttributes(emptyTextAttrs).create();

        }

        highlightHolder.add(highlightInfo);
        if (fixes != null) {
          for(Fix fix:fixes) {
            QuickFixAction.registerQuickFixAction(highlightInfo, fix);
          }
        }
      }
    };
    for(String s: errorsStringList) {
      processErrorInfoFromString(s, processor);
    }
  }

  private void processHighlighterString(Editor editor,
                                        EditorColorsScheme colorsScheme,
                                        String str,
                                       TIntObjectHashMap<RangeHighlighter> overridesMarkerMap,
                                       TIntObjectHashMap<RangeHighlighter> overridenMarkerMap,
                                       TIntObjectHashMap<RangeHighlighter> lastOffsetToMarkersMap,
                                       THashSet<RangeHighlighter> highlightersSet,
                                       THashSet<RangeHighlighter> invalidMarkersSet
                                       ) {
    if (str.startsWith(HighlightUtils.RANGES_PREFIX)) {
      final int textLength = editor.getDocument().getTextLength();
      int previousOffset = -1;
      int currentOffset = HighlightUtils.RANGES_PREFIX.length();

      while (currentOffset < str.length()) {
        int offset = str.indexOf(':', currentOffset);
        if (offset == -1) break;
        int color = Integer.parseInt(str.substring(currentOffset, offset));

        int hLen = 0;
        for (currentOffset = offset + 1; currentOffset < str.length(); ++currentOffset) {
          final char ch = str.charAt(currentOffset);

          if (Character.isDigit(ch)) {
            hLen = hLen * 10 + Character.digit(ch, 10);
          } else {
            break;
          }
        }

        int hFrom;

        if (currentOffset < str.length() && str.charAt(currentOffset) == ',') {
          ++currentOffset;
          offset = str.indexOf(Communicator.DELIMITER, currentOffset);
          if (offset == -1) {
            offset = str.length();
          }
          hFrom = Integer.parseInt(str.substring(currentOffset, offset));
          currentOffset = offset + 1;
        } else {
          hFrom = 0;
          ++currentOffset;
        }

        if (previousOffset != -1) hFrom += previousOffset;

        int hTo = hLen + hFrom;
        previousOffset = hTo;

        if (hFrom >= textLength ||
            hTo >= textLength ||
            hFrom < 0 ||
            hTo < 0
        ) {
          continue;
        }

        final int colorIndex = HighlightUtils.getOurIndexByServerId(color);

        if (colorIndex < 0) continue;

        TextAttributesKey key = HighlightUtils.getTextAttributesKeyByOurIndex(colorIndex);
        TextAttributes attrs = key != null ? colorsScheme.getAttributes(key): HighlightUtils.EMPTY_ATTRS;

        processHighlighter(hFrom, hTo, colorIndex, attrs, editor, lastOffsetToMarkersMap, highlightersSet, invalidMarkersSet);
      }
    } else if (str.startsWith(HighlightUtils.INHPAR_PREFIX)) {
      int offset = str.indexOf(Communicator.DELIMITER);
      if (offset == -1) return;

      int offset2 = str.indexOf(Communicator.DELIMITER, offset + 1);
      if (offset2 == -1) return;

      int offset3 = str.indexOf(Communicator.DELIMITER, offset2 + 1);
      if (offset3 == -1) return;

      int offset4 = str.indexOf(Communicator.DELIMITER, offset3 + 1);
      if (offset4 == -1) return;

      int hFrom = Integer.parseInt(str.substring(HighlightUtils.INHPAR_PREFIX.length(), offset));
      int hTo = Integer.parseInt(str.substring(offset + 1, offset2));
      boolean overriden = str.charAt(offset3 + 1) == 't';
      boolean overrides = str.charAt(offset2 + 1) == 't';
      int symNo = Integer.parseInt(str.substring(offset4 + 1));

      if (overrides) {
        addOverrideOrOverridenMarker(editor, overridesMarkerMap, hFrom, hTo, HighlightUtils.OVERRIDES_INDEX, symNo,
          highlightersSet, invalidMarkersSet);
      }

      if (overriden) {
        addOverrideOrOverridenMarker(editor, overridenMarkerMap, hFrom, hTo, HighlightUtils.OVERRIDDEN_INDEX, symNo,
          highlightersSet, invalidMarkersSet);
      }
    }
  }

  private static RangeHighlighter processHighlighter(int hFrom, int hTo, int colorIndex, TextAttributes attrs,
                                                     Editor editor, TIntObjectHashMap<RangeHighlighter> lastOffsetToMarkersMap,
                                                     THashSet<RangeHighlighter> highlightersSet,
                                                     THashSet<RangeHighlighter> invalidMarkersSet
                                                     ) {
    RangeHighlighter rangeHighlighter = lastOffsetToMarkersMap.get(hFrom);

    if (rangeHighlighter == null ||
        rangeHighlighter.getEndOffset() != hTo ||
        rangeHighlighter.getTextAttributes() != attrs
        ) {
      highlightersSet.add(
        rangeHighlighter = HighlightUtils.createRangeMarker(
          editor,
          hFrom,
          hTo,
          colorIndex,
          attrs
        )
      );

      lastOffsetToMarkersMap.put(hFrom, rangeHighlighter);
    } else {
      highlightersSet.add(rangeHighlighter);
      invalidMarkersSet.remove(rangeHighlighter);
    }

    return rangeHighlighter;
  }

  private RangeHighlighter addOverrideOrOverridenMarker(Editor editor,
                                            TIntObjectHashMap<RangeHighlighter> rangeMarkerMap, int hFrom, int hTo,
                                            int symbolId, int symNo, THashSet<RangeHighlighter> myHighlighters,
                                            THashSet<RangeHighlighter> invalidMarkersSet
                                            ) {
//    final int len = hTo - hFrom;
    final Document document = editor.getDocument();
    hFrom = document.getLineStartOffset(document.getLineNumber(hFrom));
    final RangeHighlighter rangeHighlighter = processHighlighter(hFrom, hFrom, symbolId, HighlightUtils.EMPTY_ATTRS,
      editor, rangeMarkerMap, myHighlighters, invalidMarkersSet);

    ((HighlightUtils.MyGutterIconRenderer)rangeHighlighter.getGutterIconRenderer()).addData(symNo);
    return rangeHighlighter;
  }

  public void commandOutputString(String str) {
//    System.out.println(System.currentTimeMillis() - started);
    if (stamp != communicator.getModificationCount() || failed) return;

    if (str.startsWith(STARTED_MARKER)) {
      return;
    }

    if (str.startsWith(HighlightUtils.RANGES_PREFIX)) {
      highlighterStringList.add(str);
      return;
    }

    if (str.startsWith(HighlightUtils.INHPAR_PREFIX)) {
      overridenStringList.add(str);
      return;
    }

    final List<String> cachedErrorsList = getErrorListIfCached(str, fileProvider);

    if (cachedErrorsList != null) errorsStringList = cachedErrorsList;
    else {
      if (parentsReady) {
        inspectionsStringList.add(str);
      } else if (!errorsReady) {
        errorsStringList.add(str);
      } else {
        System.out.println("Unexpected:"+str);
      }
    }
  }

  public static List<String> getErrorListIfCached(String str, Computable<PsiFile> fileComputable) {
    List<String> cachedErrorsList = null;

    if (str.startsWith("NOT-CHANGED:")) {
      final List<String> strings = fileComputable.compute().getUserData(myErrorsKey);
      if (strings != null) {
        cachedErrorsList = strings;
      }
    }
    return cachedErrorsList;
  }

  public void addOverridenRangeMarkers(Editor editor) {
    doAddHighlighters(editor, overridenStringList, true);
  }

  public ProblemDescriptor[] addInspectionErrors(final InspectionManager inspectionManager) {
    final List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();

    final AnalyzeProcessor processor = new AnalyzeProcessor() {
      public String getAnalizedFileName() {
        return fileName;
      }

      public void startedAnalyzedFileName(String fileName) {}

      public void addMessage(MessageType type, int start, int end, String message, Fix ... fixes) {
        if (type == MessageType.Intention) return;
        final PsiElement psiElement = file.findElementAt(start);
        if (psiElement == null) return;
        ProblemHighlightType type1 = type == MessageType.Error ? ProblemHighlightType.ERROR :
          type == MessageType.Warning ? ProblemHighlightType.GENERIC_ERROR_OR_WARNING : ProblemHighlightType.LIKE_UNUSED_SYMBOL;
        problems.add(
          inspectionManager.createProblemDescriptor(psiElement, new TextRange(0, end - start), message, type1, (LocalQuickFix[]) fixes));
      }
    };
    for(String s: inspectionsStringList) {
      processErrorInfoFromString(s, processor);
    }
    return problems.toArray(new ProblemDescriptor[problems.size()]);
  }

  public static void processErrorInfoFromString(final String str, AnalyzeProcessor processor) {
    if (str.startsWith(STARTED_MARKER)) {
      processor.startedAnalyzedFileName(str.substring(STARTED_MARKER.length()));
      return;
    }

    int scanOffset = 0;
    if (str.startsWith("AT:|")) {
      int offset = str.indexOf(Communicator.DELIMITER, 4) + 1;

      String fileName = str.substring(4, scanOffset - 1);
      if (!fileName.equals(processor.getAnalizedFileName())) return;
      scanOffset = offset;
    }

    int offset2 = str.indexOf(Communicator.DELIMITER, scanOffset) + 1;
    int offset3 = str.indexOf(Communicator.DELIMITER, offset2) + 1;
    if (offset3 == 0) return;
    int offset4 = Communicator.findDelimiter(str,offset3);
    int offset5 = offset4;

    String message = str.substring(offset3, offset4 != str.length() ? offset4 - 1:str.length());
    int messageId = getIntFromStringWithDefault(message);
    offset5 = advanceOffsetTillParametersEnd(str, offset5, messageId);

    final int rangeOffset = Integer.parseInt(str.substring(scanOffset, offset2 - 1));
    final int rangeOffset2 = Integer.parseInt(str.substring(offset2, offset3 - 1));
    String type = offset5 == offset4 ? "":str.substring(offset4, offset5 != str.length()? offset5-1:str.length());
    message = getMessageText(messageId, message, type);

    List<Fix> fixes = null;

    List<String> additionalMessages = null;
    List<String> additionalMessageTypes = null;

    while(offset5 != str.length()) {
      int delimiter = Communicator.findDelimiter(str,offset5);  
      int end = delimiter == str.length() ? str.length():delimiter - 1;
      String fix = str.substring(offset5, end);

      final String prefix = "fix:";
      if (fix.startsWith(prefix)) {
        fix = fix.substring(prefix.length());
        if (fixes == null) fixes = new ArrayList<Fix>();
        final int fixNumber = Integer.parseInt(fix);
        String msg = getFixMessageById(fixNumber);
        @NonNls String fixId = getFixId(fixNumber);

        assert fixId != null:"Null fix for " + fixNumber;
        fixes.add(new Fix(msg, fixId, rangeOffset));
      } else {
        if (additionalMessages == null) {
          additionalMessages = new ArrayList<String>();
          additionalMessageTypes = new ArrayList<String>();
        }

        additionalMessages.add(fix);
        int newDelimiter = advanceOffsetTillParametersEnd(str, delimiter, getIntFromStringWithDefault(fix));
        boolean newDelimiterIsValid = true;
        if (newDelimiter == str.length()) {
          newDelimiter = str.length() + 1;
          newDelimiterIsValid = false;
        }
        additionalMessageTypes.add(delimiter < newDelimiter ? str.substring(delimiter, newDelimiter - 1):"");
        delimiter = newDelimiterIsValid ? newDelimiter:str.length();
      }

      if (delimiter == 0) break;
      offset5 = delimiter;
    }

    processor.addMessage(
      getMessageType(messageId, type),
      rangeOffset,
      rangeOffset2,
      message,
      fixes != null ? fixes.toArray(new Fix[fixes.size()]):null
    );

    if (additionalMessages != null) {
      for(int i = 0; i < additionalMessages.size(); ++i) {
        final int additionalMessageId = getIntFromStringWithDefault(additionalMessages.get(i));

        processor.addMessage(
          getMessageType(additionalMessageId, additionalMessageTypes.get(i)),
          rangeOffset,
          rangeOffset2,
          getMessageText(additionalMessageId, additionalMessages.get(i), additionalMessageTypes.get(i))
        );
      }
    }
  }

  private static int advanceOffsetTillParametersEnd(String str, int offset, int messageId) {
    MessageInfo messageInfo = myIndexToErrorInfo.get(messageId);
    if (messageInfo != null) {
      for(int i = 0; i < messageInfo.parameterCount; ++i) {
        offset = offset != str.length() ? Communicator.findDelimiter(str,offset):str.length();
      }
    }
    return offset;
  }

  private static int getIntFromStringWithDefault(String message) {
    try {
      return Integer.parseInt(message);
    } catch (NumberFormatException ex) {
      return -1;
    }
  }

  private static String getMessageText(int messageId, String message, String params) {
    MessageInfo messageInfo = myIndexToErrorInfo.get(messageId);
    String completeMessage = messageInfo != null ? messageInfo.message:null;

    if (completeMessage != null) {
      message = completeMessage;
      int offset = 0;
      int previousOffset = 0;
      for(int i = 0; i < messageInfo.parameterCount; ++i) {
        previousOffset = offset;
        offset = offset != params.length() ? Communicator.findDelimiter(params,offset):params.length();
        message = StringUtil.replace(
          message,
          "%"+i,
          previousOffset != params.length() ?
            BuildingCommandHelper.unquote(params.substring(previousOffset, offset)):""
        );
      }
    }
    return message;
  }

  private static TIntObjectHashMap<String> myIndexToFixMessage = new TIntObjectHashMap<String>();
  private static TIntObjectHashMap<String> myIndexToFixId = new TIntObjectHashMap<String>();

  public static class MessageInfo {
    final String message;
    final AnalyzeProcessor.MessageType messageType;
    final int parameterCount;

    public MessageInfo(String message, AnalyzeProcessor.MessageType messageType, int parameterCount) {
      if (parameterCount > 5) parameterCount = 5; // guard
      this.message = message;
      this.messageType = messageType;
      this.parameterCount = parameterCount;
    }
  }

  private static TIntObjectHashMap<MessageInfo> myIndexToErrorInfo = new TIntObjectHashMap<MessageInfo>();

  private static void addOneErrorInfo(String str, TIntObjectHashMap<MessageInfo> indexToErrorInfo) {
    int firstDelimPos = str.indexOf(Communicator.DELIMITER);
    int secondDelimPos = str.indexOf(Communicator.DELIMITER, firstDelimPos + 1);
    int lastDelimPos = str.lastIndexOf(Communicator.DELIMITER);
    int messageId = Integer.parseInt(str.substring(0, firstDelimPos));
    String type = str.substring(firstDelimPos + 1, secondDelimPos);

    AnalyzeProcessor.MessageType messageType = type.indexOf("WARNING") != -1 ?
      AnalyzeProcessor.MessageType.Warning :
      type.equals("INFO") ? AnalyzeProcessor.MessageType.Info :
      type.equals("INTENTION") ? AnalyzeProcessor.MessageType.Intention :
      AnalyzeProcessor.MessageType.Error;

    indexToErrorInfo.put(
      messageId,
      new MessageInfo(
        str.substring(secondDelimPos + 1, lastDelimPos),
        messageType,
        getIntFromStringWithDefault(str.substring(lastDelimPos + 1))
      )
    );
  }

  public static void initErrorsDataInTest(String... data) {
    for(String s:data) addOneErrorInfo(s, myIndexToErrorInfo);
  }
  
  public static void initQuickFixData(Project project) {
    // TODO fix this good enough solution -for project-
    new StringCommand("available-quick-fixes") {
      private final TIntObjectHashMap<String> indexToMessage = new TIntObjectHashMap<String>();
      private final TIntObjectHashMap<String> indexToFixId = new TIntObjectHashMap<String>();

      @Override
      public void commandOutputString(String str) {
        final int spaceIndex = str.indexOf('=');
        final int spaceIndex2 = str.indexOf(Communicator.DELIMITER, spaceIndex + 1);
        int number = Integer.parseInt(str.substring(spaceIndex + 1, spaceIndex2)) - 1;
        String fixId = str.substring(0, spaceIndex);
        String message = str.substring(spaceIndex2 + 1);

        indexToFixId.put(number, fixId);
        indexToMessage.put(number, message);
      }

      public void doExecute() {
        myIndexToFixMessage = indexToMessage;
        myIndexToFixId = indexToFixId;
      }
    }.post(project);

    new StringCommand("list-errors") {
      private final TIntObjectHashMap<MessageInfo> indexToErrorInfo = new TIntObjectHashMap<MessageInfo>();

      @Override
      public void commandOutputString(String str) {
        addOneErrorInfo(str, indexToErrorInfo);
      }

      public void doExecute() {
        myIndexToErrorInfo = indexToErrorInfo;
      }
    }.post(project);
  }

  private static String getFixMessageById(int fixNumber) {
    return myIndexToFixMessage.get(fixNumber);
  }

  private static String getFixId(int fixNumber) {
    return myIndexToFixId.get(fixNumber);
  }

  private static AnalyzeProcessor.MessageType getMessageType(int messageId, String typeS) {
    MessageInfo messageInfo = myIndexToErrorInfo.get(messageId);
    AnalyzeProcessor.MessageType type = messageInfo != null ? messageInfo.messageType : null;

    if (type == null) {
      return typeS.equals("W") ?
        AnalyzeProcessor.MessageType.Warning :
          typeS.equals("E") ? AnalyzeProcessor.MessageType.Error:
            AnalyzeProcessor.MessageType.Info;
    }
    return type;
  }

  public String getCommand() {
//    System.out.println(System.currentTimeMillis() - started);
    final String quotedFileName = BuildingCommandHelper.quote(fileName);
    return ANALYZE_COMMAND_NAME + " -n " + stamp + " "+ quotedFileName + " 0 end\n" +
           FONTIFY_COMMAND_NAME + " -n " + stamp + " "+quotedFileName + " 0 end \"buf\" 0\n" +
           LIST_PARENTS_COMMAND_NAME + " -n " + stamp + " "+quotedFileName + " 0 end\n" +
           "inspect -n " + stamp + " "+quotedFileName + " " + quotedFileName + " 0 end";
  }

  public boolean isUpToDate() {
    return stamp == communicator.getModificationCount();
  }

  public void awaitErrors(Project project) {
    await(project, errorsReadyPoller);
  }

  public void awaitHighlighting(Project project) {
    await(project, markersReadyPoller);
  }

  public void awaitOverriden(Project project) {
    await(project, parentsReadyPoller);
  }

  public void awaitInspections(Project project) {
    await(project, defaultPollingConditional);
  }
}
