package com.ifillbrito.idea.immutable;

import com.ifillbrito.idea.immutable.dataholders.CodeGenerator;
import com.intellij.psi.*;

public class MakeImmutableVisitor extends PsiRecursiveElementVisitor {

    private CodeGenerator codeGenerator;

    public MakeImmutableVisitor(CodeGenerator codeGenerator) {
        this.codeGenerator = codeGenerator;
    }

    @Override
    public void visitElement(PsiElement element) {
        if (element instanceof PsiClass) {
            codeGenerator.setPsiClass((PsiClass) element);
            return;
        }
        super.visitElement(element);
    }
}
