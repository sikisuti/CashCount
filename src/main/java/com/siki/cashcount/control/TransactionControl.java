/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.control;

import com.siki.cashcount.data.DataManager;
import com.siki.cashcount.model.AccountTransaction;
import java.text.NumberFormat;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class TransactionControl extends GridPane {
    private ObservableList<AccountTransaction> transactions;
    private DailyBalanceControl parent;
    
    public TransactionControl(ObservableList<AccountTransaction> transactions, DailyBalanceControl parent) {
        this.transactions = transactions;
        this.parent = parent;
        
        buildLayout();
    }
    
    private void buildLayout() {
        int rowCnt = -1;
        for (AccountTransaction t : transactions) {
            rowCnt++;
            Label lblType = new Label(t.getTransactionType());
            Label lblAmount = new Label(NumberFormat.getCurrencyInstance().format(t.getAmount()));
            Label lblOwner = new Label(t.getOwner());
            Circle isPaired = new Circle(10, new Color(0, 0, 1, 1));
            isPaired.visibleProperty().bind(t.pairedProperty());
            Label lblComment = new Label(t.getComment());
        
            GridPane.setConstraints(lblType, 0, rowCnt);
            GridPane.setConstraints(lblAmount, 1, rowCnt);
            GridPane.setConstraints(lblOwner, 2, rowCnt);
            GridPane.setConstraints(isPaired, 3, rowCnt);
            GridPane.setConstraints(lblComment, 4, rowCnt);

            if (t.isPossibleDuplicate()) {
                HBox duplicateHandler = new HBox();
                Button removeDuplicateButton = new Button("töröl");
                Button notDuplicateButton = new Button("hozzáad");
                duplicateHandler.getChildren().addAll(removeDuplicateButton, notDuplicateButton);
                GridPane.setConstraints(duplicateHandler, 5, rowCnt);
                this.getChildren().add(duplicateHandler);
            }
        
            this.getChildren().addAll(lblType, lblAmount, lblOwner, isPaired, lblComment);
            addCategoryPicker(t, rowCnt);
        }
        
        this.setStyle("-fx-background-color: white;");
        this.setHgap(20);
        validate();
    }

    private void addCategoryPicker(AccountTransaction transaction, int rowCnt) {
        ComboBox cbCategory = null;
        if (transaction.getNotPairedAmount() != 0) {
            cbCategory = new ComboBox();
            cbCategory.setEditable(true);
            cbCategory.setItems(DataManager.getInstance().getAllCategories());
            cbCategory.valueProperty().bindBidirectional(transaction.categoryProperty());
            cbCategory.setPrefWidth(200);
            cbCategory.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    validate();
                }
            });

            GridPane.setConstraints(cbCategory, 6, rowCnt);
            this.getChildren().add(cbCategory);
        }
    }
    
    private void validate() {
        for (Node n : this.getChildren()) {
            if (n.getClass() == ComboBox.class) {
                ComboBox cb = (ComboBox)n;
                if (cb.getValue() == null || transactions.stream().anyMatch(AccountTransaction::isPossibleDuplicate)) {
                    cb.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                } else {
                    cb.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                }
            }
        }
        parent.validate();
    }
}
