package com.siki.cashcount;

import com.siki.cashcount.config.ConfigManager;
import com.siki.cashcount.data.DataManager;
import com.siki.cashcount.exception.JsonDeserializeException;
import com.siki.cashcount.helper.StopWatch;
import com.siki.cashcount.helper.ToolTipFixer;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import static javafx.application.Application.launch;

public class MainApp extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        try {
            ConfigManager.initProperties();
        } catch (IOException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hiba");
            alert.setHeaderText("Konfigurációs fájl hiba");
            alert.showAndWait();
            Platform.exit();
        }
        
        
        ToolTipFixer.setupCustomTooltipBehavior(50, 60000, 50);
        
        if (ConfigManager.getBooleanProperty("LogPerformance")) StopWatch.start("App start");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        Parent root = fxmlLoader.load();
        MainWindowController controller = fxmlLoader.getController();
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        
        stage.setTitle("CashCount");
        
        stage.setScene(scene);
        stage.setOnCloseRequest((WindowEvent event) -> {
            try {
                if (DataManager.getInstance().needSave()) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Mentés");
                    alert.setHeaderText("Mentés szükséges");
                    alert.setContentText("Az adatok megváltoztak! Szeretnéd menteni a változásokat?");
                    ButtonType buttonTypeYes = new ButtonType("Igen", ButtonData.YES);
                    ButtonType buttonTypeNo = new ButtonType("Nem", ButtonData.NO);
                    ButtonType buttonTypeCancel = new ButtonType("Mégsem", ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == buttonTypeYes) {
                        DataManager.getInstance().saveDailyBalances();
                    } else if (result.get() == buttonTypeCancel) {
                        event.consume();
                    }
                }
                ConfigManager.setProperty("DailyBalanceViewScroll", String.valueOf(controller.getDailyBalanceViewScroll()));
            } catch (IOException | JsonDeserializeException ex) {
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        if (ConfigManager.getBooleanProperty("LogPerformance")) StopWatch.start("App show");
        stage.show();
        if (ConfigManager.getBooleanProperty("LogPerformance")) StopWatch.stop("App show");
        
        if (ConfigManager.getBooleanProperty("LogPerformance")) StopWatch.stop("App start");
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
