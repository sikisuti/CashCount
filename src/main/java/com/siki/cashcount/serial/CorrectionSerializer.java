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
import com.siki.cashcount.model.Correction;
import java.lang.reflect.Type;

/**
 *
 * @author tamas.siklosi
 */
public class CorrectionSerializer implements JsonSerializer<Correction> {

    @Override
    public JsonElement serialize(Correction c, Type type, JsonSerializationContext jsc) {
        final JsonObject jsonObject = new JsonObject();
        
        jsonObject.addProperty("id", c.getId());
        jsonObject.addProperty("amount", c.getAmount());
        jsonObject.addProperty("comment", c.getComment());
        jsonObject.addProperty("type", c.getType());
        jsonObject.addProperty("pairedTransactionId", c.getPairedTransaction() != null ? c.getPairedTransaction().getId() : null);
        
        return jsonObject;
    }
    
}
