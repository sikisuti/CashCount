/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.serial;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.siki.cashcount.model.AccountTransaction;
import com.siki.cashcount.model.Correction;
import com.siki.cashcount.model.DailyBalance;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author tamas.siklosi
 */
public class DailyBalanceDeserializer implements JsonDeserializer<DailyBalance> {

    @Override
    public DailyBalance deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        final JsonObject jsonObject = je.getAsJsonObject();
        
        final LocalDate date = LocalDate.parse(jsonObject.get("date").getAsString(), DateTimeFormatter.ISO_DATE);
        final Integer balance = jsonObject.get("balance").getAsInt();
        final Integer cash = jsonObject.get("cash").getAsInt();
        final Boolean predicted = jsonObject.get("predicted").getAsBoolean();
        Boolean reviewed;
        if (jsonObject.get("reviewed") != null)
            reviewed = jsonObject.get("reviewed").getAsBoolean();
        else
            reviewed = Boolean.FALSE;
        
        AccountTransaction[] transactionArray = jdc.deserialize(jsonObject.get("transactions"), AccountTransaction[].class);
//        Saving[] savingArray = jdc.deserialize(jsonObject.get("savings"), Saving[].class);
        Correction[] correctionArray = jdc.deserialize(jsonObject.get("corrections"), Correction[].class);
        
        final DailyBalance db = new DailyBalance();
        db.setDate(date);
        db.setBalance(balance);
        db.setCash(cash);
        db.setPredicted(predicted);
        db.setReviewed(reviewed);
        
        for (AccountTransaction tr : transactionArray) {
            db.addTransaction(tr);
        }
//        for (Saving s : savingArray) {
//            db.addSaving(s);
//        }
        for (Correction c : correctionArray) {
            db.addCorrection(c);
        }
        
        return db;
    }
    
}
