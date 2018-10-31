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
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author tamas.siklosi
 */
public class CategoryManagerWindowController implements Initializable {
    @FXML private ComboBox cbField;
    @FXML private TextField tfPattern;
    @FXML private ComboBox cbCategory;
    @FXML private HBox hbCategories;
    
    private TreeMap<String, ObservableList<String>> categories = new TreeMap<>();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            cbField.getItems().addAll(DataManager.TRANSACTION_COMMENT_NAME, DataManager.TRANSACTION_TYPE_NAME, DataManager.TRANSACTION_OWNER_NAME);
            cbField.setValue(DataManager.TRANSACTION_COMMENT_NAME);
            cbCategory.setItems(DataManager.getInstance().getAllCategories());
            for (MatchingRule rule : DataManager.getInstance().getAllMatchingRules()) {
                addRule(rule);
            }
            for (String key : categories.keySet()) {
                addCategory(key);
            }
        } catch (IOException | JsonDeserializeException ex) {
            Logger.getLogger(CategoryManagerWindowController.class.getName()).log(Level.SEVERE, null, ex);
            ExceptionDialog.get(ex).showAndWait();
        }
    }    
    
    private void addCategory(String key) {
        Label categoryName = new Label(key);
        ListView patterns = new ListView(categories.get(key));
        VBox vb = new VBox();
        vb.getChildren().addAll(categoryName, patterns);
        hbCategories.getChildren().add(vb);  
    }
    
    private void addRule(MatchingRule rule) {
        if (categories.containsKey(rule.getCategory())) {
            categories.get(rule.getCategory()).add(rule.getPattern());
            FXCollections.sort(categories.get(rule.getCategory()));
        } else {
            categories.put(rule.getCategory(), FXCollections.observableArrayList());
            categories.get(rule.getCategory()).add(rule.getPattern());
        }
    }
    
    @FXML
    private void add(ActionEvent event) {
        try {
            if (cbField.getValue() == null || tfPattern.getText() == null || cbCategory.getValue() == null)
                return;
            
            MatchingRule mr = new MatchingRule.Builder()
                    .setField(cbField.getValue().toString())
                    .setPattern(tfPattern.getText())
                    .setCategory(cbCategory.getValue().toString())
                    .build();
            DataManager.getInstance().addMatchingRule(mr);
            if (!categories.containsKey(mr.getCategory())) {                
                addRule(mr);
                addCategory(mr.getCategory());
            } else {
                addRule(mr);
            }
        } catch (IOException | JsonDeserializeException ex) {
            Logger.getLogger(CategoryManagerWindowController.class.getName()).log(Level.SEVERE, null, ex);
            ExceptionDialog.get(ex).showAndWait();
        }
    }
}
