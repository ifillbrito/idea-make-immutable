package com.ifillbrito.idea.immutable.dataholders;

import com.ifillbrito.idea.immutable.NoConstructorFoundException;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.*;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CodeGenerator {

    private String className;
    private List<String> classParameters;
    private List<ClassConstructor> classConstructors;
    private List<ClassField> classFields;
    private PsiClass psiClass;

    public void setPsiClass(PsiClass psiClass) {
        this.psiClass = psiClass;
        this.className = psiClass.getName();
        this.classParameters = Arrays.stream(psiClass.getTypeParameters())
                .map(NavigationItem::getName)
                .collect(Collectors.toList());
        this.classConstructors = Arrays.stream(psiClass.getConstructors())
                .map(psiMethod -> {
                    ClassConstructor classConstructor = new ClassConstructor();
                    for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
                        classConstructor.addArgument(
                                parameter.getType().getPresentableText(),
                                parameter.getName()
                        );
                    }
                    return classConstructor;
                })
                .collect(Collectors.toList());
        if (this.classConstructors.isEmpty()) {
            throw new NoConstructorFoundException();
        }
        this.classFields = Arrays.stream(psiClass.getFields())
                .map(psiField -> {
                    ClassField classField = new ClassField();
                    classField.setType(psiField.getType().getPresentableText());
                    classField.setName(psiField.getName());
                    return classField;
                })
                .collect(Collectors.toList());
    }


    public List<String> createStaticConstructors() {
        List<String> result = new ArrayList<>();
        String template =
                "public static ${class_parameters} ${class_name} of(${arguments}) { " +
                        "return new ${class_name}${diamonds}(${argument_values});" +
                        "}";
        for (ClassConstructor constructor : classConstructors) {
            String code = template
                    .replace(
                            "${class_parameters}",
                            classParameters.isEmpty() ? "" : String.format("<%s>", StringUtils.join(classParameters, ","))
                    )
                    .replaceAll("\\$\\{class_name}", className)
                    .replace("${arguments}", StringUtils.join(constructor.getMethodArguments(), ","))
                    .replace("${diamonds}", classParameters.isEmpty() ? "" : "<>")
                    .replace("${argument_values}", StringUtils.join(constructor.getArgumentValues(), ","));
            result.add(code);
        }
        return result;
    }

    public List<String> createWithers() {
        List<String> result = new ArrayList<>();
        String template =
                "public ${class_name} with${argument_name}(${argument}) { " +
                        "return of(${argument_values});" +
                        "}";
        ClassConstructor constructor = getMaxConstructor();
        for (MethodArgument argument : constructor.getArguments()) {
            String code = template
                    .replace("${class_name}", className)
                    .replace("${argument_name}", argument.getCapitalizedName())
                    .replace("${argument}", String.format("%s %s", argument.getType(), argument.getPlainName()))
                    .replace("${argument_values}", StringUtils.join(
                            constructor.getArguments()
                                    .stream()
                                    .map(arg -> arg.getPlainName().equals(argument.getPlainName()) ?
                                            arg.getPlainName() :
                                            String.format("%s%s()", getGetterPrefix(arg.getType()), arg.getCapitalizedName()))
                                    .collect(Collectors.toList()),
                            ","));
            result.add(code);
        }
        return result;
    }

    public List<String> createGetters() {
        List<String> result = new ArrayList<>();
        List<String> getterNames = new ArrayList<>();
        String templateForField =
                "public ${fieldType} ${getterPrefix}${capitalizedFieldName}() { " +
                        "return this.${fieldName};" +
                        "}";
        for (ClassField classField : classFields) {
            String code = templateForField
                    .replace("${fieldType}", classField.getType())
                    .replace("${getterPrefix}", getGetterPrefix(classField.getType()))
                    .replace("${capitalizedFieldName}", classField.getCapitalizedName())
                    .replace("${fieldName}", classField.getPlainName());
            result.add(code);
            getterNames.add(classField.getPlainName());
        }

        String templateForConstructorArgument =
                "public ${fieldType} ${getterPrefix}${capitalizedFieldName}() { " +
                        "throw new IllegalArgumentException(\"This method must be implemented.\"); // TODO\n" +
                        "}";
        for (MethodArgument argument : getMaxConstructor().getArguments()) {
            if (!getterNames.contains(argument.getPlainName())) {
                String code = templateForConstructorArgument.replace("${fieldType}", argument.getType())
                        .replace("${getterPrefix}", getGetterPrefix(argument.getType()))
                        .replace("${capitalizedFieldName}", argument.getCapitalizedName());
                result.add(code);
            }
        }
        return result;
    }

    private String getGetterPrefix(String type) {
        String getterPrefix = "get";
        if ("boolean".equals(type)) {
            getterPrefix = "is";
        }
        return getterPrefix;
    }

    private ClassConstructor getMaxConstructor() {
        ClassConstructor maxConstructor = null;
        for (ClassConstructor constructor : classConstructors) {
            if (maxConstructor == null || constructor.getMethodArguments().size() > maxConstructor.getMethodArguments().size()) {
                maxConstructor = constructor;
            }
        }
        return maxConstructor;
    }


    public PsiClass getPsiClass() {
        return psiClass;
    }

    public List<PsiField> getPsiFields() {
        return Arrays.asList(psiClass.getAllFields());
    }

    public PsiMethod getLastConstructor() {
        return psiClass.getConstructors()[psiClass.getConstructors().length - 1];
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
        if (existingPsiMethodParams.length != newPsiMethodParams.length) {
            return false;
        }
        for (int i = 0; i < existingPsiMethodParams.length; i++) {
            PsiParameter existingPsiMethodParam = existingPsiMethodParams[i];
            PsiParameter newPsiMethodParam = newPsiMethodParams[i];
            if (!existingPsiMethodParam.getType().getPresentableText().equals(newPsiMethodParam.getType().getPresentableText())) {
                return false;
            }
        }
        return true;
    }
}
