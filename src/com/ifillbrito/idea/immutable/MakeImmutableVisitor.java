package com.ifillbrito.idea.immutable;

import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.List;

public class MakeImmutableVisitor extends PsiRecursiveElementVisitor {

    private PsiClass psiClass;
    private List<PsiField> psiFields;
    private PsiMethod psiConstructor;

    public MakeImmutableVisitor() {
        psiFields = new ArrayList<>();
    }

    @Override
    public void visitElement(PsiElement element) {
        if (element instanceof PsiClass) {
            psiClass = (PsiClass) element;
        } else if (element instanceof PsiField) {
            psiFields.add((PsiField) element);
        } else if (element instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) element;
            if (method.isConstructor()) {
                if (psiConstructor != null ){
                    throw new RuntimeException("Only one constructor allowed.");
                }
                psiConstructor = (PsiMethod) element;
            }
        }
        super.visitElement(element);
    }

    public PsiClass getPsiClass() {
        return psiClass;
    }

    public List<PsiField> getPsiFields() {
        return psiFields;
    }

    public PsiMethod getPsiConstructor() {
        return psiConstructor;
    }

    public PsiParameter[] getConstructorParameters() {
        return psiConstructor.getParameterList().getParameters();
    }
}
