package com.siki.cashcount;

import com.siki.cashcount.config.ConfigManager;
import com.siki.cashcount.constant.CashFlowSeries;
import com.siki.cashcount.control.CashFlowChart;
import com.siki.cashcount.control.DailyBalancesTitledPane;
import com.siki.cashcount.control.DateHelper;
import com.siki.cashcount.data.DataManager;
import com.siki.cashcount.exception.JsonDeserializeException;
import com.siki.cashcount.exception.NotEnoughPastDataException;
import com.siki.cashcount.exception.TransactionGapException;
import com.siki.cashcount.helper.StopWatch;
import com.siki.cashcount.model.*;
import java.io.*;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class MainWindowController implements Initializable {

    @FXML BorderPane PageFrame;
    @FXML VBox DailyBalancesPH;
    @FXML ScrollPane DailyBalancesSP;
    @FXML VBox vbCashFlow;
    
    Slider slider = new Slider();
    CashFlowChart flowChart = new CashFlowChart();
    Button btnGetPast = new Button("Időgép");
        
    LineChart.Series<Date, Integer> savingSeries = new LineChart.Series<>();
    LineChart.Series<Date, Integer> cashSeries = new LineChart.Series<>();
    LineChart.Series<Date, Integer> accountSeries = new LineChart.Series<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
            
//        prepareTable();   

        // Chart layout
        vbCashFlow.getChildren().add(flowChart);
        btnGetPast.setOnAction((ActionEvent event) -> { getPast(); });
        vbCashFlow.getChildren().add(btnGetPast);
        savingSeries.setName("Lekötések");
        cashSeries.setName("Készpénz");
        accountSeries.setName("Számla");        
        flowChart.getData().addAll(savingSeries, cashSeries, accountSeries);        
        
        prepareDailyBalances();
        DailyBalancesSP.setVvalue(ConfigManager.getDoubleProperty("DailyBalanceViewScroll"));
    }
    
    public void refreshChart(ObservableList<DailyBalance> series) {
        savingSeries.getData().clear();
        cashSeries.getData().clear();
        accountSeries.getData().clear();
                
        try {
            
            series.stream().forEach((db) -> {
                Date date = DateHelper.toDate(db.getDate());
                Integer yValue = db.getTotalSavings();
                savingSeries.getData().add(new XYChart.Data(date, yValue));
                yValue = yValue + db.getCash();
                cashSeries.getData().add(new XYChart.Data(date, yValue));
                yValue = yValue + db.getBalance();
                accountSeries.getData().add(new XYChart.Data(date, yValue));                        
            });
            
            int max = accountSeries.getData().stream().mapToInt(s -> s.getYValue()).max().getAsInt();
            int min = series.stream().filter(s -> s.isPredicted()).mapToInt(s -> s.getTotalSavings() + s.getTotalMoney()).min().getAsInt();
            
            flowChart.getYAxis().setUpperBound(Math.ceil(max / 100000d) * 100000);
            flowChart.getYAxis().setLowerBound(Math.floor((min - 350000) / 100000d) * 100000);
        
            flowChart.getXAxis().setLowerBound(Date.from(DataManager.getInstance().getAllDailyBalances().get(0).getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            flowChart.getXAxis().setUpperBound(Date.from(DataManager.getInstance().getLastDailyBalance().getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        
            
        } catch (IOException | JsonDeserializeException ex) {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void refreshChart(HashMap<CashFlowSeries, ObservableList<XYChart.Data<Date, Integer>>> series) {
        if (series != null ) {        
            if (savingSeries.getData() != series.get(CashFlowSeries.SAVING))
                savingSeries.setData(series.get(CashFlowSeries.SAVING));
            if (cashSeries.getData() != series.get(CashFlowSeries.CASH))
                cashSeries.setData(series.get(CashFlowSeries.CASH));
            if (accountSeries.getData() != series.get(CashFlowSeries.ACCOUNT))
                accountSeries.setData(series.get(CashFlowSeries.ACCOUNT));
        }
    }
    
    private void prepareDailyBalances() {
        if (ConfigManager.getBooleanProperty("LogPerformance")) StopWatch.start("prepareDailyBalances");
        DailyBalancesPH.getChildren().clear();
        try {
            LocalDate date = null;
            DailyBalancesTitledPane tp = null;
            for (DailyBalance db : DataManager.getInstance().getAllDailyBalances()) {
                if (date == null || !db.getDate().getMonth().equals(date.getMonth())) {
                    date = db.getDate();
                    tp = new DailyBalancesTitledPane(date);
                    tp.setExpanded(isAroundToday(date));
                    DailyBalancesPH.getChildren().add(tp);  
                }
                if (isAroundToday(db.getDate()))
                    tp.addDailyBalance(db);
            }
            Button btnNewMonth = new Button("+");
            btnNewMonth.setStyle("-fx-alignment: center;");
            btnNewMonth.setOnAction((ActionEvent event) -> {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setHeaderText("Kibővíted a kalkulációt egy hónappal?");
                Optional<ButtonType> result = alert.showAndWait();
                
                if (result.get() == ButtonType.OK) {
                    try {
                        DataManager.getInstance().addOneMonth();
                        prepareDailyBalances();
                    } catch (IOException | JsonDeserializeException | NotEnoughPastDataException ex) {
                        Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            DailyBalancesPH.getChildren().add(btnNewMonth);
        } catch (JsonDeserializeException ex) {
            System.out.println("Error in line: " + ex.getErrorLineNum());
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }catch (IOException ex) {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        } 
        if (ConfigManager.getBooleanProperty("LogPerformance")) StopWatch.stop("prepareDailyBalances");
    }
    
    private boolean isAroundToday(LocalDate date) {
        return date.isAfter(LocalDate.now().minusMonths(1).withDayOfMonth(LocalDate.now().minusMonths(1).lengthOfMonth())) &&
                date.isBefore(LocalDate.now().plusMonths(1).withDayOfMonth(1));
    }
    
    public double getDailyBalanceViewScroll() {
        return DailyBalancesSP.getVvalue();
    }
    
    @FXML
    private void doImport(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Válaszd ki a fájlt");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("csv files", "*.csv"), new ExtensionFilter("Minden fájl", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(PageFrame.getScene().getWindow());
        if (selectedFile != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(selectedFile), "UTF-8"))) {
                String line;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                List<AccountTransaction> newTransactions = new ArrayList<>();

                while ((line = br.readLine()) != null) {      
                    line = line.replace("\"", "");
                    String[] elements = line.split(";");

                    AccountTransaction newTransaction = new AccountTransaction.Builder()
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
                Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }
    
    @FXML
    private void refreshDailyBalances(ActionEvent event) throws IOException {
        try {
            DataManager.getInstance().calculatePredictions();
        } catch (NotEnoughPastDataException ex) {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
            
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
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void refreshChart(Event event) {
        if (((Tab)(event.getSource())).isSelected()) {
            try {
                ObservableList<DailyBalance> series = DataManager.getInstance().getAllDailyBalances();
                refreshChart(series);
            } catch (IOException | JsonDeserializeException ex) {
                Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private long cnt = 0;
    
    private void getPast() {
        try {
            vbCashFlow.getChildren().remove(btnGetPast);
            
            GridPane gp = new GridPane();
            gp.setHgrow(slider, Priority.ALWAYS);
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(41);
            ColumnConstraints col2 = new ColumnConstraints();
            gp.getColumnConstraints().addAll(col1, col2);
            slider.setPadding(new Insets(0, 0, 0, 110));
            
            slider.setMin(DAYS.between(LocalDate.now(), DataManager.getInstance().getFirstDailyBalance().getDate()));
            slider.setMax(0);
            slider.setMajorTickUnit(1);
            //slider.setShowTickMarks(true);
            slider.setSnapToTicks(true);
            
            slider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                try {
                    if (oldValue.longValue() - newValue.longValue() != 0) {
                        HashMap<CashFlowSeries, ObservableList<XYChart.Data<Date, Integer>>> data = DataManager.getInstance().getPastSeries(LocalDate.now().plusDays(newValue.longValue()));
                        flowChart.getPastLine().setXValue(DateHelper.toDate(LocalDate.now().plusDays(newValue.longValue())));
                        refreshChart(data);
                    }
                } catch (IOException | JsonDeserializeException ex) {
                    Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            gp.setConstraints(slider, 0, 0);
            gp.getChildren().add(slider);
            vbCashFlow.getChildren().add(gp);            
        } catch (IOException | JsonDeserializeException ex) {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
