/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.control;

import com.siki.cashcount.MainWindowController;
import com.siki.cashcount.data.DataManager;
import com.siki.cashcount.exception.JsonDeserializeException;
import com.siki.cashcount.model.DailyBalance;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author tamas.siklosi
 */
public class DailyBalancesTitledPane extends TitledPane {
    private LocalDate period;

    public void addDailyBalance(DailyBalance db) {
        ((VBox)this.getContent()).getChildren().add(new DailyBalanceControl(db));
    }
    
    public DailyBalancesTitledPane(LocalDate period) {
        super(period.format(DateTimeFormatter.ofPattern("uuuu. MMMM")), new VBox());
        this.period = period.withDayOfMonth(1);
        
        this.expandedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                        if (newValue) {
                            if (((VBox)this.getContent()).getChildren().isEmpty()) {
                                try {
                                    for (DailyBalance db : DataManager.getInstance().getAllDailyBalances().stream()
                                            .filter(f -> f.getDate().withDayOfMonth(1).isEqual(this.period)).collect(Collectors.toList())) {
                                        addDailyBalance(db);
                                    }
                                } catch (IOException | JsonDeserializeException ex) {
                                    Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    });
    }
}
