// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.commands;

import com.advancedtools.cpp.actions.generate.BaseGenerateAction;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.advancedtools.cpp.communicator.Communicator;
import com.advancedtools.cpp.communicator.CommunicatorCommand;
import com.advancedtools.cpp.facade.EnvironmentFacade;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.editor.Editor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author maxim
 */
public class GenerateSomethingCommand extends CommunicatorCommand {
  private final String fileName;
  private final Editor editor;
  private final int start;
  private String className;
  private List<String> fieldNames;
  private List<String> fieldTypes;
  private BaseGenerateAction.GenerateType type;

  public GenerateSomethingCommand(String _fileName, Editor _editor, BaseGenerateAction.GenerateType _type) {
    _fileName = BuildingCommandHelper.fixVirtualFileName(_fileName);
    fileName = _fileName;

    editor = _editor;
    start = editor.getCaretModel().getOffset();
    type = _type;
    fieldNames = new ArrayList<String>(1);
    fieldTypes = new ArrayList<String>(1);
  }

  public boolean doInvokeInDispatchThread() {
    return true;
  }

  public void doExecute() {
    if (className == null) return;
    final TemplateManager manager = TemplateManager.getInstance(editor.getProject());
    final Template t = manager.createTemplate("","");
    Expression expr;

    switch (type) {
      case CONSTRUCTOR:
        t.addTextSegment(className + "::" + className + "() ");
        t.addTextSegment("{\n");
        t.addTextSegment("}");
        t.addEndVariable();
      break;
      case COPY_CONSTRUCTOR:
        t.addTextSegment(className + "::" + className + "(const " + className + " & ");
        expr = new EnvironmentFacade.TextExpression("arg");
        t.addVariable("arg",expr,expr, true);
        t.addTextSegment(") {\n");
        t.addTextSegment("  this->operator=(");
        t.addVariableSegment("arg");
        t.addTextSegment(");\n}");
      break;
      case ASSIGNMENT_OPERATOR:
        t.addTextSegment("const " + className + "& " + className + "::operator = ( const " + className + "& ");
        expr = new EnvironmentFacade.TextExpression("arg");

        t.addVariable("arg", expr, expr, true);
        t.addTextSegment(") {\n");

        for (String fieldName1 : fieldNames) {
          t.addTextSegment("  " + fieldName1 + " = ");
          t.addVariableSegment("arg");
          t.addTextSegment("." + fieldName1 + ";\n");
        }
        t.addTextSegment("\n  return *this;\n}");
      break;
      case CONSTRUCTOR_WITH_PARAMETERS:
        t.addTextSegment(className + "::" + className + "(");

        if (fieldNames.size() > 0) {
          for (int i = 0; i < fieldNames.size(); ++i) {
            final String fieldName =  fieldNames.get (i);
            String fieldType =  fieldTypes.get (i);
            fieldType = fieldType.substring(0, fieldType.lastIndexOf(' '));
            if (i != 0) t.addTextSegment(", ");
            t.addTextSegment(fieldType + " ");
            final Expression argName = new EnvironmentFacade.TextExpression("_" + fieldName);
            t.addVariable(fieldName, argName, argName, true);
          }
        }

        t.addTextSegment(")");

        if (fieldNames.size() > 0) {
          t.addTextSegment(":\n");
          for (int i = 0; i < fieldNames.size(); ++i) {
            final String fieldName =  fieldNames.get (i);
            if (i !=0) t.addTextSegment(",\n");
            t.addTextSegment("  " + fieldName + "(");
            t.addVariableSegment(fieldName);
            t.addTextSegment(")");
          }
        }
        t.addTextSegment("\n{\n");
        t.addTextSegment("}");
        t.addEndVariable();
      break;
    }

    manager.startTemplate(editor, t);
  }

  public void commandOutputString(String str) {
    if (className == null) className = str;
    else {
      final int index = str.indexOf(Communicator.DELIMITER);
      fieldNames.add(str.substring(index + 1));
      fieldTypes.add(str.substring(0,index));
    }
  }

  public String getCommand() {
    final String quotedFileName = BuildingCommandHelper.quote(fileName);
    return "generate-at " +
           (type == BaseGenerateAction.GenerateType.ASSIGNMENT_OPERATOR ? "asgn":
             type == BaseGenerateAction.GenerateType.COPY_CONSTRUCTOR ? "copy":"init"
           ) +
            " " + quotedFileName + " " + start;
  }

}
