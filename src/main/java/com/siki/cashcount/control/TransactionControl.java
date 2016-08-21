/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.control;

import com.siki.cashcount.model.AccountTransaction;
import java.text.NumberFormat;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 *
 * @author tamas.siklosi
 */
public class TransactionControl extends GridPane {
    private ObservableList<AccountTransaction> transactions;
    
    public TransactionControl(ObservableList<AccountTransaction> transactions) {
        this.transactions = transactions;
        
        buildLayout();
    }
    
    private void buildLayout() {
        int rowCnt = -1;
        for (AccountTransaction t : transactions) {
            rowCnt++;
            Label lblType = new Label(t.getTransactionType());
            Label lblAmount = new Label(NumberFormat.getCurrencyInstance().format(t.getAmount()));
            Label lblOwner = new Label(t.getOwner());
            Label lblComment = new Label(t.getComment());
            ComboBox cbCategory = new ComboBox();
            cbCategory.valueProperty().bindBidirectional(t.categoryProperty());
            cbCategory.setPrefWidth(200);
            cbCategory.setEditable(true);
            cbCategory.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    validate();
                }
            });
            ComboBox cbSubCategory = new ComboBox();
            cbSubCategory.valueProperty().bindBidirectional(t.subCategoryProperty());
            cbSubCategory.setPrefWidth(200);
            cbSubCategory.setEditable(true);
        
            GridPane.setConstraints(lblType, 0, rowCnt);
            GridPane.setConstraints(lblAmount, 1, rowCnt);
            GridPane.setConstraints(lblOwner, 2, rowCnt);
            GridPane.setConstraints(lblComment, 3, rowCnt);
            GridPane.setConstraints(cbCategory, 4, rowCnt);
            GridPane.setConstraints(cbSubCategory, 5, rowCnt);
        
            this.getChildren().addAll(lblType, lblAmount, lblOwner, lblComment, cbCategory, cbSubCategory);        
        }
        
//        ColumnConstraints columnConstrait = new ColumnConstraints(150d);
//        this.getColumnConstraints().add(0, columnConstrait);
//        this.getColumnConstraints().add(1, columnConstrait);
//        this.getColumnConstraints().add(2, columnConstrait);
//        this.getColumnConstraints().add(3, columnConstrait);
//        this.getColumnConstraints().add(4, columnConstrait);
//        this.getColumnConstraints().add(5, columnConstrait);
        
        this.setStyle("-fx-background-color: white;");
        this.setHgap(20);
        validate();
    }
    
    private void validate() {
        for (Node n : this.getChildren()) {
            if (n.getClass() == ComboBox.class) {
                ComboBox cb = (ComboBox)n;
                if (cb.getValue() == null) {
                    cb.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                } else {
                    cb.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                }
            }
        }
    }
}
