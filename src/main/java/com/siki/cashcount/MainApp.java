package com.siki.cashcount;

import com.siki.cashcount.config.ConfigManager;
import com.siki.cashcount.data.DataManager;
import com.siki.cashcount.exception.JsonDeserializeException;
import com.siki.cashcount.helper.Notification;
import com.siki.cashcount.helper.StopWatch;
import com.siki.cashcount.helper.ToolTipFixer;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainApp.class);
    private static final String LOG_PERFORMANCE = "LogPerformance";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        initConfig();

        if (ConfigManager.getBooleanProperty(LOG_PERFORMANCE)) StopWatch.start("App start");
        ToolTipFixer.setupCustomTooltipBehavior(50, 60000, 50);

        loadStage(stage);

        if (ConfigManager.getBooleanProperty(LOG_PERFORMANCE)) StopWatch.start("App show");
        stage.show();
        if (ConfigManager.getBooleanProperty(LOG_PERFORMANCE)) StopWatch.stop("App show");

        if (ConfigManager.getBooleanProperty(LOG_PERFORMANCE)) StopWatch.stop("App start");
    }

    private void initConfig() {
        try {
            ConfigManager.initProperties();
            Locale.setDefault(Locale.forLanguageTag("hu-HU"));
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hiba");
            alert.setHeaderText("Konfigurációs fájl hiba");
            alert.showAndWait();
            Platform.exit();
        }
    }

    private void loadStage(Stage stage) throws IOException {
        Scene scene = getScene();

        stage.setTitle("CashCount");
        stage.setScene(scene);
        setCloseBehaviour(stage);
    }

    private Scene getScene() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        return scene;
    }

    private void setCloseBehaviour(Stage stage) {
        stage.setOnCloseRequest((WindowEvent event) -> {
            try {
                if (DataManager.getInstance().needSave()) {
                    Optional<ButtonType> result = Notification.showYesNoCancel();
                    if (result.isPresent() && result.get().getButtonData() == ButtonData.YES) {
                        DataManager.getInstance().saveDailyBalances();
                    } else if (result.isPresent() && result.get().getButtonData() == ButtonData.CANCEL_CLOSE) {
                        event.consume();
                    }
                }
            } catch (IOException | JsonDeserializeException ex) {
                LOGGER.error("Error while closing application", ex);
            }
        });
    }
}
