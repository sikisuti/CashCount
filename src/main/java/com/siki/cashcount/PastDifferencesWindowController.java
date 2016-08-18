/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author tamas.siklosi
 */
public class PastDifferencesWindowController implements Initializable {
    
    @FXML GridPane grdRoot;
    @FXML Label lblDate;
    @FXML Label lblAmount;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    public void refreshDiffs(LinkedHashMap<String, Integer> diffs, LocalDate date) {
        if (diffs != null) {
            grdRoot.getChildren().clear();

            int rowNum = -1;
            int amount = 0;
            for (String key : diffs.keySet()) {
                rowNum++;
                Label name = new Label(key + ": ");
                GridPane.setRowIndex(name, rowNum);
                GridPane.setColumnIndex(name, 0);
                grdRoot.getChildren().add(name);

                Text value = new Text(NumberFormat.getCurrencyInstance().format(diffs.get(key)));
                GridPane.setRowIndex(value, rowNum);
                GridPane.setColumnIndex(value, 1);
                grdRoot.getChildren().add(value);

                amount += diffs.get(key);
            }

            lblAmount.setText(NumberFormat.getCurrencyInstance().format(amount));
            this.lblDate.setText(date.toString());
        }
    }
}
