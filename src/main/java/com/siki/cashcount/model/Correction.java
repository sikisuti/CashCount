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
public class Correction {    
    private final IntegerProperty amount;
    public Integer getAmount() { return amount.get(); }
    public void setAmount(Integer amount) { this.amount.set(amount); }
    public IntegerProperty amountProperty() { return amount; }
    
    private final StringProperty comment;    
    public String getComment() { return comment.get(); }
    public void setComment(String comment) { this.comment.set(comment); }
    public StringProperty commentProperty() { return comment; }
    
    private final StringProperty type;    
    public String getType() { return type.get(); }
    public void setType(String type) { this.type.set(type); }
    public StringProperty typeProperty() { return type; }

    public Correction() {
        this.amount = new SimpleIntegerProperty();
        this.comment = new SimpleStringProperty();
        this.type = new SimpleStringProperty();
    }

    public Correction(String type, Integer amount, String comment) {
        this();
        this.type.set(type);
        this.amount.set(amount);
        this.comment.set(comment);
    }
    
    private Correction(Builder builder) {
        this();
        this.type.set(builder.type);
        this.amount.set(builder.amount);
        this.comment.set(builder.comment);
    }
    
    public static class Builder {
        Integer amount;
        String comment;
        String type;
        
        public Builder setAmount(Integer amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder setComment(String comment) {
            this.comment = comment;
            return this;
        }
        
        public Builder setType(String type) {
            this.type = type;
            return this;
        }
        
        public Correction build() {
            return new Correction(this);
        }
    }
}
