// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp;

import com.advancedtools.cpp.communicator.BlockingCommand;
import com.advancedtools.cpp.communicator.BuildingCommandHelper;
import com.advancedtools.cpp.communicator.Communicator;
import com.advancedtools.cpp.psi.CppElement;
import com.advancedtools.cpp.psi.CppFile;
import com.advancedtools.cpp.psi.CppKeyword;
import com.advancedtools.cpp.psi.ICppElement;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.Grouper;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.ide.util.treeView.smartTree.ActionPresentation;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Comparator;

/**
 * @author maxim
 */
public class CppStructureViewBuilder extends TextEditorBasedStructureViewModel {
  private PsiFile myFile;
  private static final Sorter ourKindSorter = new Sorter() {
    private final Comparator comparator = new Comparator() {
      public int compare(Object o1, Object o2) {
        return getWeight(o1) - getWeight(o2);
      }

      private int getWeight(Object o1) {
        final CppStructureViewTreeElement c1 = (CppStructureViewTreeElement) o1;
        final Icon icon = c1.getIcon();
        if (icon == Icons.ANNOTATION_TYPE_ICON) return 5;
        if (icon == Icons.CLASS_ICON) return 10;
        if (icon == Icons.METHOD_ICON) return 20;
        return 30;
      }
    };

    public Comparator getComparator() {
      return comparator;
    }

    @NotNull
    public ActionPresentation getPresentation() {
      return null; // will not be shown
    }

    @NotNull
    public String getName() {
      return "KIND";
    }

    public boolean isVisible() {
      return false;
    }
  };

  protected CppStructureViewBuilder(PsiFile psiFile) {
    super(psiFile);
    myFile = psiFile;
  }

  protected PsiFile getPsiFile() {
    return myFile;
  }

  @NotNull
  protected Class[] getSuitableClasses() {
    return new Class[] {
      CppElement.class
    };
  }

  static class OutlineData {
    final String structureItem;
    List<OutlineData> nestedOutlines;
    int maxCount;

    OutlineData(String val) {
      structureItem = val;
    }
  }

  private static final Key<CachedValue<StructureViewTreeElement>> ourCachedKey = Key.create("cpp.structure.view.root");

  @NotNull
  public StructureViewTreeElement getRoot() {
    CachedValue<StructureViewTreeElement> value = myFile.getUserData(ourCachedKey);
    if (value == null) {
      value = CachedValuesManager.getManager(myFile.getManager().getProject()).createCachedValue(new CachedValueProvider<StructureViewTreeElement>() {
        public Result<StructureViewTreeElement> compute() {
          final OutlineCommand outlineCommand = new OutlineCommand(myFile.getVirtualFile().getPath());
          outlineCommand.post(myFile.getProject());

          final Communicator communicator = Communicator.getInstance(myFile.getProject());

          if (outlineCommand.hasReadyResult()) {
            final OutlineData first = outlineCommand.outlineDatumStack.getFirst();
            return new Result<StructureViewTreeElement>(
              new CppStructureViewTreeElement((CppFile) myFile, first),
              communicator.getServerRestartTracker(),
              communicator.getModificationTracker()
            );
          }

          return new Result<StructureViewTreeElement>(new CppStructureViewTreeElement((CppFile) myFile, null),
            communicator.getServerRestartTracker(),
            communicator.getModificationTracker()
          );
        }
      }, false);

      myFile.putUserData(ourCachedKey, value);
    }

    return value.getValue();
  }

  static class OutlineCommand extends BlockingCommand {
    private final String fname;

    private LinkedList<OutlineData> outlineDatumStack = new LinkedList<OutlineData>();
    private OutlineData current;

    OutlineCommand(String filename) {
      fname = BuildingCommandHelper.fixVirtualFileName(filename);
      outlineDatumStack.add(new OutlineData(null));
    }

    public void commandOutputString(String str) {
      OutlineData currentParent = outlineDatumStack.getLast();

      if (str.startsWith("OUTLINES:")) {
        if (current != null) {
          currentParent = current;
          outlineDatumStack.add(currentParent);
        }

        final int capacity = Integer.parseInt(str.substring(str.indexOf(':') + 1,str.length()));
        currentParent.nestedOutlines = new ArrayList<OutlineData>(capacity);
        currentParent.maxCount = capacity;
      } else if (currentParent != null && currentParent.nestedOutlines != null) {
        current = new OutlineData(BuildingCommandHelper.unquote(str));
        currentParent.nestedOutlines.add(current);
        if (currentParent.nestedOutlines.size() == currentParent.maxCount && outlineDatumStack.size() > 1) {
          outlineDatumStack.removeLast();
        }
      }
    }

    public String getCommand() {
      return "outline "+ BuildingCommandHelper.quote(fname);
    }
  }
  @NotNull
  public Grouper[] getGroupers() {
    return Grouper.EMPTY_ARRAY;
  }

  @NotNull
  public Sorter[] getSorters() {
    return new Sorter[] {ourKindSorter, Sorter.ALPHA_SORTER};
  }

  @NotNull
  public Filter[] getFilters() {
    return Filter.EMPTY_ARRAY;
  }

  private static class CppStructureViewTreeElement implements StructureViewTreeElement {
    private final ICppElement myElement;
    private StructureViewTreeElement[] myChildren;
    private OutlineData myOutlineData;

    CppStructureViewTreeElement(CppFile file, OutlineData data) {
      if (data != null && data.structureItem != null) {
        int offset = Integer.parseInt(data.structureItem.substring(0, data.structureItem.indexOf(Communicator.DELIMITER)));
        final PsiElement psiElement = file.findElementAt(offset);
        final PsiElement element = psiElement != null ? psiElement.getParent():file;
        myElement = (ICppElement) element;
      } else {
        myElement = file;
      }
      myOutlineData = data;
    }

    public ICppElement getValue() {
      return myElement;
    }

    public StructureViewTreeElement[] getChildren() {
      if (myChildren == null) {
        if (myOutlineData != null) {
          StructureViewTreeElement[] children = new StructureViewTreeElement[myOutlineData.maxCount];
          final CppFile cppFile = (CppFile) myElement.getContainingFile();
          final List<OutlineData> list = myOutlineData.nestedOutlines;

          for(int i = 0; i < children.length; ++i) {
            children[i] = new CppStructureViewTreeElement(cppFile, list.get(i));
          }
          myChildren = children;
        } else {
          myChildren = EMPTY_ARRAY;
        }
      }
      return myChildren;
    }

    public ItemPresentation getPresentation() {
      return new ItemPresentation() {
        public String getPresentableText() {
          if (myOutlineData != null && myOutlineData.structureItem != null) {
            final int index = myOutlineData.structureItem.indexOf(Communicator.DELIMITER, myOutlineData.structureItem.indexOf(Communicator.DELIMITER) + 1) + 1;
            return myOutlineData.structureItem.substring(index, myOutlineData.structureItem.lastIndexOf(Communicator.DELIMITER));
          }
          if (myElement instanceof CppKeyword) {
            PsiElement el = myElement.getNextSibling();
            if (el instanceof PsiWhiteSpace) el = el.getNextSibling();
            if (el != null) return myElement.getText() + " " + el.getText();
          }
          return myElement instanceof CppFile ? myElement.getName() : myElement.getText();
        }

        @Nullable
        public String getLocationString() {
          return null;
        }

        @Nullable
        public Icon getIcon(boolean b) {
          return CppStructureViewTreeElement.this.getIcon();
        }

        @Nullable
        public TextAttributesKey getTextAttributesKey() {
          return null;
        }
      };
    }

    private Icon getIcon() {
      if (myOutlineData == null || myOutlineData.structureItem == null) return null;
      final char s = myOutlineData.structureItem.substring(myOutlineData.structureItem.lastIndexOf(Communicator.DELIMITER) + 1).charAt(0);
      switch (s) {
        case 'v': return Icons.VARIABLE_ICON;
        case 'c': case 'd': //return Icons.CLASS_INITIALIZER;
        case 'm': return Icons.METHOD_ICON;
        case 't': return Icons.CLASS_ICON;
        case 'i': case 'p': return Icons.ANNOTATION_TYPE_ICON;
      }

      return null;
    }

    public void navigate(boolean b) {
      myElement.navigate(true);
    }

    public boolean canNavigate() {
      return true;
    }

    public boolean canNavigateToSource() {
      return canNavigate();
    }
  }
}
