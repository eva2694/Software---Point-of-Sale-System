package ee.ut.math.tvt.salessystem.ui.controllers;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXMLLoader;
import java.io.IOException;


public class ErrorDialogController implements Initializable {

    @FXML
    private Label errorMessageLabel;

    public ErrorDialogController() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ee/ut/math/tvt/salessystem/ui/ErrorDialog.fxml"));
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //...
    }
    public void setErrorMessage(String errorMessage) {
        errorMessageLabel.setText(errorMessage);
    }
}
