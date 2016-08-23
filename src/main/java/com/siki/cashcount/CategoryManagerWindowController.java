/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount;

import com.siki.cashcount.control.ExceptionDialog;
import com.siki.cashcount.data.DataManager;
import com.siki.cashcount.exception.JsonDeserializeException;
import com.siki.cashcount.model.MatchingRule;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author tamas.siklosi
 */
public class CategoryManagerWindowController implements Initializable {
    @FXML private ComboBox cbField;
    @FXML private TextField tfPattern;
    @FXML private ComboBox cbCategory;
    @FXML private ComboBox cbSubCategory;
    @FXML private ListView lvRules;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            cbField.getItems().addAll(DataManager.TRANSACTION_COMMENT_NAME, DataManager.TRANSACTION_TYPE_NAME, DataManager.TRANSACTION_OWNER_NAME);
            cbField.setValue(DataManager.TRANSACTION_COMMENT_NAME);
            cbCategory.setItems(DataManager.getInstance().getAllCategories());
            cbSubCategory.setItems(DataManager.getInstance().getAllSubCategories());
            lvRules.setItems(DataManager.getInstance().getAllMatchingRules());
        } catch (IOException | JsonDeserializeException ex) {
            Logger.getLogger(CategoryManagerWindowController.class.getName()).log(Level.SEVERE, null, ex);
            ExceptionDialog.get(ex).showAndWait();
        }
    }    
    
    @FXML
    private void add(ActionEvent event) {
        try {
            if (cbField.getValue() == null || tfPattern.getText() == null || cbCategory.getValue() == null || cbSubCategory.getValue() == null)
                return;
            
            MatchingRule mr = new MatchingRule.Builder()
                    .setField(cbField.getValue().toString())
                    .setPattern(tfPattern.getText())
                    .setCategory(cbCategory.getValue().toString())
                    .setSubCategory(cbSubCategory.getValue().toString())
                    .build();
            DataManager.getInstance().addMatchingRule(mr);
        } catch (IOException | JsonDeserializeException ex) {
            Logger.getLogger(CategoryManagerWindowController.class.getName()).log(Level.SEVERE, null, ex);
            ExceptionDialog.get(ex).showAndWait();
        }
    }
}
