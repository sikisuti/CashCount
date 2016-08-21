/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.model;

import java.time.*;
import javafx.beans.property.*;

public final class AccountTransaction {
    private final StringProperty transactionType;    
    public String getTransactionType() { return transactionType.get(); }
    public void setTransactionType(String transactionType) { this.transactionType.set(transactionType); }
    public StringProperty transactionTypeProperty() { return transactionType; }
    
    private final ObjectProperty date;
    public LocalDate getDate() { return (LocalDate)date.get(); }
    public void setDate(LocalDate date) { this.date.set(date); }
    public ObjectProperty dateProperty() { return date; }
    
    private final IntegerProperty amount;
    public Integer getAmount() { return amount.get(); }
    public void setAmount(Integer amount) { this.amount.set(amount); }
    public IntegerProperty amountProperty() { return amount; }
    
    private final IntegerProperty balance;
    public Integer getBalance() { return balance.get(); }
    public void setBalance(Integer balance) { this.balance.set(balance); }
    public IntegerProperty balanceProperty() { return balance; }
    
    private final StringProperty accountNumber;    
    public String getAccountNumber() { return accountNumber.get(); }
    public void setAccountNumber(String accountNumber) { this.accountNumber.set(accountNumber); }
    public StringProperty accountNumberProperty() { return accountNumber; }
    
    private final StringProperty owner;    
    public String getOwner() { return owner.get(); }
    public void setOwner(String owner) { this.owner.set(owner); }
    public StringProperty ownerProperty() { return owner; }
    
    private final StringProperty comment;    
    public String getComment() { return comment.get(); }
    public void setComment(String comment) { this.comment.set(comment); }
    public StringProperty commentProperty() { return comment; }
    
    private final StringProperty counter;
    public String getCounter() { return counter.get(); }
    public void setCounter(String counter) { this.counter.set(counter); }
    public StringProperty counterProperty() { return counter; }
    
    private final StringProperty category;
    public String getCategory() { return category.get(); }
    public void setCategory(String category) { this.category.set(category); }
    public StringProperty categoryProperty() { return category; }
    
    private final StringProperty subCategory;
    public String getSubCategory() { return subCategory.get(); }
    public void setSubCategory(String subCategory) { this.subCategory.set(subCategory); }
    public StringProperty subCategoryProperty() { return subCategory; }

    public AccountTransaction() {
        this.transactionType = new SimpleStringProperty();
        this.date = new SimpleObjectProperty();
        this.amount = new SimpleIntegerProperty();
        this.balance = new SimpleIntegerProperty();
        this.accountNumber = new SimpleStringProperty();
        this.owner = new SimpleStringProperty();
        this.comment = new SimpleStringProperty();
        this.counter = new SimpleStringProperty();     
        this.category = new SimpleStringProperty();
        this.subCategory = new SimpleStringProperty();      
    }
    
    private AccountTransaction(Builder builder) {
        this();
        setTransactionType(builder.transactionType);
        setDate(builder.date);
        setAmount(builder.amount);
        setBalance(builder.balance);
        setAccountNumber(builder.accountNumber);
        setOwner(builder.owner);
        setComment(builder.comment);
        setCounter(builder.counter);
        setCategory(builder.category);
        setSubCategory(builder.subCategory);
    }
    
    public static class Builder {
        String transactionType;
        LocalDate date;
        Integer amount;
        Integer balance;
        String accountNumber;
        String owner;
        String comment;
        String counter;
        String category;
        String subCategory;
        
        public Builder setAmount(Integer amount) {
            this.amount = amount;
            return this;
        }

        public Builder setTransactionType(String transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public Builder setDate(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder setBalance(Integer balance) {
            this.balance = balance;
            return this;
        }

        public Builder setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public Builder setOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder setCounter(String counter) {
            this.counter = counter;
            return this;
        }

        public Builder setCategory(String category) {
            this.category = category;
            return this;
        }

        public Builder setSubCategory(String subCategory) {
            this.subCategory = subCategory;
            return this;
        }
        
        public AccountTransaction build() {
            return new AccountTransaction(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        AccountTransaction other = (AccountTransaction)obj;
        
        boolean rtn = 
                this.getTransactionType().equals(other.getTransactionType()) &&
                this.getAmount().equals(other.getAmount()) &&
                this.getBalance().equals(other.getBalance()) &&
                this.getAccountNumber().equals(other.getAccountNumber()) &&
                this.getOwner().equals(other.getOwner()) &&
                this.getComment().equals(other.getComment()) &&
                this.getCounter().equals(other.getCounter());
        if (!rtn) return false;
        
        if (this.getCategory() == null) {
            if (other.getCategory() != null) return false;
        } else {
            if (!this.getCategory().equals(other.getCategory())) {
                return false;
            }
        }
        if (this.getSubCategory() == null) {
            if (other.getSubCategory() != null) return false;
        } else {
            if (!this.getSubCategory().equals(other.getSubCategory())) {
                return false;
            }
        }
        return true;
    }
}
