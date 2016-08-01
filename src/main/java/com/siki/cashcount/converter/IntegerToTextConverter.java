/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.converter;

import java.text.NumberFormat;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author tamas.siklosi
 */
public class IntegerToTextConverter implements ChangeListener<Number> {
    private StringProperty stringToBind;

    public IntegerToTextConverter(StringProperty stringToBind) {
        this.stringToBind = stringToBind;
    }    

    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        stringToBind.set(NumberFormat.getCurrencyInstance().format(newValue));
    }
    
}
