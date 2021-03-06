/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.model;

import com.siki.cashcount.data.DataManager;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author tamas.siklosi
 */
public class Correction implements Externalizable {    
    
    private Long id;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
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
    
    private DailyBalance dailyBalance;
    public DailyBalance getDailyBalance() { return dailyBalance; }
    public void setDailyBalance(DailyBalance dailyBalance) { this.dailyBalance = dailyBalance; }
        
    private BooleanProperty paired;
    public Boolean isPaired() { return paired.get(); }
    public void setPaired(Boolean value) { this.paired.set(value); }
    public BooleanProperty pairedProperty() { return paired; }
    
    private Long pairedTransactionId;
    public void setPairedTransactionId(Long value) { pairedTransactionId = value; }
    public Long getPairedTransactionId() { return pairedTransactionId; }
    
    private AccountTransaction pairedTransaction;
    public void setPairedTransaction(AccountTransaction transaction) {
        pairedTransaction = transaction;
        setPaired(transaction != null ? true : false);
        setPairedTransactionId(transaction != null ? transaction.getId() : null);
    }
    public AccountTransaction getPairedTransaction() {
        return pairedTransaction;
    }

    public Correction() {
        this.amount = new SimpleIntegerProperty();
        this.comment = new SimpleStringProperty();
        this.type = new SimpleStringProperty();
        this.paired = new SimpleBooleanProperty();
    }
    
    private Correction(Builder builder) {
        this();
        this.id = builder.id == null ? DataManager.getInstance().getNextCorrectionId() : builder.id;
        this.type.set(builder.type);
        this.amount.set(builder.amount);
        this.comment.set(builder.comment);
        this.dailyBalance = builder.dailyBalance;
        setPairedTransactionId(builder.pairedTransactionId);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(getId());
        out.writeObject(getType());
        out.writeInt(getAmount());
        out.writeObject(getComment());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        setId(in.readLong());
        setType((String)in.readObject());
        setAmount(in.readInt());
        setComment((String)in.readObject());
    }
    
    public static class Builder {
        Long id;
        Integer amount = 0;
        String comment;
        String type;
        DailyBalance dailyBalance;
        Long pairedTransactionId;
        
        public Builder setId(Long id) {
            this.id = id;
            return this;
        }
        
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
        
        public Builder setDailyBalance(DailyBalance dailyBalance) {
            this.dailyBalance = dailyBalance;
            return this;
        }
        
        public Builder setPairedTransactionId(Long id) {
            this.pairedTransactionId = id;
            return this;
        }
        
        public Correction build() {
            return new Correction(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        Correction other = (Correction)obj;
        
        boolean rtn =
                this.getId().equals(other.getId()) &&
                this.getType().equals(other.getType()) &&
                this.getAmount().equals(other.getAmount()) &&
                this.getComment().equals(other.getComment()) &&
                (this.getPairedTransactionId() == null ? 
                    (other.getPairedTransactionId() == null ? true : false) : 
                    (this.getPairedTransactionId().equals(other.getPairedTransactionId())));
        
        return rtn;
    }
    
    
}
