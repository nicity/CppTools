// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.facade;

import com.advancedtools.cpp.commands.ImplementSomethingCommand;
import com.advancedtools.cpp.communicator.Communicator;
import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.generation.MemberChooserObject;
import com.intellij.codeInsight.generation.MemberChooserObjectBase;
import com.intellij.ide.util.MemberChooser;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.Icons;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author maxim
 * Date: Dec 24, 2008
 * Time: 9:06:02 PM
 */
public class CppOverrideImplementMethodHandler implements LanguageCodeInsightActionHandler {
  public boolean isValidFor(Editor editor, PsiFile file) {
    return true;
  }

  static class CppClassElementNode extends CppNamedElementNode {
    public CppClassElementNode(String name, String parentName) {
      super(name, null, parentName, Icons.CLASS_ICON);
    }
  }

  static class CppMethodElementNode extends CppNamedElementNode {
    private final String myParamNames;
    private final String myName;
    private final boolean myIsAbstract;

    public CppMethodElementNode(String nameWithSignature, String type, String parentName, String paramNames, String name, boolean isAbstract) {
      super(nameWithSignature, type, parentName, isAbstract ? Icons.ABSTRACT_METHOD_ICON:Icons.METHOD_ICON);
      myIsAbstract = isAbstract;
      myParamNames = paramNames;
      myName = name;
    }
  }

  static class CppNamedElementNode extends MemberChooserObjectBase implements ClassMember {
    private final String myParentName;
    private final String myType;

    public CppNamedElementNode(String name, String type, String parentName, Icon icon) {
      super(name, icon);
      myParentName = parentName;
      myType = type;
    }

    public MemberChooserObject getParentNodeDelegate() {
      return myParentName != null ? new CppClassElementNode(myParentName, null) : null;
    }

    public String getParentName() {
      return myParentName;
    }

    public String getType() {
      return myType;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      CppNamedElementNode that = (CppNamedElementNode) o;

      if (myParentName != null ? !myParentName.equals(that.myParentName) : that.myParentName != null) return false;
      if (myType != null ? !myType.equals(that.myParentName) : that.myType != null) return false;
      String text = getText();
      if ( text != null ? !text.equals(that.getText()) : that.getText() != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      String text = getText();
      return (myParentName != null ? myParentName.hashCode() : 0) +
        (text != null ? text.hashCode() : 0) +
        (myType != null ? myType.hashCode() : 0);
    }
  }

  public void invoke(final Project project, final Editor editor, final PsiFile file) {
    String commandName = CommandProcessor.getInstance().getCurrentCommandName();
    final boolean overrideMode = commandName != null && commandName.indexOf("Override Methods") != -1;
    ImplementSomethingCommand command = new ImplementSomethingCommand(file.getVirtualFile().getPath(), editor) {
      final List<CppMethodElementNode> elements = new ArrayList<CppMethodElementNode>();
      String className;

      @Override
      public void doExecute() {
        proceedWithChoosingMethods(project, file, editor, elements, overrideMode);
      }

      @Override
      public void commandOutputString(String str) {
        if (className == null) {
          className = str.substring(0, str.indexOf(Communicator.DELIMITER));
          return;
        }

        int signatureEnd = str.indexOf(Communicator.DELIMITER);
        String delimiter = "::";
        int dotDot = str.indexOf(delimiter);
        int abstractEnd = str.indexOf(Communicator.DELIMITER, signatureEnd + 1);

        if (dotDot == -1 || signatureEnd == -1 || abstractEnd == -1) return;
        boolean abstractFlag = "abstract".equals(str.substring(signatureEnd + 1, abstractEnd));
        if (!overrideMode && !abstractFlag) return;

        String methodClassName = str.substring(0, dotDot);
        if (className.equals(methodClassName)) return;

        int virtualEnd = str.indexOf(Communicator.DELIMITER, abstractEnd + 1);
        int returnTypeEnd = str.indexOf(Communicator.DELIMITER, virtualEnd + 1);
        int nameEnd = str.indexOf(Communicator.DELIMITER, returnTypeEnd + 1);
        int vargEnd = str.indexOf(Communicator.DELIMITER, nameEnd + 1);
        int constEnd = str.indexOf(Communicator.DELIMITER, vargEnd + 1);
        int argEnd = str.indexOf(Communicator.DELIMITER, constEnd + 1);

        String signature = "(";
        boolean varArg = Integer.parseInt(str.substring(nameEnd+1, vargEnd)) == 1;
        boolean isConst = Integer.parseInt(str.substring(vargEnd+1, constEnd)) == 1;
        int argCount = Integer.parseInt(str.substring(constEnd + 1, argEnd));
        int current = argEnd + 1;
        String paramNames = "";

        for(int i = 0; i < argCount; ++i) {
          if (i != 0) {
            signature += ", ";
            paramNames += ", ";
          }

          int paramNameEnd = str.indexOf(Communicator.DELIMITER, current);
          int paramTypeEnd = str.indexOf(Communicator.DELIMITER, paramNameEnd + 1);
          if (paramTypeEnd == -1) paramTypeEnd = str.length();

          String paramName = str.substring(current, paramNameEnd);
          signature+=  str.substring(paramNameEnd + 1, paramTypeEnd).replace("`ID`", paramName);
          paramNames += paramName;
          current = paramTypeEnd + 1;
        }

        if (varArg) {
           if (argCount > 1) signature += ", ";
          signature+="...";
        }

        signature += ")";
        if (isConst) signature +=" const";

        String name = str.substring(returnTypeEnd + 1, nameEnd);
        elements.add(
          new CppMethodElementNode(
            name + " " + signature,
            str.substring(virtualEnd + 1, returnTypeEnd),
            methodClassName,
            paramNames,
            name,
            abstractFlag
          )
        );
      }
    };
    command.post(project);
  }

  private void proceedWithChoosingMethods(Project project, PsiFile file, final Editor editor,
                                          List<CppMethodElementNode> candidates, boolean override) {
    final MemberChooser<CppMethodElementNode> chooser = new MemberChooser<CppMethodElementNode>(
      candidates.toArray(new CppMethodElementNode[candidates.size()]), false, true, project, false
    ) {
    };

    chooser.setTitle("Choose Methods to " + (override ? "Override":"Implement"));
    chooser.setCopyJavadocVisible(false);
    chooser.show();
    if (chooser.getExitCode() != DialogWrapper.OK_EXIT_CODE) return;
    final List<CppMethodElementNode> selectedElements = chooser.getSelectedElements();
    if (selectedElements == null || selectedElements.size() == 0) return;

    CommandProcessor.getInstance().executeCommand(project, new Runnable() {
      public void run() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            Document document = editor.getDocument();
            int offset = editor.getCaretModel().getOffset();

            for(CppMethodElementNode c:selectedElements) {
              String cType = c.getType();
              String methodText = "virtual " + cType + " " + c.getText() + " {\n" +
                (!"void".equals(cType) ? "return ":"") + (c.myIsAbstract ? "": c.getParentName() + "::" + c.myName + "(" + c.myParamNames + ");") + "\n" +
                "}\n";
              document.insertString(offset, methodText);
              offset += methodText.length();
            }
          }
        });
      }
    }, override ? "Override Methods":"Implement Methods", this);
  }

  public boolean startInWriteAction() {
    return false;
  }
}