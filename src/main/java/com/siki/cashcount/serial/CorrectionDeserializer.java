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
import com.siki.cashcount.model.Correction;
import java.lang.reflect.Type;

/**
 *
 * @author tamas.siklosi
 */
public class CorrectionDeserializer implements JsonDeserializer<Correction> {

    @Override
    public Correction deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        final JsonObject jsonObject = je.getAsJsonObject();
        
        final Long id = jsonObject.has("id") ? jsonObject.get("id").getAsLong() : null;
        final Integer amount = jsonObject.get("amount").getAsInt();
        final String comment = jsonObject.get("comment").getAsString();
        final String correctionType = jsonObject.get("type").getAsString();
        final Long pairedTransactionId = jsonObject.has("pairedTransactionId") ? jsonObject.get("pairedTransactionId").getAsLong() : null;
        
        Correction c = new Correction.Builder()
                .setId(id)
                .setAmount(amount)
                .setComment(comment)
                .setType(correctionType)
                .setPairedTransactionId(pairedTransactionId)
                .build();
        
        return c;
    }
    
}
