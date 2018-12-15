package com.ifillbrito.idea.immutable;

import com.ifillbrito.idea.immutable.dataholders.CodeGenerator;
import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.List;

public class CodeProcessor {

    private PsiFile psiFile;

    public CodeProcessor(PsiFile psiFile) {
        this.psiFile = psiFile;
    }

    public void processFile() {
        CodeGenerator generator = new CodeGenerator();
        if (psiFile instanceof PsiJavaFile) {
            MakeImmutableVisitor visitor = new MakeImmutableVisitor(generator);
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            psiJavaFile.acceptChildren(visitor);
        }

        PsiClass psiClass = generator.getPsiClass();
        psiClass.getModifierList().setModifierProperty(PsiModifier.FINAL, true);

        for (PsiField psiField : generator.getPsiFields()) {
            psiField.getModifierList().setModifierProperty(PsiModifier.FINAL, true);
        }

        for (PsiMethod constructor : psiClass.getConstructors()) {
            constructor.getModifierList().setModifierProperty(PsiModifier.PRIVATE, true);
        }

        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        List<String> methods = new ArrayList<>();
        methods.addAll(generator.createStaticConstructors());
        methods.addAll(generator.createGetters());
        methods.addAll(generator.createWithers());
        for (int i = methods.size() - 1; i >= 0; i--) {
            String method = methods.get(i);
            PsiMethod psiMethod = elementFactory.createMethodFromText(method, psiClass);
            if (!generator.methodExists(psiMethod)){
                PsiMethod lastConstructor = generator.getLastConstructor();
                psiClass.addAfter(psiMethod, lastConstructor);
            }
        }
    }
}
