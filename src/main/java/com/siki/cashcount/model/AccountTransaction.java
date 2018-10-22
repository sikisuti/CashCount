/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.model;

import com.siki.cashcount.data.DataManager;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.beans.property.*;

public final class AccountTransaction {
    
    private Long id;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
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
        
    private BooleanProperty paired;
    public Boolean isPaired() { return paired.get(); }
    public void setPaired(Boolean value) { this.paired.set(value); }
    public BooleanProperty pairedProperty() { return paired; }
    
    private List<Correction> pairedCorrections;
    public void addPairedCorrection(Correction correction) { 
        if (!pairedCorrections.contains(correction)) {
            pairedCorrections.add(correction);
        }
        setPaired(true);
    }
    public void removePairedCorrection(Correction correction) {
        if (pairedCorrections.contains(correction)) {
            pairedCorrections.remove(correction);
        }
        if (pairedCorrections.isEmpty()) setPaired(false);
    }
    public Integer getNotPairedAmount() {
        return getAmount() - pairedCorrections.stream().mapToInt(Correction::getAmount).sum();
    }

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
        this.pairedCorrections = new ArrayList<>();
        this.paired = new SimpleBooleanProperty();
    }
    
    private AccountTransaction(Builder builder) {
        this();
        this.id = builder.id == null ? DataManager.getInstance().getNextTransactionId() : builder.id;
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
        Long id;
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
        
        public Builder setId(Long id) {
            this.id = id;
            return this;
        }
        
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
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        AccountTransaction other = (AccountTransaction)obj;
        
        return
                this.getId().equals(other.getId()) &&
                this.getTransactionType().equals(other.getTransactionType()) &&
                this.getAmount().equals(other.getAmount()) &&
                this.getBalance().equals(other.getBalance()) &&
                this.getAccountNumber().equals(other.getAccountNumber()) &&
                this.getOwner().equals(other.getOwner()) &&
                this.getComment().equals(other.getComment()) &&
                this.getCounter().equals(other.getCounter()) &&
                this.getCategory().equals(other.getCategory()) &&
                this.getSubCategory().equals(other.getSubCategory());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, transactionType, date, amount, balance, accountNumber, owner, comment, counter, category, subCategory, paired, pairedCorrections);
    }
}
