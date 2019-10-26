// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.advancedtools.cpp.communicator.BlockingCommand;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.advancedtools.cpp.communicator.Communicator;
import com.advancedtools.cpp.psi.CppElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.parameterInfo.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ArrayUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author maxim
 */
public class CppParameterInfo implements ParameterInfoHandler<CppElement, String> {
  protected static Object[] getSignatures(Project project, String lookupString, int offset, PsiFile psiFile, Editor editor) {
    final MyCommand stringCommand = new MyCommand(
      psiFile,
      editor,
      lookupString,
      new ParameterInfoData(new TextRange(offset, offset))
    );
    stringCommand.post(project);

    return stringCommand.data.getSignatures();
  }

  static class ParameterInfoData {
    final TextRange range;
    final List<String> signatures = new ArrayList<String>();

    ParameterInfoData(TextRange _range) {
      range = _range;
    }

    public Object[] getSignatures() {
      return signatures.toArray(new Object[signatures.size()]);
    }
  }

  public boolean couldShowInLookup() {
    return true;
  }

  private static final Map<Project, ParameterInfoData> shownParameterInfos = new THashMap<Project, ParameterInfoData>();

  protected CppElement findRealElement(PsiFile file, int offset) {
    final CppElement startElement = findParentOfType(file, offset);
    CppElement el = startElement;
    while (el != null && el.getNode().getElementType() != CppTokenTypes.PARENS) {
      final PsiElement element = el.getParent();
      if (!(element instanceof CppElement)) return startElement;
      el = (CppElement) element;
    }
    return el;
  }

  @NotNull
  public String getParameterCloseChars() {
    return ",){";
  }

  public boolean tracksParameterIndex() {
    return true;
  }

  protected interface UpdateParameterInfoHandler<T> {
    void setCurrentParameter(T context, int currentParameterIndex);

    int getOffset(T context);

    PsiFile getFile(T context);

    void removeHint(T context);
  }

  protected <T> void updateParameterInfo(final CppElement o, T context, UpdateParameterInfoHandler<T> contextInfoHandler) {
    final TextRange textRange = o.getTextRange();

    synchronized (shownParameterInfos) {
      final Project project = contextInfoHandler.getFile(context).getProject();
      final ParameterInfoData parameterInfoData = shownParameterInfos.get(project);
      final TextRange textRange1 = parameterInfoData != null ? parameterInfoData.range : null;

      if (textRange1 != null && textRange.getStartOffset() != textRange1.getStartOffset()) {
        shownParameterInfos.remove(project);
        contextInfoHandler.removeHint(context);
        return;
      }
    }

    final int currentParameterIndex = getCurrentParameterIndex(o.getNode(), contextInfoHandler.getOffset(context));
    contextInfoHandler.setCurrentParameter(context, currentParameterIndex);
  }

  protected interface ParameterInfoHandler<T> {
    Project getProject(T t);

    Editor getEditor(T context);

    void setItemsToShow(T context, Object[] signatures);

    PsiFile getFile(T context);

    int getOffset(T context);
  }

  public <T> CppElement findElementForParameterInfo(final T context, ParameterInfoHandler<T> contextInfoHandler) {
    PsiFile psiFile = contextInfoHandler.getFile(context);
    int offset = contextInfoHandler.getOffset(context);
    final CppElement element = findRealElement(psiFile, offset);
    if (element == null) return null;
    final ParameterInfoData parameterInfoData = new ParameterInfoData(element.getTextRange());

    Project project = contextInfoHandler.getProject(context);
    contextInfoHandler.setItemsToShow(context, getSignatures(project, "", offset, psiFile, contextInfoHandler.getEditor(context)));

    synchronized (shownParameterInfos) {
      shownParameterInfos.put(project, parameterInfoData);
    }
    return element;
  }

  protected interface UpdateUIContext<T> {
    void setupUIComponentPresentation(T context, String p, int start, int end, boolean b, boolean b1, boolean b2, Color defaultParameterColor);

    int getCurrentParameterIndex(T context);

    Color getDefaultParameterColor(T context);
  }

  public <T> void updateUI(final String p, final T context, UpdateUIContext<T> handler) {
    final int index = handler.getCurrentParameterIndex(context);
    int start = 0;
    for (int i = 0; i < index; ++i) start = p.indexOf(',', start) + 1;
    int end = p.indexOf(',', start + 1);
    if (end == -1) end = p.length();

    handler.setupUIComponentPresentation(context, p, start, end, false, false, false, handler.getDefaultParameterColor(context));
  }

  static class MyCommand extends BlockingCommand {
    private final String filePath;
    private final int offset;
    private final String name;
    private final ParameterInfoData data;

    public MyCommand(PsiFile file, Editor editor, String _name, ParameterInfoData _data) {
      filePath = file.getVirtualFile().getPath();
      offset = editor.getCaretModel().getOffset();
      name = _name;
      data = _data;
    }

    public void commandOutputString(final String str) {
      int i = str.lastIndexOf(Communicator.DELIMITER);
      int i2 = i != -1 ? str.lastIndexOf(Communicator.DELIMITER, i - 1) : -1;
      boolean isVarArg = i2 != -1 ? str.substring(i2 + 1, i).length() > 0 : false;
      @NonNls String signature = i2 != -1 ? str.substring(0, i2) : str;

      if (signature.length() == 0) signature = isVarArg ? "..." : "<no-parameters>";
      else if (isVarArg) signature += ", ...";

      data.signatures.add(signature);
    }

    @Override
    public boolean acceptsEmptyResult() {
      return true;
    }

    public String getCommand() {
      return "parameter-info " + BuildingCommandHelper.quote(filePath) + " " + offset + " \"" + name + "\"";
    }
  }

  private final ParameterInfoHandler<CreateParameterInfoContext> createInfoHandler = new ParameterInfoHandler<CreateParameterInfoContext>() {
    public Project getProject(CreateParameterInfoContext context) {
      return context.getProject();
    }

    public Editor getEditor(CreateParameterInfoContext context) {
      return context.getEditor();
    }

    public void setItemsToShow(CreateParameterInfoContext context, Object[] signatures) {
      context.setItemsToShow(signatures);
    }

    public PsiFile getFile(CreateParameterInfoContext context) {
      return context.getFile();
    }

    public int getOffset(CreateParameterInfoContext context) {
      return context.getOffset();
    }
  };

  private final UpdateUIContext<ParameterInfoUIContext> updateUIHandler = new UpdateUIContext<ParameterInfoUIContext>() {
    public void setupUIComponentPresentation(ParameterInfoUIContext context, String p, int start, int end, boolean b, boolean b1, boolean b2, Color defaultParameterColor) {
      context.setupUIComponentPresentation(p, start, end, b, b1, b2, defaultParameterColor);
    }

    public int getCurrentParameterIndex(ParameterInfoUIContext context) {
      return context.getCurrentParameterIndex();
    }

    public Color getDefaultParameterColor(ParameterInfoUIContext context) {
      return context.getDefaultParameterColor();
    }
  };

  private final UpdateParameterInfoHandler<UpdateParameterInfoContext> updateInfoHandler = new UpdateParameterInfoHandler<UpdateParameterInfoContext>() {
    public void setCurrentParameter(UpdateParameterInfoContext context, int currentParameterIndex) {
      context.setCurrentParameter(currentParameterIndex);
    }

    public int getOffset(UpdateParameterInfoContext context) {
      return context.getOffset();
    }

    public PsiFile getFile(UpdateParameterInfoContext context) {
      return context.getFile();
    }

    public void removeHint(UpdateParameterInfoContext context) {
      context.removeHint();
    }
  };

  public CppElement findParentOfType(PsiFile file, int offset) {
    return ParameterInfoUtils.findParentOfType(file, offset, CppElement.class);
  }

  public int getCurrentParameterIndex(ASTNode node, int offset) {
    return ParameterInfoUtils.getCurrentParameterIndex(node, offset, CppTokenTypes.COMMA);
  }

  public Object[] getParametersForLookup(LookupElement lookupElement, ParameterInfoContext context) {
    return getSignatures(context.getProject(), lookupElement.getLookupString(), context.getOffset(), context.getFile(), context.getEditor());
  }

  public Object[] getParametersForDocumentation(String s, ParameterInfoContext parameterInfoContext) {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  public CppElement findElementForParameterInfo(CreateParameterInfoContext context) {
    return findElementForParameterInfo(context, createInfoHandler);
  }

  public void showParameterInfo(@NotNull CppElement element, CreateParameterInfoContext context) {
    context.showHint(element, element.getTextOffset() + 1, this);
  }

  public CppElement findElementForUpdatingParameterInfo(UpdateParameterInfoContext context) {
    return findRealElement(context.getFile(), context.getOffset());
  }

  public void updateParameterInfo(@NotNull CppElement o, UpdateParameterInfoContext context) {
    updateParameterInfo(o, context, updateInfoHandler);
  }

  public void updateUI(String s, ParameterInfoUIContext context) {
    updateUI(s, context, updateUIHandler);
  }
}
