// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.siyeh.ig.junit;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiFormatUtil;
import com.intellij.psi.util.PsiFormatUtilBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.refactoring.JavaRefactoringFactory;
import com.intellij.refactoring.changeSignature.ParameterInfoImpl;
import com.intellij.util.VisibilityUtil;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.InspectionGadgetsFix;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Bas Leijdekkers
 */
public class MakePublicStaticVoidFix extends InspectionGadgetsFix {
  private final @IntentionName String myName;
  private final boolean myMakeStatic;
  private final String myNewVisibility;

  public MakePublicStaticVoidFix(PsiMethod method, boolean makeStatic) {
    this(method, makeStatic, PsiModifier.PUBLIC);
  }

  public MakePublicStaticVoidFix(PsiMethod method, boolean makeStatic, @Nullable @PsiModifier.ModifierConstant String newVisibility) {
    final int formatOptions = PsiFormatUtilBase.SHOW_NAME | PsiFormatUtilBase.SHOW_MODIFIERS
                              | PsiFormatUtilBase.SHOW_PARAMETERS | PsiFormatUtilBase.SHOW_TYPE;
    final String methodBefore = PsiFormatUtil.formatMethod(method, PsiSubstitutor.EMPTY, formatOptions, PsiFormatUtilBase.SHOW_TYPE);

    String presentableVisibility = "";
    if (newVisibility != null) presentableVisibility = VisibilityUtil.getVisibilityString(newVisibility);
    final @NonNls String methodAfter = (presentableVisibility.isEmpty() ? presentableVisibility : presentableVisibility + " ") +
                                       (makeStatic ? "static " : "") +
                                       "void " + method.getName() + "()";

    myName = InspectionGadgetsBundle.message("make.public.static.void.fix.name", methodBefore, methodAfter);
    myMakeStatic = makeStatic;
    myNewVisibility = newVisibility;
  }

  @Override
  protected void doFix(final Project project, ProblemDescriptor descriptor) {
    final PsiMethod method = PsiTreeUtil.getParentOfType(descriptor.getPsiElement(), PsiMethod.class);
    if (method != null) {
      ParameterInfoImpl @NotNull [] parameterInfo = new ParameterInfoImpl[0];
      var csp = JavaRefactoringFactory.getInstance(project)
        .createChangeSignatureProcessor(method, false, myNewVisibility, method.getName(), PsiType.VOID, parameterInfo, null, null,
                                        null, infos -> {
            PsiUtil.setModifierProperty(method, PsiModifier.STATIC, myMakeStatic);
          });
      csp.run();
    }
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return InspectionGadgetsBundle.message("make.public.static.void.fix.family.name");
  }

  @Override
  @NotNull
  public String getName() {
    return myName;
  }
}
