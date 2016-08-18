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
import com.siki.cashcount.model.DailyBalance;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * FXML Controller class
 *
 * @author tamas.siklosi
 */
public final class DailyBalanceControl extends BorderPane {
    @FXML private Label txtDate;
    @FXML private Label txtBalance;
    @FXML private TextField tfCash;
    @FXML private CheckBox chkReviewed;
    @FXML private HBox corrections;
    @FXML private HBox hbLine;
    
    @FXML private Button btnAdd;
    
    private final DailyBalance dailyBalance;
    
    public DailyBalance getDailyBalance() {
        return dailyBalance;
    }

    /**
     * Initializes the controller class.
     * @param dailyBalance
     */
    public DailyBalanceControl(DailyBalance dailyBalance) { 
        this.dailyBalance = dailyBalance;
        
        setDragAndDrop();
        
        loadUI();
        
        btnAdd.onActionProperty().set(this::addCorrection);
        btnAdd.setVisible(false);
        chkReviewed.visibleProperty().bind(dailyBalance.predictedProperty().not());
        tfCash.visibleProperty().bind(dailyBalance.predictedProperty().not());

        tfCash.disableProperty().bind(dailyBalance.predictedProperty());
        txtBalance.disableProperty().bind(dailyBalance.predictedProperty());
        txtDate.disableProperty().bind(dailyBalance.predictedProperty());
        
        setDate(dailyBalance.getDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
        setBalance(NumberFormat.getCurrencyInstance().format(dailyBalance.getTotalMoney()));
        dailyBalance.totalMoneyProperty().addListener(new IntegerToTextConverter(txtBalance.textProperty()));
        setCash(NumberFormat.getCurrencyInstance().format(dailyBalance.getCash()));
        tfCash.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (!newValue) {
                try {
                    dailyBalance.setCash(Integer.parseInt(tfCash.getText()));
                    setCash(NumberFormat.getCurrencyInstance().format(dailyBalance.getCash()));
                    try {
                        DataManager.getInstance().calculatePredictions();
                    } catch (IOException ex) {
                        Logger.getLogger(DailyBalanceControl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (NotEnoughPastDataException ex) {
                    Logger.getLogger(DailyBalanceControl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        chkReviewed.selectedProperty().bindBidirectional(dailyBalance.reviewedProperty());
        hbLine.disableProperty().bind(chkReviewed.selectedProperty());
        chkReviewed.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            setBackground();
        });
        loadCorrections();
        
        setBackground();
    }    
    
    private void setDragAndDrop() {
        this.setOnDragOver((DragEvent event) -> {
            /* data is dragged over the target */
            /* accept it only if it is not dragged from the same node 
             * and if it has a string data */
            if (((CorrectionControl)event.getGestureSource()).getParent() != corrections &&
                    !chkReviewed.isSelected() &&
                    event.getDragboard().hasContent(CorrectionControl.CORRECTION_DATA_FORMAT)) {
                /* allow for moving */
                event.acceptTransferModes(TransferMode.MOVE);
            }

            event.consume();
        });
        this.setOnDragEntered((DragEvent event) -> {
            /* the drag-and-drop gesture entered the target */
            /* show to the user that it is an actual gesture target */
             if (((CorrectionControl)event.getGestureSource()).getParent() != corrections &&
                    !chkReviewed.isSelected() &&
                     event.getDragboard().hasContent(CorrectionControl.CORRECTION_DATA_FORMAT)) {
                 this.setStyle("-fx-background-color: yellow;");
             }

             event.consume();
        });
        this.setOnDragExited((DragEvent event) -> {
            /* mouse moved away, remove the graphical cues */
            setBackground();

            event.consume();
        });
        this.setOnDragDropped((DragEvent event) -> {
            /* data dropped */
            /* if there is a string data on dragboard, read it and use it */
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasContent(CorrectionControl.CORRECTION_DATA_FORMAT)) {
                Correction data = (Correction)db.getContent(CorrectionControl.CORRECTION_DATA_FORMAT);
                dailyBalance.getCorrections().add(data);                
                loadCorrections();
                success = true;
            }
            /* let the source know whether the string was successfully 
             * transferred and used */
            event.setDropCompleted(success);

            event.consume();
        });
    }
    
    private void setBackground() {
        if (dailyBalance.getDate().getDayOfWeek() == DayOfWeek.SATURDAY || dailyBalance.getDate().getDayOfWeek() == DayOfWeek.SUNDAY) 
            if (chkReviewed.isSelected())
                this.setStyle("-fx-background-color: green;");
            else
                this.setStyle("-fx-background-color: lightgrey;");
        else
            if (chkReviewed.isSelected())
                this.setStyle("-fx-background-color: lightgreen;");
            else
                this.setStyle("-fx-background-color: none;");
    }
    
    public void loadCorrections() {
        corrections.getChildren().clear();
        dailyBalance.getCorrections().stream().forEach((correction) -> {
            corrections.getChildren().add(new CorrectionControl(correction, this));
        });        
    }
    
    private void loadUI() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/control/DailyBalanceControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public String getDate() { return dateProperty().get(); }
    public final void setDate(String value) { dateProperty().set(value); }
    public StringProperty dateProperty() { return txtDate.textProperty(); }

    public String getBalance() { return balanceProperty().get(); }
    public final void setBalance(String value) { balanceProperty().set(value); }
    public StringProperty balanceProperty() { return txtBalance.textProperty(); }

    public String getCash() { return cashProperty().get(); }
    public final void setCash(String value) { cashProperty().set(value); }
    public StringProperty cashProperty() { return tfCash.textProperty(); }

    @FXML
    protected void doSomething() {
        System.out.println("The button was clicked!");
    }
    
    @FXML
    protected void addCorrection(ActionEvent event) {
            Correction newCorrection = new Correction.Builder().build();
            
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/NewCorrectionWindow.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            NewCorrectionWindowController controller = fxmlLoader.getController();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle(dailyBalance.getDate().toString());
            stage.setScene(new Scene(root1));
            controller.setContext(newCorrection, this);
            controller.setDialogStage(stage);
            stage.showAndWait();
            
            if (controller.isOkClicked()) {
                dailyBalance.getCorrections().add(newCorrection);
                loadCorrections();           
                DataManager.getInstance().calculatePredictions();
            }
        } catch (IOException | NotEnoughPastDataException ex) {
            Logger.getLogger(DailyBalanceControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void removeCorrection(Correction correction) {
        try {
            dailyBalance.getCorrections().remove(correction);
            loadCorrections();
            DataManager.getInstance().calculatePredictions();
        } catch (NotEnoughPastDataException | IOException ex) {
            Logger.getLogger(DailyBalanceControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void mouseEntered(MouseEvent event) {
        btnAdd.setVisible(!chkReviewed.isSelected());
    }
    
    @FXML
    private void mouseExited(MouseEvent event) {
        btnAdd.setVisible(false);
    }
}
