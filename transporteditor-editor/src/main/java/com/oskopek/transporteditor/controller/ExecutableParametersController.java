package com.oskopek.transporteditor.controller;

import com.oskopek.transporteditor.view.ExecutableParametersCreator;
import com.oskopek.transporteditor.view.TextAreaValidator;
import com.oskopek.transporteditor.view.ValidationProperty;
import com.oskopek.transporteditor.view.executables.DefaultExecutableWithParameters;
import com.oskopek.transporteditor.view.executables.ExecutableWithParameters;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;

import java.util.function.Predicate;

/**
 * Controller for choosing a course out of several choices.
 */
public class ExecutableParametersController extends AbstractController {

    private Stage dialog;
    @FXML
    private Label headerText;
    @FXML
    private Label executableSubLabel;
    @FXML
    private TextArea executableArea;
    private ValidationProperty executableAreaValid;
    @FXML
    private Label parametersSubLabel;
    @FXML
    private TextArea parametersArea;
    private ValidationProperty parametersAreaValid;

    @FXML
    private Label noteLabel;

    @FXML
    private Button applyButton;
    @FXML
    private Button cancelButton;
    private ButtonBar.ButtonData result;
    private BooleanBinding allValidationsValid;

    @FXML
    private void initialize() {
        executableSubLabel.setFont(Font.font(null, FontPosture.ITALIC, 10d));
        parametersSubLabel.setFont(Font.font(null, FontPosture.ITALIC, 10d));

        noteLabel.setStyle("-fx-border-color: transparent");

        ButtonBar.setButtonData(applyButton, ButtonBar.ButtonData.APPLY);
        ButtonBar.setButtonData(cancelButton, ButtonBar.ButtonData.CANCEL_CLOSE);
    }

    public TextArea getExecutableArea() {
        return executableArea;
    }

    public TextArea getParametersArea() {
        return parametersArea;
    }

    public void setHeaderText(String headerText) {
        this.headerText.setText(headerText);
    }

    public void setNoteText(String noteText) {
        this.noteLabel.setText(noteText);
    }

    public void setExecutableSubLabelText(String subLabelText) {
        this.executableSubLabel.setText(subLabelText);
    }

    public void setParametersSubLabelText(String subLabelText) {
        this.parametersSubLabel.setText(subLabelText);
    }

    public void enableValidation(Predicate<String> executableTextValidator, String executableValidationFailed,
            Predicate<String> parameterTextValidator, String parameterValidationFailed) {
        executableAreaValid = new ValidationProperty(executableValidationFailed, executableArea);
        parametersAreaValid = new ValidationProperty(parameterValidationFailed, parametersArea);

        Predicate<String> nonEmpty = s -> !s.trim().isEmpty();
        executableAreaValid.bind(
                new TextAreaValidator(executableArea.textProperty(), nonEmpty.and(executableTextValidator)::test)
                        .isValidProperty());
        parametersAreaValid.bind(
                new TextAreaValidator(parametersArea.textProperty(), nonEmpty.and(parameterTextValidator)::test)
                        .isValidProperty());

        allValidationsValid = executableAreaValid.and(parametersAreaValid);
        applyButton.disableProperty().bind(allValidationsValid.not());
    }

    @FXML
    private void handleApplyButton() {
        if (allValidationsValid.get()) {
            result = ButtonBar.ButtonData.APPLY;
            dialog.close();
        } else {
            throw new IllegalStateException("Button should be disabled if validation failed!");
        }
    }

    @FXML
    private void handleCancelButton() {
        result = ButtonBar.ButtonData.CANCEL_CLOSE;
        dialog.close();
    }

    /**
     * Set the dialog (used for reporting double clicks in the table).
     *
     * @param dialog the dialog wrapper for {@link ExecutableParametersCreator}
     */
    public void setDialog(Stage dialog) {
        this.dialog = dialog;
    }

    public ExecutableWithParameters getExecutable() {
        return allValidationsValid.get() ? new DefaultExecutableWithParameters(executableArea.getText(),
                parametersArea.getText()) : null;
    }
}
