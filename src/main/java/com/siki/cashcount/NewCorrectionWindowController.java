/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount;

import com.siki.cashcount.control.DailyBalanceControl;
import com.siki.cashcount.data.DataManager;
import com.siki.cashcount.model.AccountTransaction;
import com.siki.cashcount.model.Correction;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

/**
 * FXML Controller class
 *
 * @author tamas.siklosi
 */
public class NewCorrectionWindowController implements Initializable {
    Correction correction;
    DailyBalanceControl dailyBalanceControl;
    
    private Stage dialogStage;
    private boolean okClicked = false;
    
    @FXML ComboBox<String> cbType;
    @FXML TextField tfAmount;
    @FXML TextField tfComment;
    @FXML TableView tblTransactions;

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tfAmount.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));
            
        cbType.setItems(DataManager.getInstance().getAllCategories());
    }    
    
    public void setContext(Correction correction, DailyBalanceControl dbControl) {
        cbType.setValue(correction.getType());
        tfAmount.setText(correction.getAmount().toString());
        tfComment.setText(correction.getComment());
        
        this.correction = correction;
        this.dailyBalanceControl = dbControl;
        
        prepareTable();
        try {
            tblTransactions.setItems(dbControl.getDailyBalance().getTransactions());
        } catch (Exception ex) {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public boolean isOkClicked() {
        return okClicked;
    }
    
    private void prepareTable() {     
        tblTransactions.setRowFactory( tv -> {
            TableRow<AccountTransaction> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    AccountTransaction rowData = row.getItem();
                    tfAmount.setText(rowData.getAmount().toString());
                }
            });
            return row ;
        });
        
        TableColumn<AccountTransaction, String> transactionTypeCol = new TableColumn<>("Forgalom típusa");
        transactionTypeCol.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
//        TableColumn<AccountTransaction, LocalDate> dateCol = new TableColumn<>("Könyvelési dátum");
//        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<AccountTransaction, Integer> amountCol = new TableColumn<>("Összeg");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
//        TableColumn<AccountTransaction, Integer> balanceCol = new TableColumn<>("Új könyvelt egyenleg");
//        balanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
//        TableColumn<AccountTransaction, String> accountNumberCol = new TableColumn<>("Ellenoldali számlaszám");
//        accountNumberCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        TableColumn<AccountTransaction, String> ownerCol = new TableColumn<>("Ellenoldali név");
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("owner"));
        TableColumn<AccountTransaction, String> commentCol = new TableColumn<>("Közlemény");
        commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
//        TableColumn<AccountTransaction, String> counterCol = new TableColumn<>("?");
//        counterCol.setCellValueFactory(new PropertyValueFactory<>("counter"));    
        tblTransactions.getColumns().setAll(transactionTypeCol, amountCol, ownerCol, commentCol);    
    }    
    
    @FXML
    protected void doSave(ActionEvent event) {
        correction.setAmount(Integer.parseInt(tfAmount.getText().replace(",", "")));
        correction.setType(cbType.getValue());
        correction.setComment(tfComment.getText());
        
        okClicked = true;
        
//        try {
//            if (!DataManager.getInstance().getAllCorrectionType().contains(cbType.getValue())) {
//                DataManager.getInstance().getAllCorrectionType().add(cbType.getValue());
//                DataManager.getInstance().saveCorrectionTypes();
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(NewCorrectionWindowController.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
        dialogStage.close();
    }
    
    @FXML
    protected void doCancel(ActionEvent event) {
        dialogStage.close();
    }
    
    @FXML
    protected void doRemove(ActionEvent event) {
        dailyBalanceControl.removeCorrection(correction);
        dialogStage.close();
    }
}
