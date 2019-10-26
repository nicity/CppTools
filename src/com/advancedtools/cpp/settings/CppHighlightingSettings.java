// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.settings;

import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;

/**
 * @author maxim
 */
public class CppHighlightingSettings implements JDOMExternalizable {
  private static final
  @NonNls
  String REPORT_REDUNDANT_CAST_KEY = "reportRedundantCast";
  private static final
  @NonNls
  String REPORT_IMPLICIT_CAST_TO_BOOL_KEY = "reportImplicitCastToBool";
  private static final
  @NonNls
  String REPORT_NAME_NEVER_REFERENCED_KEY = "reportNameNeverReferenced";
  private static final
  @NonNls
  String REPORT_NAME_REFERENCED_ONCE_KEY = "reportNameReferencedOnce";
  private static final
  @NonNls
  String REPORT_REDUNDANT_QUALIFIER_KEY = "reportRedundantQualifier";
  private static final
  @NonNls
  String REPORT_STATIC_CALL_FROM_INSTANCE_KEY = "reportStaticCallFromInstance";
  private static final
  @NonNls
  String REPORT_UNNEEDED_BRACES_KEY = "reportUnneededBraces";

  private static final
  @NonNls
  String REPORT_DUPLICATED_SYMBOLS_KEY = "reportDuplicatedSymbols";

  private boolean myReportImplicitCastToBool = false;
  private boolean myReportRedundantCast = true;
  private boolean myReportNameNeverReferenced = true;
  private boolean myReportNameUsedOnce = false;
  private boolean myReportRedundantQualifier = true;
  private boolean myReportStaticCallFromInstance = true;
  private boolean myReportUnneededBraces = false;
  private boolean myReportDuplicatedSymbols = true;

  public void readExternal(Element element) throws InvalidDataException {
    String s = element.getAttributeValue(REPORT_IMPLICIT_CAST_TO_BOOL_KEY);
    if (s != null) myReportImplicitCastToBool = Boolean.parseBoolean(s);

    s = element.getAttributeValue(REPORT_NAME_NEVER_REFERENCED_KEY);
    if (s != null) myReportNameNeverReferenced = Boolean.parseBoolean(s);

    s = element.getAttributeValue(REPORT_NAME_REFERENCED_ONCE_KEY);
    if (s != null) myReportNameUsedOnce = Boolean.parseBoolean(s);

    s = element.getAttributeValue(REPORT_REDUNDANT_CAST_KEY);
    if (s != null) myReportRedundantCast = Boolean.parseBoolean(s);

    s = element.getAttributeValue(REPORT_REDUNDANT_QUALIFIER_KEY);
    if (s != null) myReportRedundantQualifier = Boolean.parseBoolean(s);

    s = element.getAttributeValue(REPORT_STATIC_CALL_FROM_INSTANCE_KEY);
    if (s != null) myReportStaticCallFromInstance = Boolean.parseBoolean(s);

    s = element.getAttributeValue(REPORT_UNNEEDED_BRACES_KEY);
    if (s != null) myReportUnneededBraces = Boolean.parseBoolean(s);

    s = element.getAttributeValue(REPORT_DUPLICATED_SYMBOLS_KEY);
    if (s != null) myReportDuplicatedSymbols = Boolean.parseBoolean(s);
  }

  public void writeExternal(Element element) throws WriteExternalException {
    if (!myReportImplicitCastToBool) element.setAttribute(REPORT_IMPLICIT_CAST_TO_BOOL_KEY, "false");
    if (!myReportNameNeverReferenced) element.setAttribute(REPORT_NAME_NEVER_REFERENCED_KEY, "false");
    if (!myReportNameUsedOnce) element.setAttribute(REPORT_NAME_REFERENCED_ONCE_KEY, "false");
    if (!myReportRedundantCast) element.setAttribute(REPORT_REDUNDANT_CAST_KEY, "false");
    if (!myReportRedundantQualifier) element.setAttribute(REPORT_REDUNDANT_QUALIFIER_KEY, "false");
    if (!myReportStaticCallFromInstance) element.setAttribute(REPORT_STATIC_CALL_FROM_INSTANCE_KEY, "false");
    if (myReportUnneededBraces) element.setAttribute(REPORT_UNNEEDED_BRACES_KEY, "true");
    if (!myReportDuplicatedSymbols) element.setAttribute(REPORT_DUPLICATED_SYMBOLS_KEY, "false");
  }

  public boolean toReportImplicitCastToBool() {
    return myReportImplicitCastToBool;
  }

  public boolean toReportRedundantCast() {
    return myReportRedundantCast;
  }

  public boolean toReportNameNeverReferenced() {
    return myReportNameNeverReferenced;
  }

  public boolean toReportNameUsedOnce() {
    return myReportNameUsedOnce;
  }

  public boolean toReportRedundantQualifier() {
    return myReportRedundantQualifier;
  }

  public boolean toReportStaticCallFromInstance() {
    return myReportStaticCallFromInstance;
  }

  public boolean toReportUnneededBraces() {
    return myReportUnneededBraces;
  }

  public boolean toReportDuplicatedSymbols() {
    return myReportDuplicatedSymbols;
  }

  private Settings mySettings;

  public boolean isModified() {
    return mySettings.isModified();
  }

  public JComponent createComponent() {
    mySettings = new Settings();
    return mySettings.projectSettingsPanel;
  }

  public void apply() {
    mySettings.apply();
  }

  public void init() {
    mySettings.init();
  }

  public void dispose() {
    mySettings = null;
  }

  class Settings {
    private JPanel projectSettingsPanel;
    private JCheckBox implicitCastToBoolSwitch;
    private JCheckBox redundantCastSwitch;
    private JCheckBox nameNeverReferencedSwitch;
    private JCheckBox nameUsedOnceSwitch;
    private JCheckBox redundantQualifierSwitch;
    private JCheckBox staticMemberThroughInstanceSwitch;
    private JCheckBox reportUnneededBracesSwitch;
    private JCheckBox reportDuplicatedDefinitions;

    private boolean isModified() {
      return
        myReportImplicitCastToBool != implicitCastToBoolSwitch.isSelected() ||
          myReportNameNeverReferenced != nameNeverReferencedSwitch.isSelected() ||
          myReportNameUsedOnce != nameUsedOnceSwitch.isSelected() ||
          myReportRedundantCast != redundantCastSwitch.isSelected() ||
          myReportUnneededBraces != reportUnneededBracesSwitch.isSelected() ||
          myReportRedundantQualifier != redundantQualifierSwitch.isSelected() ||
          myReportDuplicatedSymbols != reportDuplicatedDefinitions.isSelected() ||
          myReportStaticCallFromInstance != staticMemberThroughInstanceSwitch.isSelected();
    }

    private void apply() {
      myReportImplicitCastToBool = implicitCastToBoolSwitch.isSelected();
      myReportNameNeverReferenced = nameNeverReferencedSwitch.isSelected();
      myReportNameUsedOnce = nameUsedOnceSwitch.isSelected();
      myReportRedundantCast = redundantCastSwitch.isSelected();
      myReportRedundantQualifier = redundantQualifierSwitch.isSelected();
      myReportStaticCallFromInstance = staticMemberThroughInstanceSwitch.isSelected();
      myReportUnneededBraces = reportUnneededBracesSwitch.isSelected();
      myReportDuplicatedSymbols = reportDuplicatedDefinitions.isSelected();
    }

    private void init() {
      implicitCastToBoolSwitch.setSelected(myReportImplicitCastToBool);
      nameNeverReferencedSwitch.setSelected(myReportNameNeverReferenced);
      nameUsedOnceSwitch.setSelected(myReportNameUsedOnce);
      redundantCastSwitch.setSelected(myReportRedundantCast);
      redundantQualifierSwitch.setSelected(myReportRedundantQualifier);
      staticMemberThroughInstanceSwitch.setSelected(myReportStaticCallFromInstance);
      reportUnneededBracesSwitch.setSelected(myReportUnneededBraces);
      reportDuplicatedDefinitions.setSelected(myReportDuplicatedSymbols);
    }
  }
}