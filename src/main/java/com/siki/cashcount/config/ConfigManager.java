/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 *
 * @author tamas.siklosi
 */
public final class ConfigManager {
    private final static ConfigManager INSTANCE = new ConfigManager();
    public final static ConfigManager getInstance() {
        return INSTANCE;
    }
    private ConfigManager() {
    }
    
    private Properties properties;
    
    private void loadProperties() throws IOException {
        properties = new Properties();
        
        try (InputStream inputStream = new FileInputStream("./config.properties")) {            
            
            if (inputStream != null) {
                    properties.load(inputStream);
            } else {
                    throw new FileNotFoundException("property file 'config/config.properties' not found in the classpath");
            }            
        }
    }
    
    private void saveProperties() throws IOException {
        try (OutputStream outputStream = new FileOutputStream("./config.properties")) {            
            
            if (outputStream != null) {
                    properties.store(outputStream, "CashCount property file");
            } else {
                    throw new FileNotFoundException("property file 'config/config.properties' not found in the classpath");
            }            
        }
    }
    
    public String getProperty(String propertyName) throws IOException {
        if (properties == null) {
            loadProperties();
        }
        return properties.getProperty(propertyName);
    }
    
    public void setProperty(String propertyName, String propertyValue) throws IOException {
        properties.setProperty(propertyName, propertyValue);
        saveProperties();
    }
}
