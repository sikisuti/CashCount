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
import com.siki.cashcount.model.SavingStore;
import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author tamas.siklosi
 */
public class SavingStoreSerializer implements JsonSerializer<SavingStore> {

    @Override
    public JsonElement serialize(SavingStore s, Type type, JsonSerializationContext jsc) {
        final JsonObject jsonObject = new JsonObject();
        
        jsonObject.addProperty("from", s.getFrom().format(DateTimeFormatter.ISO_DATE));
        if (s.getTo() != null) 
            jsonObject.addProperty("to", s.getFrom().format(DateTimeFormatter.ISO_DATE));
        else
            jsonObject.addProperty("to", "");
        jsonObject.addProperty("amount", s.getAmount());
        jsonObject.addProperty("comment", s.getComment());
        
        return jsonObject;
    }
    
}
