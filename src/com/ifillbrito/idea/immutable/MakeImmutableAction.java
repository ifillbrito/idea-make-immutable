package com.ifillbrito.idea.immutable;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang.exception.ExceptionUtils;

public class MakeImmutableAction extends BaseGenerateAction {

    private static final String CONFIRMATION_MESSAGE = "Do you want to make this class immutable?";

    private static final String CONFIRMATION_TITLE = "Confirmation";
    private static final String ERROR_TITLE = "Error";
    private static final String OK_TEXT = "Ok";
    private static final String CANCEL_TEXT = "Cancel";
    private static final int OK_INDEX = 0;
    private static final String[] YES_NO_BUTTONS = {OK_TEXT, CANCEL_TEXT};

    public MakeImmutableAction() {
        super((project, editor, psiFile) -> {
            try {
                int answer = Messages.showDialog(project, CONFIRMATION_MESSAGE, CONFIRMATION_TITLE, YES_NO_BUTTONS, 0, Messages.getQuestionIcon());
                if (answer == OK_INDEX) {
                    CodeGenerator codeCodeGenerator = new CodeGenerator(psiFile);
                    codeCodeGenerator.generate();
                }
            } catch (Throwable t) {
                Messages.showDialog(project, ExceptionUtils.getFullStackTrace(t), ERROR_TITLE, new String[]{OK_TEXT}, -1, Messages.getErrorIcon());
            }
        });
    }
}
