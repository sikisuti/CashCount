/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.siki.cashcount.exception.JsonDeserializeException;
import com.siki.cashcount.exception.NotEnoughPastDataException;
import com.siki.cashcount.exception.TransactionGapException;
import com.siki.cashcount.model.AccountTransaction;
import com.siki.cashcount.model.Correction;
import com.siki.cashcount.model.DailyBalance;
import com.siki.cashcount.model.Saving;
import com.siki.cashcount.serial.CorrectionDeserializer;
import com.siki.cashcount.serial.CorrectionSerializer;
import com.siki.cashcount.serial.DailyBalanceDeserializer;
import com.siki.cashcount.serial.DailyBalanceSerialiser;
import com.siki.cashcount.serial.SavingDeserializer;
import com.siki.cashcount.serial.SavingSerializer;
import com.siki.cashcount.serial.TransactionDeserializer;
import com.siki.cashcount.serial.TransactionSerializer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author tamas.siklosi
 */
public class DataManager {
    private static final DataManager INSTANCE = new DataManager();
    public static DataManager getInstance() { return INSTANCE; }
    
    private ObservableList<DailyBalance> dailyBalanceCache;
    private HashMap<LocalDate, Integer> weeklyAverages;
    private List<String> correctionTypeCache;
    
    public ObservableList<DailyBalance> getAllDailyBalances() throws IOException, JsonDeserializeException {
        if (dailyBalanceCache == null) {
            dailyBalanceCache = FXCollections.observableArrayList();
            final GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(DailyBalance.class, new DailyBalanceDeserializer());
            gsonBuilder.registerTypeAdapter(AccountTransaction.class, new TransactionDeserializer());
            gsonBuilder.registerTypeAdapter(Saving.class, new SavingDeserializer());
            gsonBuilder.registerTypeAdapter(Correction.class, new CorrectionDeserializer());
            final Gson gson = gsonBuilder.create();
                int lineCnt = 0;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("./data.jsn"), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lineCnt++;
                    dailyBalanceCache.add(gson.fromJson(line, DailyBalance.class));
                }
            } catch (IOException e) {
                dailyBalanceCache = null;
                throw e;
            } catch (Exception e) {
                dailyBalanceCache = null;
                throw new JsonDeserializeException(lineCnt, e);
            }
        }
        
        return dailyBalanceCache;
    }
    private DailyBalance getLastDailyBalance() throws IOException, JsonDeserializeException {
        return getAllDailyBalances().get(getAllDailyBalances().size() - 1);
    }
    private DailyBalance getOrCreateDailyBalance(LocalDate date) throws IOException, NotEnoughPastDataException, JsonDeserializeException {
        if (dailyBalanceCache.stream().filter(d -> d.getDate().equals(date)).findFirst().isPresent())
            return dailyBalanceCache.stream().filter(d -> d.getDate().equals(date)).findFirst().get();        
        
        DailyBalance newDb = new DailyBalance();
        // fill the possible date gaps
        while (!getLastDailyBalance().getDate().equals(date)) {
            newDb = new DailyBalance.Builder()
                    .setDate(date)
                    .setBalance(getLastDailyBalance().getTotalMoney() + getDayAverage(getLastDailyBalance().getDate().plusDays(1)))
                    .setPredicted(Boolean.TRUE)
                    //.setPredictedBalance(getLastDailyBalance().getPredictedBalance() + getDayAverage(date) + getLastDailyBalance().getTotalCorrections())
                    .build();
            
            dailyBalanceCache.add(newDb);
        }
            
        return newDb;
    }
    public void saveDailyBalances() throws IOException {        
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./data.jsn"), "UTF-8"))) {
            final GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(DailyBalance.class, new DailyBalanceSerialiser());
            gsonBuilder.registerTypeAdapter(AccountTransaction.class, new TransactionSerializer());
            gsonBuilder.registerTypeAdapter(Saving.class, new SavingSerializer());
            gsonBuilder.registerTypeAdapter(Correction.class, new CorrectionSerializer());
            //gsonBuilder.setPrettyPrinting();
            final Gson gson = gsonBuilder.create();
            for (int i = 0; i < dailyBalanceCache.size(); i++) {
                bw.write(gson.toJson(dailyBalanceCache.get(i), DailyBalance.class));
                if (i < dailyBalanceCache.size() - 1) bw.write("\n");
            }
        } catch (IOException e) {
            throw e;
        }
    }
    
    public Integer saveTransactions(List<AccountTransaction> newTransactions, boolean force) throws IOException, TransactionGapException, NotEnoughPastDataException, JsonDeserializeException {
        
        LocalDate nextDay = getAllDailyBalances().stream().filter(db -> db.isPredicted()).findFirst().get().getDate();
        
        // Filter transactions that already exist and sort the remaining
        newTransactions = newTransactions.stream().filter(t -> t.getDate().isEqual(nextDay) || t.getDate().isAfter(nextDay)).collect(Collectors.toList());
        newTransactions.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        
        // Group transactions by date
        TreeMap<LocalDate, List<AccountTransaction>> groupped = newTransactions.stream().collect(Collectors.groupingBy(t -> t.getDate(), TreeMap::new, Collectors.toList()));
        
        if (groupped.size() > 0) {        
            LocalDate firstDay = newTransactions.get(0).getDate();
            
            // If date gap occures an exception thrown except when the force flag has been set.
            if (!force && !nextDay.equals(firstDay)) {
                TransactionGapException ex = new TransactionGapException();
                int i = -1;
                while (!nextDay.plusDays(++i).equals(firstDay))
                    ex.addDate(nextDay.plusDays(i));

                throw ex;
            }

            for (LocalDate d : groupped.keySet()) {
                DailyBalance db = getOrCreateDailyBalance(d);
                resetPredicted(db);
                
                groupped.get(d).stream().forEach((t) -> {
                    db.addTransaction(t);
                });
            }
            
            calculatePredictions();
        }
        
        System.out.println(newTransactions.size() + " transactions saved");
        return newTransactions.size();
    }
    
    private void resetPredicted(DailyBalance db) throws IOException, JsonDeserializeException {
        int endIndex = getAllDailyBalances().indexOf(db);
        int startIndex = endIndex;
        while (getAllDailyBalances().get(--startIndex).isPredicted()) {}
        startIndex++;
        
        for (int i = startIndex; i <= endIndex; i++) {
            getAllDailyBalances().get(i).setPredicted(Boolean.FALSE);
            getAllDailyBalances().get(i).setBalance(getAllDailyBalances().get(i - 1).getBalance());
        }
    }
    
    @SuppressWarnings("empty-statement")
    private Integer getDayAverage(LocalDate date) throws NotEnoughPastDataException {
        if (weeklyAverages == null) weeklyAverages = new HashMap<>();
        
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./averages.csv", true), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        String line = "";
        
        int averageSum = 0;
        // Consider the last 6 months
        for (int i = -6; i < 0; i++) {
            Integer weeklyAverage = 0;

            // Check if already cached
            if (weeklyAverages.containsKey(date.plusMonths(i))) {
                weeklyAverage = weeklyAverages.get(date.plusMonths(i));
            } else {
                
                // Check if data exists in the past
                if (dailyBalanceCache.get(0).getDate().compareTo(date.plusMonths(i).minusDays(4)) <= 0) {
                    int index = -1;
                    // Search the day in the past
                    while (!dailyBalanceCache.get(++index).getDate().equals(date.plusMonths(i)));
                    int correctionSum = 0;
                    
                    // Summarize the corredtions of the week
                    for (int j = -3; j <= 3; j++) {
                        correctionSum += dailyBalanceCache.get(index + j).getTotalCorrections();
                    }
                                        
                    weeklyAverage = Math.round((dailyBalanceCache.get(index + 3).getTotalMoney() - correctionSum - dailyBalanceCache.get(index - 4).getTotalMoney()) / 7f);
                    weeklyAverages.put(date.plusMonths(i), weeklyAverage);
                }
                else throw new NotEnoughPastDataException();
            }
            averageSum += weeklyAverage;
            line = weeklyAverage.toString().concat(";" + line);
        }
        Integer average = Math.round(averageSum / 6f);
        line = line + ";" + average;
        
        if (bw != null) 
            try {
                bw.write(line);
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        return average;
    }
    
    public void calculatePredictions() throws NotEnoughPastDataException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./averages.csv"), "UTF-8"))) {
            
        } catch (IOException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        weeklyAverages = new HashMap<>();
        List<DailyBalance> dbList = new ArrayList<>();
        List<DailyBalance> notPredictedList = dailyBalanceCache.stream().filter(d -> !d.isPredicted()).collect(Collectors.toList());
        dbList.add(notPredictedList.get(notPredictedList.size() - 1));
        dbList.addAll(dailyBalanceCache.stream().filter(d -> d.isPredicted()).collect(Collectors.toList()));
        for (int i = 1; i < dbList.size(); i++) {
            dbList.get(i).setBalance(dbList.get(i - 1).getTotalMoney() + getDayAverage(dbList.get(i).getDate()) + dbList.get(i).getTotalCorrections());
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./averages.csv", true), "UTF-8"))) {
                bw.write(";;" + dbList.get(i).getBalance().toString() + "\n");
            } catch (IOException ex) {
                Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public List<String> getAllCorrectionType() throws IOException {
        if (correctionTypeCache == null) {
            correctionTypeCache = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("./correctionType.jsn"), "UTF-8"))) {
                final GsonBuilder gsonBuilder = new GsonBuilder();
                final Gson gson = gsonBuilder.create();
                String line;
                while ((line = br.readLine()) != null) {
                    correctionTypeCache.add(gson.fromJson(line, String.class));
                }
            } catch (IOException e) {
                correctionTypeCache = null;
                throw e;
            }
        }
        
        return correctionTypeCache;
    }
    
    public void saveCorrectionTypes() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./correctionType.jsn"), "UTF-8"))) {
            final GsonBuilder gsonBuilder = new GsonBuilder();
            //gsonBuilder.setPrettyPrinting();
            final Gson gson = gsonBuilder.create();
            for (int i = 0; i < correctionTypeCache.size(); i++) {
                bw.write(gson.toJson(correctionTypeCache.get(i), String.class));
                if (i < correctionTypeCache.size() - 1) bw.write("\n");
            }
        } catch (IOException e) {
            throw e;
        }
    }
}
