package otpproju;

import javafx.application.Application;
import javafx.stage.Stage;
import otpproju.screens.Login;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        new Login().show(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}