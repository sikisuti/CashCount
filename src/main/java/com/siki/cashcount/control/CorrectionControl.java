/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.control;

import com.siki.cashcount.model.Correction;
import java.io.IOException;
import java.text.NumberFormat;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

/**
 *
 * @author tamas.siklosi
 */
public class CorrectionControl extends GridPane {
    
    @FXML private Text txtType;
    @FXML private Text txtAmount;
    
    private Correction correction;
    private final DailyBalanceControl parent;

    public CorrectionControl(Correction correction, DailyBalanceControl parent) {
        this.correction = correction;
        
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/control/CorrectionControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        
        this.parent = parent;
        
        setType(correction.getType());
        setAmount(NumberFormat.getCurrencyInstance().format(correction.getAmount()));
    }

    public String getType() { return typeProperty().get(); }
    public final void setType(String value) { typeProperty().set(value); }
    public StringProperty typeProperty() { return txtType.textProperty(); }

    public String getAmount() { return amountProperty().get(); }
    public final void setAmount(String value) { amountProperty().set(value); }
    public StringProperty amountProperty() { return txtAmount.textProperty(); }
    
    public void doRemove(ActionEvent event) {
        parent.removeCorrection(correction);
    }
}
