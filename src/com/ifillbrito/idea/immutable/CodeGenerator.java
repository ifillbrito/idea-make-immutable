package com.ifillbrito.idea.immutable;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {

    private PsiFile psiFile;

    public CodeGenerator(PsiFile psiFile) {
        this.psiFile = psiFile;
    }

    public void generate() {
        MakeImmutableVisitor visitor = new MakeImmutableVisitor();
        if (psiFile instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
            psiJavaFile.acceptChildren(visitor);
        }

        for (PsiField psiField : visitor.getPsiFields()) {
            psiField.getModifierList().setModifierProperty(PsiModifier.FINAL, true);
        }

        PsiClass psiClass = visitor.getPsiClass();
        psiClass.getModifierList().setModifierProperty(PsiModifier.FINAL, true);
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());

        PsiMethod staticMaxConstructor = createMaxStaticConstructor(visitor, psiClass, elementFactory);
        createWithers(visitor, psiClass, elementFactory, staticMaxConstructor);
        createGetters(visitor, psiClass, elementFactory, staticMaxConstructor);
        createFurtherStaticConstructors(visitor, staticMaxConstructor, psiClass, elementFactory);

    }

    private void createFurtherStaticConstructors(MakeImmutableVisitor visitor, PsiMethod staticMaxConstructor, PsiClass psiClass, PsiElementFactory elementFactory) {
        for (int i = visitor.getPsiConstructors().size() - 1; i >= 0; i--) {
            PsiMethod psiConstructor = visitor.getPsiConstructors().get(i);
            if (psiConstructor != visitor.getPsiMaxConstructor()) {
                PsiMethod staticConstructor = createStaticConstructor(psiConstructor, visitor.getStaticConstructorParameters(), psiClass, elementFactory);
                if (!visitor.methodExists(staticConstructor)) {
                    psiClass.addAfter(staticConstructor, staticMaxConstructor);
                }
            }
        }
    }

    private PsiMethod createMaxStaticConstructor(MakeImmutableVisitor visitor, PsiClass psiClass, PsiElementFactory elementFactory) {
        PsiMethod psiConstructor = visitor.getPsiMaxConstructor();
        PsiMethod staticConstructor = createStaticConstructor(psiConstructor, visitor.getStaticConstructorParameters(), psiClass, elementFactory);
        if (!visitor.methodExists(staticConstructor)) {
            return (PsiMethod) psiClass.addAfter(staticConstructor, psiConstructor);
        }
        return visitor.findMethodMatching(staticConstructor);
    }

    private PsiMethod createStaticConstructor(PsiMethod psiConstructor, String staticConstructorTypeParams, PsiClass psiClass, PsiElementFactory elementFactory) {
        psiConstructor.getModifierList().setModifierProperty(PsiModifier.PRIVATE, true);

        StringBuilder code = new StringBuilder();
        code.append(String.format("public static %s %s of(", staticConstructorTypeParams, psiClass.getName()));
        PsiParameter[] parameters = psiConstructor.getParameterList().getParameters();
        for (int i = 0; i < parameters.length; i++) {
            PsiParameter parameter = parameters[i];
            if (i < parameters.length - 1) {
                code.append(String.format("%s %s,", parameter.getType().getPresentableText(), parameter.getName()));
            } else {
                code.append(String.format("%s %s", parameter.getType().getPresentableText(), parameter.getName()));
            }
        }
        code.append(") {");
        code.append(String.format("return new %s (", psiClass.getName() + staticConstructorTypeParams));
        for (int i = 0; i < parameters.length; i++) {
            PsiParameter parameter = parameters[i];
            if (i < parameters.length - 1) {
                code.append(String.format("%s,", parameter.getName()));
            } else {
                code.append(String.format("%s", parameter.getName()));
            }
        }
        code.append("); }");
        return elementFactory.createMethodFromText(code.toString(), psiClass);
    }

    private void createWithers(MakeImmutableVisitor visitor, PsiClass psiClass, PsiElementFactory elementFactory, PsiMethod staticConstructor) {
        PsiParameter[] constructorParameters = visitor.getMaxConstructorParameters();
        for (int i = constructorParameters.length - 1; i >= 0; i--) {
            PsiParameter methodParameter = constructorParameters[i];
            String getterName = getMethodNameWithoutPrefix(methodParameter.getName());

            StringBuilder code = new StringBuilder();
            code.append(String.format("public %s with%s(%s %s)", psiClass.getName(), getterName, methodParameter.getType().getPresentableText(), methodParameter.getName()));
            code.append("{ return of(");
            for (int j = 0; j < constructorParameters.length; j++) {
                PsiParameter argumentParameter = constructorParameters[j];
                if (argumentParameter.getName().equals(methodParameter.getName())) {
                    code.append(argumentParameter.getName());
                } else {
                    code.append(String.format("%s()", getGetterPrefix(argumentParameter.getType()) + getMethodNameWithoutPrefix(argumentParameter.getName())));
                }
                if (j < constructorParameters.length - 1) {
                    code.append(",");
                }
            }
            code.append(");}");
            PsiMethod wither = elementFactory.createMethodFromText(code.toString(), psiClass);
            if (!visitor.methodExists(wither)) {
                psiClass.addAfter(wither, staticConstructor);
            }
        }
    }

    private void createGetters(MakeImmutableVisitor visitor, PsiClass psiClass, PsiElementFactory elementFactory, PsiMethod staticConstructor) {
        List<String> existingFieldNames = new ArrayList<>();
        for (int i = visitor.getPsiFields().size() - 1; i >= 0; i--) {
            PsiField psiField = visitor.getPsiFields().get(i);
            StringBuilder code = new StringBuilder();
            String getterName = getMethodNameWithoutPrefix(psiField.getName());
            String getterPrefix = getGetterPrefix(psiField.getType());
            code.append(String.format("public %s %s%s()", psiField.getType().getPresentableText(), getterPrefix, getterName));
            code.append(String.format("{ return %s; }", psiField.getName()));
            PsiMethod getter = elementFactory.createMethodFromText(code.toString(), psiClass);
            if (!visitor.methodExists(getter)) {
                psiClass.addAfter(getter, staticConstructor);
                existingFieldNames.add(psiField.getName());
            }
        }

        PsiParameter[] constructorParameters = visitor.getMaxConstructorParameters();
        for (int i = constructorParameters.length - 1; i >= 0; i--) {
            PsiParameter psiParameter = constructorParameters[i];
            if (!existingFieldNames.contains(psiParameter.getName())) {
                StringBuilder code = new StringBuilder();
                String getterName = getMethodNameWithoutPrefix(psiParameter.getName());
                String getterPrefix = getGetterPrefix(psiParameter.getType());
                code.append(String.format("public %s %s%s()", psiParameter.getType().getPresentableText(), getterPrefix, getterName));
                code.append("{ throw new RuntimeException(\"This method must be implemented\"); // TODO\n }");
                PsiMethod getter = elementFactory.createMethodFromText(code.toString(), psiClass);
                if (!visitor.methodExists(getter)) {
                    psiClass.addAfter(getter, staticConstructor);
                }
            }
        }
    }

    @NotNull
    private String getGetterPrefix(PsiType type) {
        String getterPrefix = "get";
        if (type.equalsToText("boolean")) {
            getterPrefix = "is";
        }
        return getterPrefix;
    }

    @NotNull
    private String getMethodNameWithoutPrefix(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
