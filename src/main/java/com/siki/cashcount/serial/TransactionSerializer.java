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
import com.siki.cashcount.model.AccountTransaction;
import java.lang.reflect.Type;

/**
 *
 * @author tamas.siklosi
 */
public class TransactionSerializer implements JsonSerializer<AccountTransaction> {

    @Override
    public JsonElement serialize(AccountTransaction t, Type type, JsonSerializationContext jsc) {
        final JsonObject jsonObject = new JsonObject();
        
        jsonObject.addProperty("id", t.getId());
        jsonObject.addProperty("type", t.getTransactionType());
        jsonObject.addProperty("amount", t.getAmount());
        jsonObject.addProperty("balance", t.getBalance());
        jsonObject.addProperty("accountNumber", t.getAccountNumber());
        jsonObject.addProperty("owner", t.getOwner());
        jsonObject.addProperty("comment", t.getComment());
        jsonObject.addProperty("counter", t.getCounter());
        jsonObject.addProperty("subCategory", t.getCategory());
        if (t.isPossibleDuplicate()) {
            jsonObject.addProperty("possibleDuplicate", true);
        }
        
        return jsonObject;
    }
    
}
