package com.ifillbrito.idea.immutable;

import com.intellij.psi.*;

import java.util.Arrays;
import java.util.List;

public class MakeImmutableVisitor extends PsiRecursiveElementVisitor {

    private PsiClass psiClass;

    @Override
    public void visitElement(PsiElement element) {
        if (element instanceof PsiClass) {
            if (psiClass == null) {
                psiClass = (PsiClass) element;
            }
        }
        super.visitElement(element);
    }

    public PsiClass getPsiClass() {
        return psiClass;
    }

    public List<PsiField> getPsiFields() {
        return Arrays.asList(psiClass.getAllFields());
    }

    public List<PsiMethod> getPsiConstructors() {
        return Arrays.asList(psiClass.getConstructors());
    }

    public PsiMethod getPsiMaxConstructor() {
        PsiMethod maxConstructor = null;
        for (PsiMethod psiConstructor : getPsiConstructors()) {
            if (maxConstructor == null || getParameterLength(maxConstructor) < getParameterLength(psiConstructor)) {
                maxConstructor = psiConstructor;
            }
        }
        return maxConstructor;
    }

    private int getParameterLength(PsiMethod maxConstructor) {
        return maxConstructor.getParameterList().getParameters().length;
    }

    public PsiParameter[] getMaxConstructorParameters() {
        return getPsiMaxConstructor().getParameterList().getParameters();
    }

    public String getStaticConstructorParameters() {
        PsiTypeParameter[] typeParameters = psiClass.getTypeParameters();
        StringBuilder parameters = new StringBuilder();
        for (int i = 0; i < typeParameters.length; i++) {
            PsiTypeParameter typeParameter = typeParameters[i];
            if (i == 0) {
                parameters.append("<");
            }
            parameters.append(typeParameter.getText());
            if (i < typeParameters.length - 1) {
                parameters.append(",");
            } else {
                parameters.append(">");
            }
        }
        return parameters.toString();
    }

    public boolean methodExists(PsiMethod newPsiMethod) {
        for (PsiMethod existingPsiMethod : psiClass.getAllMethods()) {
            if (sameMethod(newPsiMethod, existingPsiMethod)
            ) {
                return true;
            }
        }
        return false;
    }

    private boolean sameMethod(PsiMethod newPsiMethod, PsiMethod existingPsiMethod) {
        return existingPsiMethod.getName().equals(newPsiMethod.getName()) &&
                equalsReturnType(newPsiMethod, existingPsiMethod) &&
                equalsParameterTypes(existingPsiMethod, newPsiMethod);
    }

    public PsiMethod findMethodMatching(PsiMethod psiMethod){
        for (PsiMethod existingPsiMethod : psiClass.getAllMethods()) {
            if(sameMethod(existingPsiMethod, psiMethod)){
                return existingPsiMethod;
            }
        }
        return null;
    }

    private boolean equalsReturnType(PsiMethod newPsiMethod, PsiMethod existingPsiMethod) {
        PsiType existingPsiMethodReturnType = existingPsiMethod.getReturnType();
        PsiType newPsiMethodReturnType = newPsiMethod.getReturnType();
        if (existingPsiMethodReturnType == null && newPsiMethodReturnType == null) {
            return true;
        } else if (
                existingPsiMethodReturnType != null && newPsiMethodReturnType != null &&
                        existingPsiMethodReturnType.getPresentableText().equals(newPsiMethodReturnType.getPresentableText())) {
            return true;
        }
        return false;
    }

    private boolean equalsParameterTypes(PsiMethod existingPsiMethod, PsiMethod newPsiMethod) {
        PsiParameter[] existingPsiMethodParams = existingPsiMethod.getParameterList().getParameters();
        PsiParameter[] newPsiMethodParams = newPsiMethod.getParameterList().getParameters();
        if (existingPsiMethodParams.length != newPsiMethodParams.length){
            return false;
        }
        for (int i = 0; i < existingPsiMethodParams.length; i++) {
            PsiParameter existingPsiMethodParam = existingPsiMethodParams[i];
            PsiParameter newPsiMethodParam = newPsiMethodParams[i];
            if (!existingPsiMethodParam.getType().getPresentableText().equals(newPsiMethodParam.getType().getPresentableText())){
                return false;
            }
        }
        return true;
    }
}
