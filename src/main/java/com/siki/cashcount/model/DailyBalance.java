/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.model;

import java.time.LocalDate;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author tamas.siklosi
 */
public final class DailyBalance {
    private final ObjectProperty date;
    public LocalDate getDate() { return (LocalDate)date.get(); }
    public void setDate(LocalDate date) { this.date.set(date); }
    public ObjectProperty dateProperty() { return date; }
    
    private final IntegerProperty balance;
    public Integer getBalance() { return balance.get(); }
    public void setBalance(Integer balance) { 
        this.balance.set(balance); 
        this.totalMoneyProperty().set(getBalance() + getCash());
    }
    public IntegerProperty balanceProperty() { return balance; }
    
    private final IntegerProperty cash;
    public Integer getCash() { return cash.get(); }
    public void setCash(Integer cash) { 
        this.cash.set(cash); 
        this.totalMoneyProperty().set(getBalance() + getCash());
    }
    public IntegerProperty cashProperty() { return cash; }
    
    private final BooleanProperty predicted;
    public Boolean isPredicted() { return predicted.get(); }
    public void setPredicted(Boolean predicted) { 
        this.predicted.set(predicted); 
//        if (!predicted) predictedBalance.set(0);
    }
    public BooleanProperty predictedProperty() { return predicted; }
    
//    private final IntegerProperty predictedBalance;
//    public Integer getPredictedBalance() { return predictedBalance.get(); }
//    public void setPredictedBalance(Integer predictedBalance) { this.predictedBalance.set(predictedBalance); }
//    public IntegerProperty predictedBalanceProperty() { return predictedBalance; }
    
    private final IntegerProperty totalMoney;
    public Integer getTotalMoney() { return totalMoney.get(); }
    public IntegerProperty totalMoneyProperty() { return totalMoney; }
    
    ObservableList<Saving> savings;
    ObservableList<Correction> corrections;
    ObservableList<AccountTransaction> transactions;

    public DailyBalance() {
        this.date = new SimpleObjectProperty();
        this.balance = new SimpleIntegerProperty();
        this.cash = new SimpleIntegerProperty();
        this.predicted = new SimpleBooleanProperty();
//        this.predictedBalance = new SimpleIntegerProperty();
        this.totalMoney = new SimpleIntegerProperty();
        savings = FXCollections.observableArrayList();
        corrections = FXCollections.observableArrayList();
        transactions = FXCollections.observableArrayList();
    }

    public DailyBalance(LocalDate date, Integer balance, Boolean predicted) {
        this();
        setDate(date);
        setBalance(balance);
        setPredicted(predicted);
    }
    
    private DailyBalance(Builder builder) {
        this();
        setDate(builder.date);
        setBalance(builder.balance);
        setCash(builder.cash);
        setPredicted(builder.predicted);
//        setPredictedBalance(builder.predictedBalance);
    }
    
    public void addSaving(Saving saving) {
        savings.add(saving);
    }
    
    public ObservableList<Saving> getSavings() {
        return savings;
    }
    
    public Integer getTotalSavings() {
        return savings.stream().mapToInt(i -> i.getAmount()).sum();
    }
    
    public void addCorrection(Correction correction) {
        corrections.add(correction);
    }
    
    public ObservableList<Correction> getCorrections() {
        return corrections;
    }
    
    public void addTransaction(AccountTransaction transaction) {
        transactions.add(transaction);
        setBalance(transaction.getBalance());
    }
    
    public ObservableList<AccountTransaction> getTransactions() {
        return transactions;
    }
    
    public Integer getTotalCorrections() {
        return corrections.stream().mapToInt(c -> c.getAmount()).sum();
    }
    
    public static class Builder {
        LocalDate date;
        Integer balance;
        Integer cash;
        Boolean predicted;
//        Integer predictedBalance;

        public Builder setDate(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder setBalance(Integer balance) {
            this.balance = balance;
            return this;
        }

        public Builder setCash(Integer cash) {
            this.cash = cash;
            return this;
        }

        public Builder setPredicted(Boolean predicted) {
            this.predicted = predicted;
            return this;
        }

//        public Builder setPredictedBalance(Integer predictedBalance) {
//            this.predictedBalance = predictedBalance;
//            return this;
//        }      
        
        public DailyBalance build() {
            return new DailyBalance(this);
        }
    }
}
