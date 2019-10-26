// Copyright 2006-2012 AdvancedTools. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.advancedtools.cpp.inspections;

import com.advancedtools.cpp.facade.EnvironmentFacade;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;

/**
 * @author maxim
 */
public class JNIFunction2JavaMethodBinding {
  public static final String JAVA_NATIVE_PREFIX = "Java_";
  private final PsiMethod method;
  private final PsiClass clazz;

  public JNIFunction2JavaMethodBinding(String jniFunctionName, PsiManager psiManager) {
    int lastUnderscoreIndex = jniFunctionName.lastIndexOf('_');
    String className = jniFunctionName.substring(0, lastUnderscoreIndex).replace('_','.').substring(JAVA_NATIVE_PREFIX.length());
    String methodName = jniFunctionName.substring(lastUnderscoreIndex + 1);
    Project project = psiManager.getProject();
    clazz = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.projectScope(project));
    PsiMethod m = null;

    if (clazz != null) {
      PsiMethod[] psiMethods = clazz.findMethodsByName(methodName, false);
      if (psiMethods.length > 0) {
        m = psiMethods[0];
      }
    }

    method = m;
  }

  public PsiMethod getMethod() {
    return method;
  }
}
