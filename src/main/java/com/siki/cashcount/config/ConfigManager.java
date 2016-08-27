/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tamas.siklosi
 */
public final class ConfigManager {
//    private final static ConfigManager INSTANCE = new ConfigManager();
//    public final static ConfigManager getInstance() {
//        return INSTANCE;
//    }
    private ConfigManager() {
    }
    
    private static final Properties properties = new Properties();
    
    static {
        
    }
    
//    private void loadProperties() throws IOException {
//        properties = new Properties();
//        
//        try (InputStream inputStream = new FileInputStream("./config.properties")) {            
//            
//            if (inputStream != null) {
//                    properties.load(inputStream);
//            } else {
//                    throw new FileNotFoundException("property file 'config/config.properties' not found in the classpath");
//            }            
//        }
//    }
    
    public static void initProperties() throws IOException {
        try (InputStreamReader inputStream = new InputStreamReader(new FileInputStream("./config.properties"), "UTF-8")) {
            properties.load(inputStream);
        }
    }
    
    private static void saveProperties() {
        try (OutputStream outputStream = new FileOutputStream("./config.properties")) {   
            properties.store(outputStream, "CashCount property file");
        } catch (IOException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static String getStringProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }
    
    public static boolean getBooleanProperty(String propertyName) {
        return Boolean.parseBoolean(getStringProperty(propertyName));
    }
    
    public static double getDoubleProperty(String propertyName) {
        return Double.parseDouble(getStringProperty(propertyName));
    }
    
    public static int getIntegerProperty(String propertyName) throws NumberFormatException {
        return Integer.parseInt(getStringProperty(propertyName));
    }
    
    public static void setProperty(String propertyName, String propertyValue) throws IOException {
        properties.setProperty(propertyName, propertyValue);
        saveProperties();
    }
}
