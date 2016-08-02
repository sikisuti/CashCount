/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount;

import com.siki.cashcount.data.DataManager;
import com.siki.cashcount.model.Correction;
import com.siki.cashcount.model.DailyBalance;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

/**
 * FXML Controller class
 *
 * @author tamas.siklosi
 */
public class NewCorrectionWindowController implements Initializable {
    Correction correction;
    
    private Stage dialogStage;
    private boolean okClicked = false;
    
    @FXML ComboBox<String> cbType;
    @FXML TextField tfAmount;
    @FXML TextField tfComment;

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tfAmount.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));
            
        try {
            cbType.getItems().addAll(DataManager.getInstance().getAllCorrectionType());
        } catch (IOException ex) {
            Logger.getLogger(NewCorrectionWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
    public void setContext(Correction correction) {
        cbType.setValue(correction.getType());
        tfAmount.setText(correction.getAmount().toString());
        tfComment.setText(correction.getComment());
        
        this.correction = correction;
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public boolean isOkClicked() {
        return okClicked;
    }
    
    @FXML
    protected void doSave(ActionEvent event) {
        correction.setAmount(Integer.parseInt(tfAmount.getText().replace(",", "")));
        correction.setType(cbType.getValue());
        correction.setComment(tfComment.getText());
        
        okClicked = true;
        
        try {
            if (!DataManager.getInstance().getAllCorrectionType().contains(cbType.getValue())) {
                DataManager.getInstance().getAllCorrectionType().add(cbType.getValue());
                DataManager.getInstance().saveCorrectionTypes();
            }
        } catch (IOException ex) {
            Logger.getLogger(NewCorrectionWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        dialogStage.close();
    }
    
    @FXML
    protected void doCancel(ActionEvent event) {
        dialogStage.close();
    }
}
