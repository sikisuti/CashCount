/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class StopWatch {
    private static final Logger LOGGER = LoggerFactory.getLogger(StopWatch.class);
    
    private static final HashMap<String, Long> ROUNDS = new HashMap<>();
    
    private StopWatch() {
    }
    
    public static void start(String name) {
        if (!ROUNDS.containsKey(name)) {
            ROUNDS.put(name, System.currentTimeMillis());
        }
    }
    
    public static void stop(String name) {
        long stop = System.currentTimeMillis();
        long start = ROUNDS.get(name);
        ROUNDS.remove(name);
        LOGGER.info("{} time: {}s", name, (stop - start) / 1000d);
    }
}
