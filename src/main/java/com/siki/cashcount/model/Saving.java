/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author tamas.siklosi
 */
public class Saving {
    private final IntegerProperty amount;
    public Integer getAmount() { return amount.get(); }
    public void setAmount(Integer amount) { this.amount.set(amount); }
    public IntegerProperty amountProperty() { return amount; }
    
    private final StringProperty comment;    
    public String getComment() { return comment.get(); }
    public void setComment(String comment) { this.comment.set(comment); }
    public StringProperty commentProperty() { return comment; }

    public Saving() {
        this.amount = new SimpleIntegerProperty();
        this.comment = new SimpleStringProperty();
    }
}
