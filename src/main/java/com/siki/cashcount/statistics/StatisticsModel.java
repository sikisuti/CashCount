package com.siki.cashcount.statistics;

import com.siki.cashcount.model.Correction;

import java.util.HashSet;
import java.util.Set;

public class StatisticsModel {
    private Set<Correction> corrections = new HashSet<>();

    public void putCorrection(Correction correction) {
        corrections.add(correction);
    }
}
