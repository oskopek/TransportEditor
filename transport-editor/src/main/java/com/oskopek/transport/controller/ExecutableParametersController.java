package com.oskopek.transport.controller;

import com.oskopek.transport.view.ObservableStringValidator;
import com.oskopek.transport.view.ExecutableParametersCreator;
import com.oskopek.transport.view.ValidationProperty;
import com.oskopek.transport.tools.executables.DefaultExecutableWithParameters;
import com.oskopek.transport.tools.executables.ExecutableWithParameters;
import com.sun.javafx.scene.control.skin.TextAreaSkin;
import javafx.beans.binding.BooleanBinding;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;

import java.util.function.Predicate;

/**
 * Controller for choosing an executable with templated parameters.
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

    /**
     * JavaFX initializer method. Initializes button disabling
     * and other validation.
     */
    @FXML
    private void initialize() {
        executableSubLabel.setFont(Font.font(null, FontPosture.ITALIC, 10d));
        parametersSubLabel.setFont(Font.font(null, FontPosture.ITALIC, 10d));

        noteLabel.setStyle("-fx-border-color: transparent");

        ButtonBar.setButtonData(applyButton, ButtonBar.ButtonData.APPLY);
        ButtonBar.setButtonData(cancelButton, ButtonBar.ButtonData.CANCEL_CLOSE);

        EventHandler<? super KeyEvent> textAreaTabHandler = event -> {
            if (event.getCode().equals(KeyCode.TAB)) {
                TextArea textArea = (TextArea) event.getSource();
                TextAreaSkin skin = (TextAreaSkin) textArea.getSkin();
                if (event.isShiftDown()) {
                    skin.getBehavior().traversePrevious();
                } else {
                    skin.getBehavior().traverseNext();
                }
                event.consume();
            }
        };
        executableArea.setOnKeyPressed(textAreaTabHandler);
        parametersArea.setOnKeyPressed(textAreaTabHandler);
    }

    /**
     * Gets the executable's text area.
     *
     * @return the executable's text area
     */
    public TextArea getExecutableArea() {
        return executableArea;
    }

    /**
     * Gets the parameters' text area.
     *
     * @return the parameters' text area
     */
    public TextArea getParametersArea() {
        return parametersArea;
    }

    /**
     * Sets the header text.
     *
     * @param headerText the text to set
     */
    public void setHeaderText(String headerText) {
        this.headerText.setText(headerText);
    }

    /**
     * Sets the note text.
     *
     * @param noteText the text to set
     */
    public void setNoteText(String noteText) {
        this.noteLabel.setText(noteText);
    }

    /**
     * Sets the executable sub-label text.
     *
     * @param subLabelText the text to set
     */
    public void setExecutableSubLabelText(String subLabelText) {
        this.executableSubLabel.setText(subLabelText);
    }

    /**
     * Sets the parameter sub-label text.
     *
     * @param subLabelText the text to set
     */
    public void setParametersSubLabelText(String subLabelText) {
        this.parametersSubLabel.setText(subLabelText);
    }

    /**
     * Enables validation in the dialog.
     *
     * @param executableTextValidator true iff the executable is valid
     * @param executableValidationFailed message to display iff the executable is not valid
     * @param parameterTextValidator true iff the parameters are valid
     * @param parameterValidationFailed message to display iff the parameters are not valid
     */
    public void enableValidation(Predicate<String> executableTextValidator, String executableValidationFailed,
            Predicate<String> parameterTextValidator, String parameterValidationFailed) {
        executableAreaValid = new ValidationProperty(executableValidationFailed, executableArea);
        parametersAreaValid = new ValidationProperty(parameterValidationFailed, parametersArea);

        Predicate<String> nonEmpty = s -> !s.trim().isEmpty();
        executableAreaValid.bind(new ObservableStringValidator(executableArea.textProperty(),
                nonEmpty.and(executableTextValidator)).isValidProperty());
        parametersAreaValid.bind(new ObservableStringValidator(parametersArea.textProperty(),
                nonEmpty.and(parameterTextValidator)).isValidProperty());

        allValidationsValid = executableAreaValid.and(parametersAreaValid);
        applyButton.disableProperty().bind(allValidationsValid.not());
    }

    /**
     * Handles an Apply button click.
     */
    @FXML
    private void handleApplyButton() {
        if (allValidationsValid.get()) {
            result = ButtonBar.ButtonData.APPLY;
            dialog.close();
        } else {
            throw new IllegalStateException("Button should be disabled if validation failed!");
        }
    }

    /**
     * Handles a Cancel button click.
     */
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

    /**
     * Get the resulting executable or null if the dialog wasn't submitted successfully.
     *
     * @return the executable
     */
    public ExecutableWithParameters getExecutable() {
        return allValidationsValid.get() && ButtonBar.ButtonData.APPLY.equals(result)
                ? new DefaultExecutableWithParameters(executableArea.getText(), parametersArea.getText()) : null;
    }
}
