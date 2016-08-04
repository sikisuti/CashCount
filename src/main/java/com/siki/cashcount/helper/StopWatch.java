/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.helper;

import java.util.HashMap;

/**
 *
 * @author tamas.siklosi
 */
public class StopWatch {
    
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
        System.out.println(name + " time: " + (stop - start) / 1000d + "s");
    }
}
