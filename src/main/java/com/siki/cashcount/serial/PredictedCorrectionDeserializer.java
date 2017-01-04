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
import com.siki.cashcount.model.PredictedCorrection;
import java.lang.reflect.Type;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author tamas.siklosi
 */
public class PredictedCorrectionDeserializer implements JsonDeserializer<PredictedCorrection> {

    @Override
    public PredictedCorrection deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        final JsonObject jsonObject = je.getAsJsonObject();
                
        final String category = jsonObject.get("category").getAsString();
        final String subCategory = jsonObject.get("subCategory").getAsString();
        final Integer dayNoOfWeek = jsonObject.has("dayOfWeek") ? jsonObject.get("dayOfWeek").getAsInt() : null;
        final Integer month = jsonObject.has("month") ? jsonObject.get("month").getAsInt() : null;
        final Integer monthDay = jsonObject.has("monthDay") ? jsonObject.get("monthDay").getAsInt() : null;
        final Integer day = jsonObject.has("day") ? jsonObject.get("day").getAsInt() : null;
        final LocalDate startDate = jsonObject.has("startDate") ? LocalDate.parse(jsonObject.get("startDate").getAsString(), DateTimeFormatter.ISO_DATE) : null;
        final LocalDate endDate = jsonObject.has("endDate") ? LocalDate.parse(jsonObject.get("endDate").getAsString(), DateTimeFormatter.ISO_DATE) : null;
        final Integer amount = jsonObject.get("amount").getAsInt();
        
        PredictedCorrection pc = new PredictedCorrection.Builder()
                .setCategory(category)
                .setSubCategory(subCategory)
                .setDayOfWeek(dayNoOfWeek != null ? DayOfWeek.of(dayNoOfWeek) : null)
                .setMonthAndDay(month != null ? Month.of(month) : null, monthDay)
                .setDay(day)
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setAmount(amount)
                .build();
        
        return pc;
    }
    
}
