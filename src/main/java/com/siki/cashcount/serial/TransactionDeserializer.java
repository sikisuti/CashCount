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
import java.lang.reflect.Type;

/**
 *
 * @author tamas.siklosi
 */
public class TransactionDeserializer implements JsonDeserializer<AccountTransaction> {

    @Override
    public AccountTransaction deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        final JsonObject jsonObject = je.getAsJsonObject();
        
        final Long id = jsonObject.has("id") ? jsonObject.get("id").getAsLong() : null;
        final String transactionType = jsonObject.get("type").getAsString();
        final Integer amount = jsonObject.get("amount").getAsInt();
        final Integer balance = jsonObject.get("balance").getAsInt();
        final String accountNumber = jsonObject.get("accountNumber").getAsString();
        final String owner = jsonObject.get("owner").getAsString();
        final String comment = jsonObject.get("comment").getAsString();
        final String counter = jsonObject.get("counter").getAsString();
        String category = null;
        if (jsonObject.has("category"))
            category = jsonObject.get("category").getAsString();
        String subCategory = null;
        if (jsonObject.has("subCategory"))
            subCategory = jsonObject.get("subCategory").getAsString();
        
        AccountTransaction tr = new AccountTransaction.Builder()
                .setId(id)
                .setTransactionType(transactionType)
                .setAmount(amount)
                .setBalance(balance)
                .setAccountNumber(accountNumber)
                .setOwner(owner)
                .setComment(comment)
                .setCounter(counter)
                .setCategory(category)
                .setSubCategory(subCategory)
                .build();
        if (jsonObject.has("possibleDuplicate") && jsonObject.get("possibleDuplicate").getAsBoolean()) {
            tr.setPossibleDuplicate(true);
        }

        return tr;
    }
    
}
