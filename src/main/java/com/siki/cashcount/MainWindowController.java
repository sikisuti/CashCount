package com.siki.cashcount;

import com.siki.cashcount.config.ConfigManager;
import com.siki.cashcount.control.DailyBalanceControl;
import com.siki.cashcount.control.DailyBalancesTitledPane;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class MainWindowController implements Initializable {

//    @FXML TableView SourceTable;
    @FXML BorderPane PageFrame;
    @FXML LineChart FlowChart;
    @FXML VBox DailyBalancesPH;
    @FXML NumberAxis yAxis;
    @FXML ScrollPane DailyBalancesSP;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
            
//        prepareTable();        
        prepareChart();
        prepareDailyBalances();
        DailyBalancesSP.setVvalue(ConfigManager.getDoubleProperty("DailyBalanceViewScroll"));
    }
    
    private void prepareChart() {
        if (ConfigManager.getBooleanProperty("LogPerformance")) StopWatch.start("prepareChart");
        FlowChart.setTitle("Cash Flow");
        yAxis.setTickUnit(100000);
        
        FlowChart.getData().clear();
                
        try {
            ObservableList<DailyBalance> series = DataManager.getInstance().getAllDailyBalances();
        
            LineChart.Series<Date, Integer> savingSeries = new LineChart.Series<>();
            LineChart.Series<Date, Integer> cashSeries = new LineChart.Series<>();
            LineChart.Series<Date, Integer> accountSeries = new LineChart.Series<>();
            savingSeries.setName("Lekötések");
            cashSeries.setName("Készpénz");
            accountSeries.setName("Számla");
            series.stream().forEach((db) -> {
                Date date = Date.from(db.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
                Integer yValue = db.getTotalSavings();
                savingSeries.getData().add(new XYChart.Data(date, yValue));
                yValue = yValue + db.getCash();
                cashSeries.getData().add(new XYChart.Data(date, yValue));
                yValue = yValue + db.getBalance();
                accountSeries.getData().add(new XYChart.Data(date, yValue));
            });
            
            int max = accountSeries.getData().stream().mapToInt(s -> s.getYValue()).max().getAsInt();
            int min = series.stream().filter(s -> s.isPredicted()).mapToInt(s -> s.getTotalSavings() + s.getTotalMoney()).min().getAsInt();
            
            yAxis.setUpperBound(Math.ceil(max / 100000d) * 100000);
            yAxis.setLowerBound(Math.floor((min - 350000) / 100000d) * 100000);
            
            FlowChart.getData().addAll(savingSeries, cashSeries, accountSeries);
            
        } catch (IOException | JsonDeserializeException ex) {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (ConfigManager.getBooleanProperty("LogPerformance")) StopWatch.stop("prepareChart");
    }
    
    private void prepareDailyBalances() {
        if (ConfigManager.getBooleanProperty("LogPerformance")) StopWatch.start("prepareDailyBalances");
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
            prepareChart();
        }
    }
}
