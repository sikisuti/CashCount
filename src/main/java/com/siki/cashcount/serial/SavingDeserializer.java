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
import com.siki.cashcount.model.Saving;
import java.lang.reflect.Type;

/**
 *
 * @author tamas.siklosi
 */
public class SavingDeserializer implements JsonDeserializer<Saving> {

    @Override
    public Saving deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        final JsonObject jsonObject = je.getAsJsonObject();
        
        final Integer amount = jsonObject.get("amount").getAsInt();
        final String comment = jsonObject.get("comment").getAsString();
        
        Saving s = new Saving();
        s.setAmount(amount);
        s.setComment(comment);
        
        return s;
    }
    
}
