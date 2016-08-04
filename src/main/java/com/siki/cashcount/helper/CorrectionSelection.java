/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.helper;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author tamas.siklosi
 */
public final class CorrectionSelection {
    private static final CorrectionSelection INSTANCE = new CorrectionSelection();
    public static final CorrectionSelection getInstance() { return INSTANCE; }
    
    private CorrectionSelection() {
        selectedCategory = new SimpleStringProperty();
        setSelectedCategory("");
    }
    
    private final StringProperty selectedCategory;
    public String getSelectedCategory() { return selectedCategory.get(); }
    public void setSelectedCategory(String selectedCategory) { this.selectedCategory.set(selectedCategory); }
    public StringProperty selectedCategoryProperty() { return selectedCategory; }
}
