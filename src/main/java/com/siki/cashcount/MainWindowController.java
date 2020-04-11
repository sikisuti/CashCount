package com.siki.cashcount;

import com.siki.cashcount.chart.CashFlowChart;
import com.siki.cashcount.chart.ChartController;
import com.siki.cashcount.config.ConfigManager;
import com.siki.cashcount.control.*;
import com.siki.cashcount.data.DataManager;
import com.siki.cashcount.exception.*;
import com.siki.cashcount.helper.StopWatch;
import com.siki.cashcount.model.*;
import java.io.*;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.siki.cashcount.statistics.StatisticsController;
import com.siki.cashcount.statistics.StatisticsMonthModel;
import com.siki.cashcount.statistics.StatisticsViewBuilder;

import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.stage.FileChooser.ExtensionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainWindowController implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainWindowController.class);

    @FXML BorderPane pageFrame;
    @FXML VBox dailyBalancesPH;
    @FXML ScrollPane dailyBalancesSP;
    @FXML VBox vbCashFlow;
    @FXML VBox vbStatistics;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        vbCashFlow.getChildren().add(ChartController.flowChart);
        prepareDailyBalances();
    }

    private void prepareDailyBalances() {
        if (ConfigManager.getBooleanProperty("LogPerformance")) StopWatch.start("prepareDailyBalances");
        dailyBalancesPH.getChildren().clear();
        try {
            LocalDate date = null;
            DailyBalancesTitledPane tp = null;
            // 1. create DailyBalancesTitledPane
            // 2. add DailyBalancesTitledPne to the list
            // 3. add all DailyBalances to the DailyBalancesTitledPane
            // 4. validate DailyBalancesTitledPane
            for (int i = 0; i < DataManager.getInstance().getAllDailyBalances().size(); i++) {
                DailyBalance db = DataManager.getInstance().getAllDailyBalances().get(i);
                if (db.getDate().plusYears(1).isBefore(LocalDate.now().withDayOfMonth(1))) {
                    continue;
                }

                if (date == null || !db.getDate().getMonth().equals(date.getMonth())) {
                    if (tp != null) {
                        tp.validate();
                    }

                    date = db.getDate();
                    tp = new DailyBalancesTitledPane(date);
                    dailyBalancesPH.getChildren().add(tp);
                }

                tp.addDailyBalance(db);
            }

            Button btnNewMonth = new Button("+");
            btnNewMonth.setStyle("-fx-alignment: center;");
            btnNewMonth.setOnAction((ActionEvent event) -> {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setHeaderText("Kibővíted a kalkulációt egy hónappal?");
                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try {
                        DataManager.getInstance().addOneMonth();
                        prepareDailyBalances();
                    } catch (IOException | JsonDeserializeException | NotEnoughPastDataException ex) {
                        LOGGER.error("", ex);
                    }
                }
            });
            dailyBalancesPH.getChildren().add(btnNewMonth);
        } catch (JsonDeserializeException ex) {
            LOGGER.error("Error in line: " + ex.getErrorLineNum(), ex);
            ExceptionDialog.get(ex).showAndWait();
        } catch (Exception ex) {
            LOGGER.error("", ex);
            ExceptionDialog.get(ex).showAndWait();

        }
        if (ConfigManager.getBooleanProperty("LogPerformance")) StopWatch.stop("prepareDailyBalances");
    }

    @FXML
    private void refreshChart(Event event) {
        if (((Tab)(event.getSource())).isSelected()) {
            ChartController.refreshChart();
        }
    }

    @FXML
    private void scrollChart(Event event) {
        ScrollEvent scrollEvent = (ScrollEvent) event;
        ChartController.scroll(scrollEvent.getDeltaY() * 10000);
    }
    
    @FXML
    private void doImport(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Válaszd ki a fájlt");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("csv files", "*.csv"), new ExtensionFilter("Minden fájl", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(pageFrame.getScene().getWindow());
        if (selectedFile != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(selectedFile), "UTF-8"))) {
                String line;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                List<AccountTransaction> newTransactions = new ArrayList<>();
                long actId = DataManager.getInstance().getNextTransactionId();

                while ((line = br.readLine()) != null) {      
                    line = line.replace("\"", "");
                    String[] elements = line.split(";");

                    AccountTransaction newTransaction = new AccountTransaction.Builder()
                            .setId(actId++)
                            .setAmount(Integer.valueOf(elements[2]))
                            .setDate(LocalDate.parse(elements[4], formatter))
                            .setAccountNumber(elements[7])
                            .setOwner(elements[8])
                            .setComment(elements[9] + " " + elements[11])
                            .setCounter("")
                            .setTransactionType(elements[12])
                            .build();
                    
                    newTransactions.add(newTransaction);
                }
                
                Integer importedRows = 0;
                boolean force = false;
                do {
                    try {
                        importedRows = DataManager.getInstance().saveTransactions(newTransactions, force);
                        force = false;
                    } catch (TransactionGapException ex) {
                        Alert alert = new Alert(AlertType.CONFIRMATION);
                        alert.setTitle("Adathiány");
                        alert.setHeaderText("Adat hiányzik");
                        StringBuilder content = new StringBuilder();
                        ex.missingDates.stream().forEach((d) -> {
                            content.append(d.toString()).append(" (").append(d.getDayOfWeek().name()).append(")\n");
                        });
                        content.append("\nMégis betöltsem a fájlt?");
                        alert.setContentText(content.toString());
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == ButtonType.OK) force = true;
                    }
                } while (force);
                
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Üzenet");
                alert.setHeaderText("Importálás kész");
                alert.setContentText(importedRows + " új tranzakció importálva");
                alert.showAndWait();
                
            } catch (Exception e) {
                LOGGER.error("", e);
                ExceptionDialog.get(e).showAndWait();
            }
        }
    }
    
    @FXML
    private void refreshDailyBalances(ActionEvent event) throws IOException {
        try {
            DataManager.getInstance().calculatePredictions();
        } catch (NotEnoughPastDataException ex) {
            LOGGER.error("", ex);
            
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Hiba");
            alert.setHeaderText("Számítás nem lehetséges.");
            alert.setContentText("Nincs elég adat a múltban.");
            alert.showAndWait();
        }
    }
    
    @FXML
    private void doSave(ActionEvent event) {
        try {
            DataManager.getInstance().saveDailyBalances();
        } catch (IOException ex) {
            LOGGER.error("", ex);
            ExceptionDialog.get(ex).showAndWait();
        }
    }
    
    @FXML
    private void categories(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/CategoryManagerWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.initOwner(vbCashFlow.getScene().getWindow());
            stage.initModality(Modality.NONE);
            stage.setAlwaysOnTop(true);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Kategóriák");
            stage.setScene(new Scene(root1));
            stage.show();
        } catch (IOException ex) {
            LOGGER.error("", ex);
            ExceptionDialog.get(ex).showAndWait();
        }
    }
    
    @FXML
    private void loadPredictedCorrections(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Válaszd ki a korrekciós fájlt");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("json files", "*.jsn"), new ExtensionFilter("Minden fájl", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(pageFrame.getScene().getWindow());
        if (selectedFile != null) {
            List<PredictedCorrection> pcList;
            try {
                pcList = DataManager.getInstance().loadPredictedCorrection(selectedFile.getAbsolutePath());
                DataManager.getInstance().clearPredictedCorrections();
                DataManager.getInstance().fillPredictedCorrections(pcList);
                prepareDailyBalances();
            } catch (IOException | JsonDeserializeException | NotEnoughPastDataException ex) {
                LOGGER.error("", ex);
            }
            
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Üzenet");
            alert.setHeaderText("Végrehajtva");
            alert.setContentText("Korrekciók betöltve");
            alert.showAndWait();
        }
    }
    
    @FXML
    private void refreshStatistics(Event event) {
        if (((Tab)(event.getSource())).isSelected()) {
            try {
            	vbStatistics.getChildren().clear();
            	SortedMap<LocalDate, StatisticsMonthModel> statistics = new StatisticsController().getStatistics();
            	Node statisticsView = new StatisticsViewBuilder().getStatisticsView(statistics);
            	vbStatistics.getChildren().addAll(statisticsView);            	
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }        
    }
}
