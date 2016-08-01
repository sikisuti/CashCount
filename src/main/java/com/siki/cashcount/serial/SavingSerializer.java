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
import com.siki.cashcount.model.Saving;
import java.lang.reflect.Type;

/**
 *
 * @author tamas.siklosi
 */
public class SavingSerializer implements JsonSerializer<Saving> {

    @Override
    public JsonElement serialize(Saving s, Type type, JsonSerializationContext jsc) {
        final JsonObject jsonObject = new JsonObject();
        
        jsonObject.addProperty("amount", s.getAmount());
        jsonObject.addProperty("comment", s.getComment());
        
        return jsonObject;
    }
    
}
