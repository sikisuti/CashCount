/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.model;

public class MatchingRule {
    private String pattern;
    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }
    
    private String category;
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    private MatchingRule(Builder builder) {
        this.pattern = builder.pattern;
        this.category = builder.category;
    }
    
    public static class Builder {
        private String pattern;
        private String category;
        
        public Builder setPattern(String pattern) {
            this.pattern = pattern;
            return this;
        }
        
        public Builder setCategory(String category) {
            this.category = category;
            return this;
        }
        
        public MatchingRule build() {
            return new MatchingRule(this);
        }
    }

    @Override
    public String toString() {
        return "MatchingRule{pattern=" + pattern + ", category=" + category + '}';
    }
    
    
}
