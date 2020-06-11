package NockFX.Client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ControllerSingUp {

    @FXML
    private TextField txtNick;
    @FXML
    private TextField txtLogin;
    @FXML
    private PasswordField txtPassword1;
    @FXML
    private PasswordField getTxtPassword2;

    private ControllerClient controllerClient;

    public void setControllerClient(ControllerClient controllerClient) {
        this.controllerClient = controllerClient;
    }

    public void doSingUp(ActionEvent actionEvent) {
        // тут будет дописан код проверки заполнения полей на корректность
        controllerClient.trySingUp(txtNick.getText().trim(), txtLogin.getText().trim(), txtPassword1.getText().trim());
    }

    public void doCancel(ActionEvent actionEvent) {
    }
}
