/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.model;

/**
 *
 * @author tamas.siklosi
 */
public class MatchingRule {
    private String field;
    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
    
    private String pattern;
    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }
    
    private String category;
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    private String subCategory;
    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }

    private MatchingRule(Builder builder) {
        this.field = builder.filed;
        this.pattern = builder.pattern;
        this.category = builder.category;
        this.subCategory = builder.subCategory;
    }
    
    public static class Builder {
        private String filed;
        private String pattern;
        private String category;
        private String subCategory;
        
        public Builder setField(String field) {
            this.filed = field;
            return this;
        }
        
        public Builder setPattern(String pattern) {
            this.pattern = pattern;
            return this;
        }
        
        public Builder setCategory(String category) {
            this.category = category;
            return this;
        }
        
        public Builder setSubCategory(String subCategory) {
            this.subCategory = subCategory;
            return this;
        }
        
        public MatchingRule build() {
            return new MatchingRule(this);
        }
    }
}
