/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.serial;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.siki.cashcount.model.DailyBalance;
import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author tamas.siklosi
 */
public class DailyBalanceSerialiser implements JsonSerializer<DailyBalance> {

    @Override
    public JsonElement serialize(DailyBalance db, Type type, JsonSerializationContext jsc) {
        final JsonObject jsonObject = new JsonObject();
        
        jsonObject.addProperty("date", db.getDate().format(DateTimeFormatter.ISO_DATE));
        jsonObject.addProperty("balance", db.getBalance());
        jsonObject.addProperty("cash", db.getCash());
        jsonObject.addProperty("predicted", db.isPredicted());
        jsonObject.addProperty("reviewed", db.isReviewed());
//        jsonObject.addProperty("predictedBalance", db.getPredictedBalance());
        
        final JsonElement jsonTransactions = jsc.serialize(db.getTransactions());
        jsonObject.add("transactions", jsonTransactions);
//        final JsonElement jsonSavings = jsc.serialize(db.getSavings());
//        jsonObject.add("savings", jsonSavings);
        final JsonElement jsonCorrections = jsc.serialize(db.getCorrections());
        jsonObject.add("corrections", jsonCorrections);
        
        return jsonObject;
    }
    
}
