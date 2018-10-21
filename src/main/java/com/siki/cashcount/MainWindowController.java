package com.siki.cashcount;

import com.siki.cashcount.config.ConfigManager;
import com.siki.cashcount.constant.CashFlowSeriesEnum;
import com.siki.cashcount.control.*;
import com.siki.cashcount.data.DataManager;
import com.siki.cashcount.exception.*;
import com.siki.cashcount.helper.StopWatch;
import com.siki.cashcount.model.*;
import java.io.*;
import java.net.URL;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
//import java.util.logging.*;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.event.*;
import javafx.fxml.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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

    private CashFlowChart flowChart = new CashFlowChart();

    private LineChart.Series<Date, Integer> savingSeries = new LineChart.Series<>();
    private LineChart.Series<Date, Integer> cashSeries = new LineChart.Series<>();
    private LineChart.Series<Date, Integer> accountSeries = new LineChart.Series<>();

    private LineChart.Series<Date, Integer> savingSeriesRef = new LineChart.Series<>();
    private LineChart.Series<Date, Integer> cashSeriesRef = new LineChart.Series<>();
    private LineChart.Series<Date, Integer> accountSeriesRef = new LineChart.Series<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        // Chart layout
        vbCashFlow.getChildren().add(flowChart);
        savingSeries.setName("Lekötések");
        cashSeries.setName("Készpénz");
        accountSeries.setName("Számla");  
        flowChart.getData().addAll(savingSeriesRef, cashSeriesRef, accountSeriesRef, savingSeries, cashSeries, accountSeries);
        
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
                if (date == null || !db.getDate().getMonth().equals(date.getMonth())) {
                    if (tp != null) {
                        tp.validate();
                        tp.refreshStatistics();
                    }

                    date = db.getDate();
                    tp = new DailyBalancesTitledPane(date);
                    if (i != 0)
                        tp.setLastMonthEndBalance(DataManager.getInstance().getAllDailyBalances().get(i - 1).getTotalMoney());
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
            try {
                ObservableList<DailyBalance> series = DataManager.getInstance().getAllDailyBalances();
                refreshChart(series);
            } catch (IOException | JsonDeserializeException ex) {
                LOGGER.error("", ex);
                ExceptionDialog.get(ex).showAndWait();
            }
        }
    }
    
    private void refreshChart(ObservableList<DailyBalance> series) {
        savingSeries.getData().clear();
        cashSeries.getData().clear();
        accountSeries.getData().clear();
        savingSeriesRef.getData().clear();
        cashSeriesRef.getData().clear();
        accountSeriesRef.getData().clear();
                
        try {
            
            series.forEach(db -> {
                Date date = DateHelper.toDate(db.getDate());
                Integer yValue = db.getTotalSavings();
                savingSeries.getData().add(new XYChart.Data(date, yValue));
                savingSeriesRef.getData().add(new XYChart.Data(date, yValue));
                yValue = yValue + db.getCash();
                cashSeries.getData().add(new XYChart.Data(date, yValue));
                cashSeriesRef.getData().add(new XYChart.Data(date, yValue));
                yValue = yValue + db.getBalance();
                accountSeries.getData().add(new XYChart.Data(date, yValue));  
                accountSeriesRef.getData().add(new XYChart.Data(date, yValue));                       
            });
            
            int max = accountSeries.getData().stream().mapToInt(s -> s.getYValue()).max().getAsInt();
            int min = series.stream().filter(s -> s.isPredicted()).mapToInt(s -> s.getTotalSavings() + s.getTotalMoney()).min().getAsInt();
            
            flowChart.getYAxis().setUpperBound(Math.ceil(max / 100000d) * 100000);
            flowChart.getYAxis().setLowerBound(Math.floor((min - 350000) / 100000d) * 100000);
        
            flowChart.getXAxis().setLowerBound(Date.from(DataManager.getInstance().getAllDailyBalances().get(0).getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            flowChart.getXAxis().setUpperBound(Date.from(DataManager.getInstance().getLastDailyBalance().getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        
            
        } catch (IOException | JsonDeserializeException ex) {
            LOGGER.error("", ex);
            ExceptionDialog.get(ex).showAndWait();
        }
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
                            .setBalance(Integer.valueOf(elements[6]))
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
            refreshStatistics();
        }        
    }
    private void refreshStatistics() {
        try {
            vbStatistics.getChildren().clear();
            
            TreeMap<LocalDate, TreeMap<String, Entry<Integer, String>>> data = new TreeMap<>();
            List<String> keys = new ArrayList<>();
                        
            for (Node node : dailyBalancesPH.getChildren()) {
                if (node.getClass() != DailyBalancesTitledPane.class) continue;
                DailyBalancesTitledPane entry = (DailyBalancesTitledPane)node;
                
                TreeMap<String, Entry<Integer, String>> monthCorrectionData = DataManager.getInstance().getStatisticsFromCorrections(entry.getPeriod().getYear(), entry.getPeriod().getMonth());
                TreeMap<String, Entry<Integer, String>> monthTransactionData = DataManager.getInstance().getStatisticsFromTransactions(entry.getPeriod().getYear(), entry.getPeriod().getMonth());
                Integer allTransactionAmount = 0;
                for (String key : monthTransactionData.keySet()) {
                    allTransactionAmount += monthTransactionData.get(key).getKey();
                }
                if (monthCorrectionData.containsKey(DataManager.GENERAL_TEXT) && LocalDate.of(entry.getPeriod().getYear(), entry.getPeriod().getMonthValue(), 1).isBefore(LocalDate.now().withDayOfMonth(1))) {
                    Integer cashSpent = monthCorrectionData.get(DataManager.GENERAL_TEXT).getKey() - allTransactionAmount;
                    if (cashSpent != 0)
                        monthTransactionData.put("  -- Készpénzköltés", new AbstractMap.SimpleEntry<>(cashSpent, "Költés készpénzből"));
                }
                monthCorrectionData.putAll(monthTransactionData);
                data.put(entry.getPeriod(), monthCorrectionData);
                for (String key : monthCorrectionData.keySet()) {
                    if (!keys.contains(key)) {
                        keys.add(key);
                    }
                }
            }
            
            GridPane gpStatFromCorrections = new GridPane();
            gpStatFromCorrections.setPadding(new Insets(20, 10, 20, 10));
            buildStatGrid(gpStatFromCorrections, data, keys);
            
            vbStatistics.getChildren().addAll(gpStatFromCorrections);
        } catch (Exception ex) {
            LOGGER.error("", ex);
        }
    }
    
    private void buildStatGrid( GridPane grid, TreeMap<LocalDate, TreeMap<String, Entry<Integer, String>>> data, List<String> keys) {
        int colCnt = 0;
        
        TreeMap<String, Integer> averages = calculateAverages(data);
        
        for (LocalDate date : data.keySet()) {
            colCnt++;

            GridPane headerBg = new GridPane();
            headerBg.setPrefSize(100, 30);
            headerBg.setAlignment(Pos.CENTER);
            Label colHeader = new Label(date.getYear() + "." + date.getMonthValue() + ".");
            colHeader.setStyle("-fx-font-weight: bold;");
            if (date.isEqual(LocalDate.now().withDayOfMonth(1))) {
                headerBg.setBorder(new Border(new BorderStroke(Color.BLACK, Color.GRAY, Color.TRANSPARENT, Color.BLACK, 
                        BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, 
                        CornerRadii.EMPTY, new BorderWidths(1, 2, 0, 1), Insets.EMPTY)));
                headerBg.setAlignment(Pos.TOP_CENTER);                        
                headerBg.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
            }

            headerBg.getChildren().add(colHeader);
            GridPane.setColumnIndex(headerBg, colCnt);
            GridPane.setRowIndex(headerBg, 0);
            grid.getChildren().add(headerBg);

            for (String category : keys) {
                int rowNo = 0;
                try {
                    rowNo = ConfigManager.getIntegerProperty(category);
                } catch (NumberFormatException ex) {
                    continue;
                }
                if (colCnt == 1) {
                    Label rowHeader = new Label(category);
                    rowHeader.setMinWidth(150);
                    rowHeader.setPrefWidth(150);
                    rowHeader.setMaxWidth(150);
                    if (!category.startsWith("  -- ")) rowHeader.setStyle("-fx-font-weight: bold;");
                    GridPane.setColumnIndex(rowHeader, colCnt - 1);
                    GridPane.setRowIndex(rowHeader, rowNo);
                    grid.getChildren().add(rowHeader);
                }

                GridPane cell = new GridPane();
                cell.setPrefSize(100, 30);
                cell.setAlignment(Pos.CENTER_RIGHT);
                Integer value;
                Label lblValue;
                if (data.get(date).containsKey(category)) {
                    value = data.get(date).get(category).getKey();
                    lblValue = new Label(NumberFormat.getCurrencyInstance().format(value));
                    Tooltip tt = new Tooltip(data.get(date).get(category).getValue());
                    lblValue.setTooltip(tt);
                } else {
                    value = 0;
                    lblValue = new Label();
                }
                if (date.isEqual(LocalDate.now().withDayOfMonth(1))) {
                    cell.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, Color.GRAY, Color.TRANSPARENT, Color.BLACK, 
                            BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, 
                            CornerRadii.EMPTY, new BorderWidths(0, 2, 0, 1), Insets.EMPTY)));
                    cell.setAlignment(Pos.TOP_RIGHT);                        
                    lblValue.setStyle("-fx-font-weight: bold;");
                }
                cell.getChildren().add(lblValue);
                double opacity;
                Color bgColor;
                // Instead of considering the diff between value and 0 I consider the diff between value and average
                /*
                double inLowerBound = ConfigManager.getDoubleProperty("IncomeDecoratorLowerBound");
                double inUpperBound = ConfigManager.getDoubleProperty("IncomeDecoratorUpperBound");
                double outLowerBound = ConfigManager.getDoubleProperty("OutcomeDecoratorLowerBound");
                double outUpperBound = ConfigManager.getDoubleProperty("OutcomeDecoratorUpperBound");
                */
                double diffBound = ConfigManager.getDoubleProperty("DifferenceDecoratorBound");
                
                Integer avgValue = averages.containsKey(category) ? averages.get(category) : 0;
                Integer diff = value - avgValue;
                opacity = Math.abs(diff / diffBound);
                if (opacity > 1) { opacity = 1; }
                bgColor = diff > 0 ? Color.rgb(0, 200, 0, opacity) : Color.rgb(230, 0, 0, opacity);
                
                /*
                if (value < 0) {
                    opacity = ((value + outUpperBound) <= 0 ? (value + outUpperBound) : 0) / (outLowerBound + outUpperBound);
                    if (opacity > 1d) opacity = 1;
                    bgColor = Color.rgb(230, 0, 0, opacity);
                } else {
                    opacity = ((value - inLowerBound) >= 0 ? (value - inLowerBound) : 0) / (inUpperBound - inLowerBound);
                    if (opacity > 1d) opacity = 1;
                    bgColor = Color.rgb(0, 200, 0, opacity);
                }
*/
                cell.setBackground(new Background(new BackgroundFill(bgColor, CornerRadii.EMPTY, Insets.EMPTY)));
                GridPane.setColumnIndex(cell, colCnt);
                GridPane.setRowIndex(cell, rowNo);
                grid.getChildren().add(cell);
            }
        }
    }
    
    private TreeMap<String, Integer> calculateAverages(TreeMap<LocalDate, TreeMap<String, Entry<Integer, String>>> data) {
        
        TreeMap<String, Integer> averages = new TreeMap<>();
            
        for (LocalDate date : data.keySet().stream().filter(d -> d.isBefore(LocalDate.now().withDayOfMonth(1))).collect(Collectors.toList())) {
            for (String category : data.get(date).keySet()){
                if (!averages.containsKey(category)) { averages.put(category, 0); }
                
                averages.replace(category, averages.get(category) + data.get(date).get(category).getKey());
            }
        }
        
        Integer divider = Math.toIntExact(data.keySet().stream().filter(d -> d.isBefore(LocalDate.now().withDayOfMonth(1))).count());
        for (String category : averages.keySet()){
            averages.replace(category, averages.get(category) / divider);
        }
        
        return averages;
    }
}
