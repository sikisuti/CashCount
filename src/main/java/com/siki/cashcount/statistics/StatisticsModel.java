package com.siki.cashcount.statistics;

import com.siki.cashcount.model.Correction;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class StatisticsModel {
    private Set<Correction> corrections = new HashSet<>();
    private int average;

    public void putCorrection(Correction correction) {
        corrections.add(correction);
    }
    
    public Integer getAmount() {
    	return corrections.stream().mapToInt(Correction::getAmount).sum();
    }
    
    public String getDetails() {
    	return corrections.stream().map(Correction::getComment).collect(Collectors.joining("\n"));
    }
    
    public void setAverage(int average) {
    	this.average = average;
    }
    
    public int getAverage() {
    	return average;
    }
}
