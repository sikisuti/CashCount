/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;

/**
 *
 * @author tamas.siklosi
 */
public class PredictedCorrection {
    
    private String category;
    public String getCategory() { return category; }
    
    private String subCategory;
    public String getSubCategory() { return subCategory; }
        
    private DayOfWeek dayOfWeek;
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    
    private Month month;
    public Month getMonth() { return month; }
    
    private Integer monthDay;
    public Integer getMonthDay() { return monthDay; }
    
    private Integer day;
    public Integer getDay() { return day; }
    
    private LocalDate startDate;
    public LocalDate getStartDate() { return startDate; }
    
    private LocalDate endDate;
    public LocalDate getEndDate() { return endDate; }
    
    private Integer amount;
    public Integer getAmount() { return amount; }
            
    private PredictedCorrection(Builder builder){
        this.category = builder.category;
        this.subCategory = builder.subCategory;
        this.dayOfWeek = builder.dayOfWeek;
        this.month = builder.month;
        this.monthDay = builder.monthDay;
        this.day = builder.day;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.amount = builder.amount;
    }    
    
    private class MonthAndDay {
        private Month month;
        public Month getMonth() { return month; }
        private Integer day;
        public Integer getDay() { return day; }
        
        private MonthAndDay(Month month, Integer day) {
            this.month = month;
            this.day = day;
        }
    }
    
    public static class Builder {
        private String category;
        private String subCategory;
        private DayOfWeek dayOfWeek;
        private Month month;
        private Integer monthDay;
        private Integer day;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer amount;
        
        public Builder setCategory(String category) {
            this.category = category;
            return this;
        }
        
        public Builder setSubCategory(String subCategory) {
            this.subCategory = subCategory;
            return this;
        }
        
        public Builder setDayOfWeek(DayOfWeek dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
            return this;
        }
        
        public Builder setMonthAndDay(Month month, Integer monthDay) {
            this.month = month;
            this.monthDay = monthDay;
            return this;
        }
        
        public Builder setDay(Integer day) {
            this.day = day;
            return this;
        }
        
        public Builder setStartDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }
        
        public Builder setEndDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }
        
        public Builder setAmount(Integer amount) {
            this.amount = amount;
            return this;
        }
        
        public PredictedCorrection build() {
            return new PredictedCorrection(this);
        }
    }
    
}
