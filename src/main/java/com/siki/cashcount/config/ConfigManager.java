/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;

public final class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);

    private ConfigManager() {
    }
    
    private static final Properties properties = new Properties();

    public static void initProperties() throws IOException {
        try (InputStreamReader inputStream = new InputStreamReader(new FileInputStream("./config.properties"), "UTF-8")) {
            properties.load(inputStream);
        } catch (IOException e) {
            LOGGER.error("Unable to initialize properties", e);
            throw e;
        }
    }
    
    private static void saveProperties() {
        try (OutputStream outputStream = new FileOutputStream("./config.properties")) {   
            properties.store(outputStream, "CashCount property file");
        } catch (IOException ex) {
            LOGGER.error("Unable to save properties", ex);
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
    
    public static int getIntegerProperty(String propertyName) {
        return Integer.parseInt(getStringProperty(propertyName));
    }
    
    public static void setProperty(String propertyName, String propertyValue) {
        properties.setProperty(propertyName, propertyValue);
        saveProperties();
    }
}
