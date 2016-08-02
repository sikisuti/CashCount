/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.control;

import com.siki.cashcount.NewCorrectionWindowController;
import com.siki.cashcount.converter.IntegerToTextConverter;
import com.siki.cashcount.data.DataManager;
import com.siki.cashcount.exception.NotEnoughPastDataException;
import com.siki.cashcount.model.Correction;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.converter.NumberStringConverter;

/**
 *
 * @author tamas.siklosi
 */
public class CorrectionControl extends GridPane {
    
    @FXML private Text txtType;
    @FXML private Text txtAmount;
    
    private final String baseStyle = 
            "-fx-border-width: 5px;" + 
            "-fx-border-style: round;" +
            "-fx-border-radius: 5px;";
    
    private Correction correction;
    private final DailyBalanceControl parent;
    
    public static final DataFormat CorrectionDataFormat = new DataFormat("com.siki.cashcount.model.Correction");

    public CorrectionControl(Correction correction, DailyBalanceControl parent) {
        this.correction = correction;
        this.parent = parent;
        
        setDragAndDrop();        
        loadUI();
                
        txtType.textProperty().bind(correction.typeProperty());
        txtAmount.textProperty().bind(Bindings.convert(correction.amountProperty()));
    }
    
    private void loadUI() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/control/CorrectionControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    private void setDragAndDrop() {
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
                parent.removeCorrection(correction);
            }
            event.consume();
        });
    }

    public String getType() { return typeProperty().get(); }
    public final void setType(String value) { typeProperty().set(value); }
    public StringProperty typeProperty() { return txtType.textProperty(); }

    public String getAmount() { return amountProperty().get(); }
    public final void setAmount(String value) { amountProperty().set(value); }
    public StringProperty amountProperty() { return txtAmount.textProperty(); }
    
    public void doModify(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/NewCorrectionWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            NewCorrectionWindowController controller = fxmlLoader.getController();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle(parent.getDate().toString());
            stage.setScene(new Scene(root1));
            controller.setContext(correction);
            controller.setDialogStage(stage);
            stage.showAndWait();
            
            if (controller.isOkClicked()) {       
                DataManager.getInstance().calculatePredictions();
            }
        } catch (IOException | NotEnoughPastDataException ex) {
            Logger.getLogger(DailyBalanceControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
