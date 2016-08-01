/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.exception;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tamas.siklosi
 */
public class TransactionGapException extends Exception {
    public List<LocalDate> missingDates = new ArrayList<>();
    
    public void addDate(LocalDate date) {
        missingDates.add(date);
    }
}
