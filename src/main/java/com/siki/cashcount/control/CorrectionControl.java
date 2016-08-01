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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

/**
 *
 * @author tamas.siklosi
 */
public class CorrectionControl extends GridPane {
    
    @FXML private Text txtType;
    @FXML private Text txtAmount;
    @FXML private Button btnRemove;
    
    private Correction correction;
    private final DailyBalanceControl parent;
    
    public static final DataFormat CorrectionDataFormat = new DataFormat("com.siki.cashcount.model.Correction");

    public CorrectionControl(Correction correction, DailyBalanceControl parent) {
        this.correction = correction;
        
        this.setOnDragDetected((MouseEvent event) -> {
            /* drag was detected, start a drag-and-drop gesture*/
            /* allow any transfer mode */
            Dragboard db = this.startDragAndDrop(TransferMode.ANY);

            /* Put a string on a dragboard */
            ClipboardContent content = new ClipboardContent();
            content.put(CorrectionDataFormat, this.correction);
            db.setContent(content);

            event.consume();
        });
        this.setOnDragDone((DragEvent event) -> {
            /* the drag and drop gesture ended */
            /* if the data was successfully moved, clear it */
            if (event.getTransferMode() == TransferMode.MOVE) {
                System.out.println("Todo: remove dropped element");
            }
            event.consume();
        });
        
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
    
    @FXML
    private void mouseEntered(MouseEvent event) {
        btnRemove.setVisible(true);
    }
    
    @FXML
    private void mouseExited(MouseEvent event) {
        btnRemove.setVisible(false);
    }
}
