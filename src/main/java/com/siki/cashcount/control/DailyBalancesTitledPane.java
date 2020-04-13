/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.control;

import com.siki.cashcount.model.AccountTransaction;
import com.siki.cashcount.model.DailyBalance;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class DailyBalancesTitledPane extends TitledPane {
    private LocalDate period;
    private ObservableList<DailyBalance> dailyBalances = FXCollections.observableArrayList();
    private VBox vbDailyBalances = new VBox();
    
    public LocalDate getPeriod() { return period; }

    public void addDailyBalance(DailyBalance db) {
        dailyBalances.add(db);
        if (isAroundToday(period))
            vbDailyBalances.getChildren().add(new DailyBalanceControl(db, this));
    }

    public List<DailyBalance> getDailyBalances() {
        return dailyBalances;
    }
    
    public DailyBalancesTitledPane(LocalDate period) {
        super(period.format(DateTimeFormatter.ofPattern("uuuu. MMMM")), new GridPane());
        GridPane gpRoot = (GridPane)this.getContent();
        
        GridPane.setColumnIndex(vbDailyBalances, 0);
        
        gpRoot.getChildren().addAll(vbDailyBalances/*, gpStatisticsBg*/);
        
        this.period = period.withDayOfMonth(1);
        this.setExpanded(isAroundToday(period));
        
        this.expandedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                        if (newValue && vbDailyBalances.getChildren().isEmpty()) {
                            for (DailyBalance db : dailyBalances) {
                                vbDailyBalances.getChildren().add(new DailyBalanceControl(db, this));
                            }
                        }
                    });
    }
    
    private boolean isAroundToday(LocalDate date) {
        return date.isAfter(LocalDate.now().minusMonths(1).withDayOfMonth(LocalDate.now().minusMonths(1).lengthOfMonth())) &&
                date.isBefore(LocalDate.now().plusMonths(1).withDayOfMonth(1));
    }

    public void validate() {
        this.isValid();
    }
    
    private void isValid() {
        boolean isValid = dailyBalances.stream().allMatch(DailyBalance::isValid);

        if (!isValid) {
            this.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        }
    }
}
