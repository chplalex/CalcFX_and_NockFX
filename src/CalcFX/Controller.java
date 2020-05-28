package CalcFX;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.mariuszgromada.math.mxparser.Expression;

public class Controller {

    @FXML
    private TextField txtExpression;
    @FXML
    private TextField txtResult;
    @FXML
    private Slider sldPrecision;

    public void keyPressed(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            doCalc();
            return;
        }
        if (event.getCode().equals(KeyCode.ESCAPE)) {
            txtExpression.clear();
            txtResult.clear();
            return;
        }
    }

    public void doCalc() {
        Expression expression = new Expression(txtExpression.getText());
        double result = expression.calculate();
        if (Double.isNaN(result)) {
            txtResult.setText("expression error");
        } else {
            txtResult.setText(String.format("%." + (int) sldPrecision.getValue() + "f", result));
        }
    }

}
