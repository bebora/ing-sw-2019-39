package it.polimi.se2019.view.gui;

import it.polimi.se2019.Logger;
import it.polimi.se2019.Priority;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class FinalScreen extends Pane {
    @FXML
    private ImageView loseView;
    public FinalScreen(boolean lose, Stage primaryStage, List<String> winners){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(
                "fxml/FinalScreen.fxml"));
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            Logger.log(Priority.DEBUG, exception.getMessage());
        }
        GuiHelper.resizeToScreenSize(this);
        primaryStage.setScene(new Scene(this));
        primaryStage.setFullScreenExitHint("Press SPACEBAR to see the list of winners");
        primaryStage.setFullScreen(true);
        if(lose) {
            FadeTransition ft = new FadeTransition(Duration.millis(3000), loseView);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.setCycleCount(1);
            ft.play();
        }
        primaryStage.addEventHandler(KeyEvent.KEY_RELEASED, (KeyEvent event) -> {
            if (KeyCode.SPACE == event.getCode()) {
                String win = "Winners are: ";
                for(String s: winners)
                    win = win.concat(s + " ");
                InfoAlert.handleAlert(win);
            }
        });
    }
}