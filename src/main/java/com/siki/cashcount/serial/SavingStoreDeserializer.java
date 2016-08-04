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
import com.siki.cashcount.model.SavingStore;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author tamas.siklosi
 */
public class SavingStoreDeserializer implements JsonDeserializer<SavingStore> {

    @Override
    public SavingStore deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        final JsonObject jsonObject = je.getAsJsonObject();
        
        final LocalDate from = LocalDate.parse(jsonObject.get("from").getAsString(), DateTimeFormatter.ISO_DATE);
        String stringTo = jsonObject.get("to").getAsString();
        LocalDate to = null;
        if (!stringTo.isEmpty())
            to = LocalDate.parse(stringTo, DateTimeFormatter.ISO_DATE);
        final Integer amount = jsonObject.get("amount").getAsInt();
        final String comment = jsonObject.get("comment").getAsString();
        
        SavingStore s = new SavingStore();
        s.setFrom(from);
        s.setTo(to);
        s.setAmount(amount);
        s.setComment(comment);
        
        return s;
    }
    
}
