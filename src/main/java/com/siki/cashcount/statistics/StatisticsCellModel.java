package com.siki.cashcount.statistics;

import com.siki.cashcount.model.AccountTransaction;
import com.siki.cashcount.model.Correction;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StatisticsCellModel {
    private Set<Correction> corrections = new HashSet<>();
    private Set<AccountTransaction> transactions = new HashSet<>();
    private Integer average;
    private StatisticsCellModel previousStatisticsModel;

    public void putCorrection(Correction correction) {
        corrections.add(correction);
    }
    
    public void putAllCorrections(List<Correction> corrections) {
    	for (Correction correction : corrections) {
    		this.corrections.add(correction);
    	}
    }
    
    public void putTransaction(AccountTransaction transaction) {
    	transactions.add(transaction);
    }
    
    public void putAllTransactions(List<AccountTransaction> transactions) {
    	for (AccountTransaction transaction : transactions) {
    		this.transactions.add(transaction);
    	}
    }
    
    public Integer getAmount() {
    	Integer amount = 0;
    	if (!corrections.isEmpty()) {
    		amount = corrections.stream().mapToInt(Correction::getAmount).sum();
    	} else if (!transactions.isEmpty()) {
    		amount = transactions.stream().mapToInt(AccountTransaction::getAmount).sum();
    	}
    	
    	return amount;
    }
    
    public String getDetails() {
    	String details = "";
    	if (!corrections.isEmpty()) {
    		details = corrections.stream().map(Correction::getComment).collect(Collectors.joining("\n"));
    	} else if (!transactions.isEmpty()) {
    		details = transactions.stream().map(AccountTransaction::getComment).collect(Collectors.joining("\n"));
    	}
    	
    	return details;
    }
    
    public void setAverage(Integer average) {
    	this.average = average;
    }
    
    public Integer getAverage() {
    	return average;
    }
    
    public void setPreviousStatisticsModel(StatisticsCellModel statisticsModel) {
    	this.previousStatisticsModel = statisticsModel;
    }
    
    public StatisticsCellModel getPreviousStatisticsModel() {
    	return previousStatisticsModel;
    }
}
