/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.exception;

/**
 *
 * @author tamas.siklosi
 */
public class JsonDeserializeException extends Exception {
    private Integer errorLineNum;

    public Integer getErrorLineNum() {
        return errorLineNum;
    }

    public JsonDeserializeException(Integer errorLineNum, Exception e) {
        super(e);
        this.errorLineNum = errorLineNum;
    }
    
    
}
