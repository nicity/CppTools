// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.hilighting;

import com.advancedtools.cpp.CppHighlighter;
import com.advancedtools.cpp.communicator.Communicator;
import com.advancedtools.cpp.commands.NavigationCommand;
import com.advancedtools.cpp.usages.FileUsage;
import com.advancedtools.cpp.usages.OurUsage;
import com.advancedtools.cpp.utils.IconableGutterNavigator;
import com.advancedtools.cpp.utils.NavigationUtils;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.util.containers.HashMap;
import com.intellij.psi.PsiFile;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntProcedure;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.util.Map;

/**
 * @author maxim
 */
public class HighlightUtils {
  static final @NonNls String RANGES_PREFIX = "ranges:";
  static final @NonNls String INHPAR_PREFIX = "INHPAR:";
  static final int PREPROCESSOR_ARGUMENT_INDEX = 0;
  static final int CONSTANT_INDEX = 1;
  static final int OVERRIDES_INDEX = 2;
  static final int OVERRIDDEN_INDEX = 3;
  static final int LABEL_INDEX = 4;
  static final int FUNCTION_INDEX = 5;
  static final int TYPE_INDEX = 6;
  static final int MACROS_INDEX = 7;
  static final int UNUSED_INDEX = 8;
  static final int PP_SKIPPED_INDEX = 9;
  static final int NAMESPACE_INDEX = 10;
  static final int STATIC_FUNCTION_INDEX = 11;
  static final int STATIC_INDEX = 12;
  static final int FIELD_INDEX = 13;
  static final int PARAMETER_INDEX = 14;
  static final int METHOD_INDEX = 15;

  static final TextAttributes EMPTY_ATTRS = new TextAttributes();

  private static TextAttributesKey[] ourAttributes = new TextAttributesKey[20];
  @NonNls
  static final String OK_ANALIZE_COMMAND_RESPONSE = "<OK:analyze";

  private static Key<HighlightCommand> ourCurrentHighlightingCommandKey = Key.create("last.hilighting.command");
  public static final boolean debug = Communicator.isDebugEnabled;
  static final long start = System.currentTimeMillis();

  static {
    ourAttributes[PREPROCESSOR_ARGUMENT_INDEX] = CppHighlighter.CPP_PP_ARG;
    ourAttributes[PP_SKIPPED_INDEX] = CppHighlighter.CPP_PP_SKIPPED;
    ourAttributes[CONSTANT_INDEX] = CppHighlighter.CPP_CONSTANT;

    ourAttributes[OVERRIDES_INDEX] = null;
    ourAttributes[OVERRIDDEN_INDEX] = null;

    ourAttributes[LABEL_INDEX] = CppHighlighter.CPP_LABEL;

    ourAttributes[NAMESPACE_INDEX] = CppHighlighter.CPP_NAMESPACE;
    ourAttributes[FUNCTION_INDEX] = CppHighlighter.CPP_FUNCTION;
    ourAttributes[STATIC_FUNCTION_INDEX] = CppHighlighter.CPP_STATIC_FUNCTION;
    ourAttributes[STATIC_INDEX] = CppHighlighter.CPP_STATIC;
    ourAttributes[FIELD_INDEX] = CppHighlighter.CPP_FIELD;
    ourAttributes[METHOD_INDEX] = CppHighlighter.CPP_METHOD;
    ourAttributes[PARAMETER_INDEX] = CppHighlighter.CPP_PARAMETER;
    ourAttributes[TYPE_INDEX] = CppHighlighter.CPP_TYPE;

    ourAttributes[MACROS_INDEX] = CppHighlighter.CPP_MACROS;
    ourAttributes[UNUSED_INDEX] = CppHighlighter.CPP_UNUSED;
  }

  static final TextAttributesKey getTextAttributesKeyByOurIndex(int ourIndex) {
    return ourAttributes[ourIndex];
  }

  static int getOurIndexByServerId(int color) {
    int ourIndex = color == 6 ? CONSTANT_INDEX :
      color == 7 ? PREPROCESSOR_ARGUMENT_INDEX :
        color == 5 ? LABEL_INDEX :
          color == 4 ? NAMESPACE_INDEX :
            color == 2 ? FUNCTION_INDEX :
              color == 1 ? TYPE_INDEX :
                color == 3 ? MACROS_INDEX :
                  color == 0 ? UNUSED_INDEX :
                    color == 11 ? PP_SKIPPED_INDEX :
                    color == 12 ? STATIC_FUNCTION_INDEX :
                    color == 13 ? STATIC_INDEX :
                    color == 14 ? FIELD_INDEX :
                    color == 15 ? PARAMETER_INDEX :
                    color == 16 ? METHOD_INDEX :
                      -1;
    if (ourIndex >= ourAttributes.length) ourIndex = -1;
    return ourIndex;
  }

  static RangeHighlighter createRangeMarker(Editor editor, final int rangeOffset, final int rangeOffset2, final int index, TextAttributes attrs) {
    RangeHighlighter rangeHighlighter = editor.getMarkupModel().addRangeHighlighter(
      rangeOffset,
      rangeOffset2,
      index == PP_SKIPPED_INDEX ? HighlighterLayer.LAST:100,
      attrs,
      HighlighterTargetArea.EXACT_RANGE
    );

    if (index == OVERRIDES_INDEX) {
      rangeHighlighter.setGutterIconRenderer(
        new MyGutterIconRenderer(
          IconLoader.getIcon("/gutter/overridingMethod.png"),
          true
        )
      );
    } else if (index == OVERRIDDEN_INDEX) {
      rangeHighlighter.setGutterIconRenderer(
        new MyGutterIconRenderer(
          IconLoader.getIcon("/gutter/overridenMethod.png"),
          false
        )
      );
    }

    return rangeHighlighter;
  }

  public static void trace(PsiFile psiFile, Editor editor, String msg) {
    final Thread thread = Thread.currentThread();
    Communicator.debug(msg +psiFile.getName()+","+ thread + ":" + thread.getId() + ", editor:"+editor + ", time:"+ (System.currentTimeMillis() - start));
  }

  public static HighlightCommand getUpToDateHighlightCommand(PsiFile psiFile, Project project) {
    HighlightCommand command = psiFile.getUserData(ourCurrentHighlightingCommandKey);

    if (command == null || !command.isUpToDate() || command.isFailedOrCancelled()) {
      command = new HighlightCommand(psiFile);
      psiFile.putUserData(ourCurrentHighlightingCommandKey, command);
      command.nonblockingPost(project);
    }
    return command;
  }

  public static class MyGutterIconRenderer extends IconableGutterNavigator {
    private final TIntArrayList myData;
    private final boolean myOverrides;

    MyGutterIconRenderer(Icon icon, boolean overrides) {
      super(icon, overrides ? "overrides super method" : "overriden by descendants");
      myData = new TIntArrayList();
      myOverrides = overrides;
    }

    public void addData(int data) {
      // to prevent bug with long list to the same loc
      if (myData.size() < 5 && !myData.contains(data)) myData.add(data);
    }

    protected void doNavigate(final Project project) {
      class MyNavigationCommand extends NavigationCommand {
        int currentData;
        Map<String, FileUsage> fileNameToFileUsagesMap = new HashMap<String, FileUsage>();
        Map<FileUsage, TIntObjectHashMap<OurUsage>> fileUsageToUsagesMap = new HashMap<FileUsage, TIntObjectHashMap<OurUsage>>();

        public MyNavigationCommand(Project _project) {
          super(_project, "", -1);
        }

        protected FileUsage findFileUsage(String fileName) {
          return fileNameToFileUsagesMap.get(fileName);
        }

        protected void doAddFileUsage(FileUsage _fileUsage) {
          super.doAddFileUsage(_fileUsage);
          fileNameToFileUsagesMap.put(_fileUsage.fileName, _fileUsage);
        }

        protected OurUsage findUsage(FileUsage currentFileUsage, int start, int end) {
          final TIntObjectHashMap<OurUsage> usageMap = fileUsageToUsagesMap.get(currentFileUsage);
          if (usageMap != null) return usageMap.get(start);
          return null;
        }

        protected void doAddUsage(FileUsage fileUsage, OurUsage ourUsage) {
          super.doAddUsage(fileUsage, ourUsage);
          TIntObjectHashMap<OurUsage> usageMap = fileUsageToUsagesMap.get(currentFileUsage);
          if (usageMap == null) fileUsageToUsagesMap.put(fileUsage, usageMap = new TIntObjectHashMap<OurUsage>());
          usageMap.put(ourUsage.getStart(), ourUsage);
        }

        public String getCommand() {
          return (myOverrides ? "find-parents-by-no " : "find-inheritors-by-no ") + currentData;
        }
      }

      final MyNavigationCommand myNavigationCommand = new MyNavigationCommand(project);
      myData.forEach(new TIntProcedure() {
        public boolean execute(int i) {
          myNavigationCommand.currentData = i;
          myNavigationCommand.post(project);
          return myNavigationCommand.hasReadyResult();
        }
      });
      NavigationUtils.navigate(project, myNavigationCommand.getUsagesList());
    }

    public boolean isOverrides() {
      return myOverrides;
    }

    public void clearData() {
      myData.clear();
    }
  }
}
