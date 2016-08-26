/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.data;

//<editor-fold desc="Imports" defaultstate="collapsed">

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.siki.cashcount.config.ConfigManager;
import com.siki.cashcount.constant.CashFlowSeriesEnum;
import com.siki.cashcount.control.DateHelper;
import com.siki.cashcount.exception.JsonDeserializeException;
import com.siki.cashcount.exception.NotEnoughPastDataException;
import com.siki.cashcount.exception.TransactionGapException;
import com.siki.cashcount.model.AccountTransaction;
import com.siki.cashcount.model.Correction;
import com.siki.cashcount.model.DailyBalance;
import com.siki.cashcount.model.MatchingRule;
import com.siki.cashcount.model.SavingStore;
import com.siki.cashcount.serial.CorrectionDeserializer;
import com.siki.cashcount.serial.CorrectionSerializer;
import com.siki.cashcount.serial.DailyBalanceDeserializer;
import com.siki.cashcount.serial.DailyBalanceSerialiser;
import com.siki.cashcount.serial.SavingStoreDeserializer;
import com.siki.cashcount.serial.SavingStoreSerializer;
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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;

//</editor-fold>

/**
 *
 * @author tamas.siklosi
 */
public class DataManager {
    private static final DataManager INSTANCE = new DataManager();
    public static DataManager getInstance() { return INSTANCE; }
    
    //<editor-fold desc="Constants" defaultstate="collapsed">
    public static final String GENERAL_TEXT = "Általános";
    public static final String TRANSACTION_COMMENT_NAME = "Közlemény";
    public static final String TRANSACTION_TYPE_NAME = "Forgalomtípus";
    public static final String TRANSACTION_OWNER_NAME = "Számlatulajdonos";
    //</editor-fold>
    
    //<editor-fold desc="Fields" defaultstate="collapsed">
    
    private ObservableList<DailyBalance> dailyBalanceCache;
    private HashMap<LocalDate, Integer> weeklyAverages;
    private List<String> correctionTypeCache;
    private List<SavingStore> savingCache;
    private TreeMap<LocalDate, HashMap<CashFlowSeriesEnum, ObservableList<Data<Date, Integer>>>> pastSeries;
    private TreeMap<LocalDate, LinkedHashMap<String, Integer>> pastDifferences;
    private ObservableList<MatchingRule> matchingRules;
    private ObservableList<String> categories = FXCollections.observableArrayList();
    private ObservableList<String> subCategories = FXCollections.observableArrayList();
    
    Gson gsonDeserializer;
    Gson gsonSerializer;
    
    //</editor-fold>

    private DataManager() {        
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DailyBalance.class, new DailyBalanceDeserializer());
        gsonBuilder.registerTypeAdapter(AccountTransaction.class, new TransactionDeserializer());
        gsonBuilder.registerTypeAdapter(Correction.class, new CorrectionDeserializer());
        gsonBuilder.registerTypeAdapter(SavingStore.class, new SavingStoreDeserializer());
        gsonDeserializer = gsonBuilder.create();
        
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DailyBalance.class, new DailyBalanceSerialiser());
        gsonBuilder.registerTypeAdapter(AccountTransaction.class, new TransactionSerializer());
        gsonBuilder.registerTypeAdapter(Correction.class, new CorrectionSerializer());
        gsonBuilder.registerTypeAdapter(SavingStore.class, new SavingStoreSerializer());
        gsonSerializer = gsonBuilder.create();
    }  
    
    //<editor-fold desc="DailyBalance methods" defaultstate="collapsed">
    
    public ObservableList<DailyBalance> getAllDailyBalances() throws IOException, JsonDeserializeException {
        if (dailyBalanceCache == null) {
            String dataPath = ConfigManager.getStringProperty("DataPath");
            dailyBalanceCache = loadDailyBalances(dataPath);
            categorize();
        }
        
        return dailyBalanceCache;
    }
    private ObservableList<DailyBalance> loadDailyBalances(String dataPath) throws IOException, JsonDeserializeException {        
        ObservableList<DailyBalance> rtnList = FXCollections.observableArrayList();
        int lineCnt = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dataPath), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineCnt++;
                DailyBalance db = gsonDeserializer.fromJson(line, DailyBalance.class);
                if (db.getDate().isBefore(LocalDate.now().minusMonths(7).withDayOfMonth(1)))
                    continue;
                getSavings(db.getDate()).stream().forEach(s -> db.addSaving(s));
                db.getCorrections().stream().forEach((c) -> {
                    c.setDailyBalance(db);
                });
                rtnList.add(db);
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonDeserializeException(lineCnt, e);
        }
        
        return rtnList;
    }
    public DailyBalance getLastDailyBalance() throws IOException, JsonDeserializeException {
        return getAllDailyBalances().get(getAllDailyBalances().size() - 1);
    }
    public DailyBalance getFirstDailyBalance() throws IOException, JsonDeserializeException {
        return getAllDailyBalances().get(0);
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
        String dataPath = ConfigManager.getStringProperty("DataPath");
        FileTime lastModifiedTime = Files.getLastModifiedTime(Paths.get(dataPath));
        LocalDate lastModifiedDate = LocalDateTime.ofInstant(lastModifiedTime.toInstant(), ZoneId.systemDefault()).toLocalDate();
        if (!lastModifiedDate.equals(LocalDate.now())) {
            String backupPath = ConfigManager.getStringProperty("BackupPath");
            if (Files.notExists(Paths.get(backupPath))) Files.createDirectory(Paths.get(backupPath));
            Files.copy(Paths.get(dataPath), Paths.get(backupPath + "/data_" + lastModifiedDate + ".jsn"));
        }
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataPath), "UTF-8"))) {
            for (int i = 0; i < dailyBalanceCache.size(); i++) {
                bw.write(gsonSerializer.toJson(dailyBalanceCache.get(i), DailyBalance.class));
                if (i < dailyBalanceCache.size() - 1) bw.write("\n");
            }
        } catch (IOException e) {
            throw e;
        }
    }
    
    //</editor-fold>
    
    //<editor-fold desc="Transaction metods" defaultstate="collapsed">
    
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
    
    //</editor-fold>
    
    //<editor-fold desc="Correction methods" defaultstate="collapsed">
    
    public List<String> getAllCorrectionType() throws IOException {
        if (correctionTypeCache == null) {
            correctionTypeCache = new ArrayList<>();
            String correctionTypesPath = ConfigManager.getStringProperty("CorrectionTypesPath");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(correctionTypesPath), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    correctionTypeCache.add(gsonDeserializer.fromJson(line, String.class));
                }
            } catch (IOException e) {
                correctionTypeCache = null;
                throw e;
            }
        }
        
        return correctionTypeCache;
    }    
    public void saveCorrectionTypes() throws IOException {
        String correctionTypesPath = ConfigManager.getStringProperty("CorrectionTypesPath");
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(correctionTypesPath), "UTF-8"))) {
            for (int i = 0; i < correctionTypeCache.size(); i++) {
                bw.write(gsonSerializer.toJson(correctionTypeCache.get(i), String.class));
                if (i < correctionTypeCache.size() - 1) bw.write("\n");
            }
        } catch (IOException e) {
            throw e;
        }
    }
    public Long getNextCorrectionId() {
        List<Correction> corrections = dailyBalanceCache.stream().flatMap(db -> db.getCorrections().stream()).collect(Collectors.toList());
        return corrections.stream().mapToLong(c -> c.getId()).max().getAsLong() + 1;
    }
    private HashMap<String, Integer> collectCorrections(ObservableList<DailyBalance> dbList, LocalDate date) throws IOException, JsonDeserializeException {
        HashMap<String, Integer> rtn = new HashMap<>();
        
        List<Correction> predictedCorrectionsUpToNow = new ArrayList<>();
        List<Correction> predictedAllCorrections = new ArrayList<>();
        List<Correction> actCorrectionsUpToNow = new ArrayList<>();
        List<Correction> actAllCorrections = new ArrayList<>();
        
        // List of predicted corrections in the past between the prediction date and now
        List<DailyBalance> predictedDailyBalancesUpToNow = dbList.stream()
                .filter(d -> d.getDate().isAfter(date.minusDays(1)) && d.getDate().isBefore(LocalDate.now()))
                .collect(Collectors.toList());
        for (DailyBalance db : predictedDailyBalancesUpToNow) {
            for (Correction c : db.getCorrections()) {
                predictedCorrectionsUpToNow.add(c);
            }
        }
        
        // List of predicted corrections after the prediction date (including the future corrections)
        List<DailyBalance> predictedAllDailyBalances = dbList.stream()
                .filter(d -> d.getDate().isAfter(date.minusDays(1)))
                .collect(Collectors.toList());
        for (DailyBalance db : predictedAllDailyBalances) {
            for (Correction c : db.getCorrections()) {
                predictedAllCorrections.add(c);
            }
        }
        
        // List of actual corrections between the prediction date and now
        List<DailyBalance> actDailyBalancesUpToNow = getAllDailyBalances().stream()
                .filter(d -> d.getDate().isAfter(date.minusDays(1)) && d.getDate().isBefore(LocalDate.now()))
                .collect(Collectors.toList());
        for (DailyBalance db : actDailyBalancesUpToNow) {
            for (Correction c : db.getCorrections()) {
                actCorrectionsUpToNow.add(c);
            }
        }
        
        // List of actual corrections after the prediction date (including the future corrections)
        List<DailyBalance> actAllDailyBalances = getAllDailyBalances().stream()
                .filter(d -> d.getDate().isAfter(date.minusDays(1)))
                .collect(Collectors.toList());
        for (DailyBalance db : actAllDailyBalances) {
            for (Correction c : db.getCorrections()) {
                actAllCorrections.add(c);
            }
        }
        
        for (Correction predictedCorrection : predictedCorrectionsUpToNow) {
            Optional<Correction> matchingCorrection = actAllCorrections.stream().filter(c -> c.getId().equals(predictedCorrection.getId())).findFirst();
            if (matchingCorrection.isPresent()) {
                if (!matchingCorrection.get().getAmount().equals(predictedCorrection.getAmount())) {
                    putInto(rtn, predictedCorrection.getType(), matchingCorrection.get().getAmount() - predictedCorrection.getAmount());
                }
            } else {
                putInto(rtn, predictedCorrection.getType(), -predictedCorrection.getAmount());
            }
        }
        for (Correction actCorrection : actCorrectionsUpToNow) {
            Optional<Correction> matchingCorrection = predictedAllCorrections.stream()
                    .filter(c -> c.getId().equals(actCorrection.getId()))
                    .findFirst();
            if (matchingCorrection.isPresent()) {
                if (!matchingCorrection.get().getAmount().equals(actCorrection.getAmount()) && 
                        matchingCorrection.get().getDailyBalance().getDate().isAfter(LocalDate.now().minusDays(1))) {
                    putInto(rtn, actCorrection.getType(), actCorrection.getAmount() - matchingCorrection.get().getAmount());
                }
            } else {
                putInto(rtn, actCorrection.getType(), actCorrection.getAmount());
            }
        }
        
        // Calculate General daily expense differences
        DailyBalance predictedStartDailyBalance = dbList.stream()
                .filter(d -> d.getDate().isEqual(date.minusDays(1)))
                .findFirst()
                .get();
        DailyBalance predictedEndDailyBalance = dbList.stream()
                .filter(d -> d.getDate().isEqual(LocalDate.now().minusDays(1)))
                .findFirst()
                .get();
        Integer predictedExpense = predictedEndDailyBalance.getTotalMoney()
                - predictedStartDailyBalance.getTotalMoney() 
                - predictedDailyBalancesUpToNow.stream().mapToInt(db -> db.getCorrections().stream().mapToInt(c -> c.getAmount()).sum()).sum();
        
        DailyBalance actStartDailyBalance = getAllDailyBalances().stream()
                .filter(d -> d.getDate().isEqual(date.minusDays(1)))
                .findFirst()
                .get();
        DailyBalance actEndDailyBalance = getAllDailyBalances().stream()
                .filter(d -> d.getDate().isEqual(LocalDate.now().minusDays(1)))
                .findFirst()
                .get();
        Integer actExpense = actEndDailyBalance.getTotalMoney()
                - actStartDailyBalance.getTotalMoney() 
                - actDailyBalancesUpToNow.stream().mapToInt(db -> db.getCorrections().stream().mapToInt(c -> c.getAmount()).sum()).sum();
        
        if (actExpense - predictedExpense != 0)
            rtn.put(GENERAL_TEXT, actExpense - predictedExpense);
        
        return rtn;
    }
    
    //</editor-fold>
    
    //<editor-fold desc="Saving method" defaultstate="collapsed">
    
    private void loadSavings() throws IOException, JsonDeserializeException {
        savingCache = new ArrayList<>();
        
        String filePath = ConfigManager.getStringProperty("SavingStorePath");
        int lineCnt = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineCnt++;
                savingCache.add(gsonDeserializer.fromJson(line, SavingStore.class));
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonDeserializeException(lineCnt, e);
        }
    }
    public List<SavingStore> getSavings(LocalDate date) throws IOException, JsonDeserializeException {
        if (savingCache == null) loadSavings();
        
        return savingCache.stream().filter(s ->                 
                (s.getFrom().isEqual(date) || s.getFrom().isBefore(date)) && 
                (s.getTo() == null || s.getTo().isAfter(date)))
                .collect(Collectors.toList());
    }
    
    //</editor-fold>
    
    //<editor-fold desc="Past data methods" defaultstate="collapsed">
    
    public HashMap<CashFlowSeriesEnum, ObservableList<Data<Date, Integer>>> getPastSeries(LocalDate date) throws IOException, JsonDeserializeException {
        if (pastSeries == null) {
            loadAllPastSeries();
        }
        
        HashMap<CashFlowSeriesEnum, ObservableList<Data<Date, Integer>>> rtn = null;        
        int decr = 0;
        while ((rtn = pastSeries.getOrDefault(date.minusDays(decr), null)) == null && date.minusDays(decr).isAfter(pastDifferences.firstKey())) { decr++; }
        return rtn;
    }    
    public LinkedHashMap<String, Integer> getPastDifferences(LocalDate date) throws IOException, JsonDeserializeException {
        if (pastDifferences == null) {
            loadAllPastSeries();
        }
        
        LinkedHashMap<String, Integer> rtn = null;        
        int decr = 0;
        while ((rtn = pastDifferences.getOrDefault(date.minusDays(decr), null)) == null && date.minusDays(decr).isAfter(pastSeries.firstKey())) { decr++; }
        return rtn;
    }    
    public void loadAllPastSeries() throws IOException, JsonDeserializeException {
        pastSeries = new TreeMap<>();
        pastDifferences = new TreeMap<>();
                
        String backupPath = ConfigManager.getStringProperty("BackupPath");
        
        if (!Files.exists(Paths.get(backupPath))) 
            Files.createDirectory(Paths.get(backupPath));
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(backupPath), "*.{jsn}")) {
            for (Path entry: stream) {
                loadPastSeries(entry);
            }            
            loadPastSeries(null);
        } catch (Exception ex) {
            throw ex;
        }
    }    
    private void loadPastSeries(Path entry) throws IOException, JsonDeserializeException {
        ObservableList<Data<Date, Integer>> savingSeries;
        ObservableList<Data<Date, Integer>> cashSeries;
        ObservableList<Data<Date, Integer>> accountSeries;
        
        LocalDate lDate;
        if (entry != null) {
            String fileName = entry.getFileName().toString().split("[.]")[0];
            String dateString = fileName.substring(fileName.length() - 10);
            lDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        else lDate = LocalDate.now();
        if (!pastSeries.containsKey(lDate)) {
            savingSeries = FXCollections.observableArrayList();
            cashSeries = FXCollections.observableArrayList();
            accountSeries = FXCollections.observableArrayList();   
            ObservableList<DailyBalance> dbList;
            if (entry != null) dbList = loadDailyBalances(entry.toString());
            else dbList = getAllDailyBalances();
            for (DailyBalance db : dbList) {
                Date date = DateHelper.toDate(db.getDate());
                Integer yValue = db.getTotalSavings();
                savingSeries.add(new XYChart.Data(date, yValue));
                yValue = yValue + db.getCash();
                cashSeries.add(new XYChart.Data(date, yValue));
                yValue = yValue + db.getBalance();
                accountSeries.add(new XYChart.Data(date, yValue));                        
            }
            HashMap<CashFlowSeriesEnum, ObservableList<Data<Date, Integer>>> sr = new HashMap<>(3);
            sr.put(CashFlowSeriesEnum.SAVING, savingSeries);
            sr.put(CashFlowSeriesEnum.CASH, cashSeries);
            sr.put(CashFlowSeriesEnum.ACCOUNT, accountSeries);
            pastSeries.put(lDate, sr);
            
            HashMap<String, Integer> correctionDiffs = collectCorrections(dbList, lDate);
            
            List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(correctionDiffs.entrySet());
            
            // Sorting the list based on values
            Collections.sort(list, new Comparator<Entry<String, Integer>>()
            {
                public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                    if (o1.getKey().equals(GENERAL_TEXT)) return 1;
                    if (o2.getKey().equals(GENERAL_TEXT)) return -1;
                    return o1.getValue().compareTo(o2.getValue());
                }
            });
            
            // Maintaining insertion order with the help of LinkedList
            LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
            list.stream().forEach((e) -> {
                sortedMap.put(e.getKey(), e.getValue());
            });
            
            pastDifferences.put(lDate, sortedMap);
        }        
    }    
    
    private void putInto(HashMap<String, Integer> map, String name, Integer amount) {
        if (map.containsKey(name)) {
            map.put(name, map.get(name) + amount);
        } else {
            map.put(name, amount);
        }
    }
    
    //</editor-fold>
    
    //<editor-fold desc="Matching rule methods" defaultstate="collapsed">
    
    public ObservableList<MatchingRule> getAllMatchingRules() throws IOException, JsonDeserializeException {
        if (matchingRules == null) {
            loadMatchingRules();
        }
        return matchingRules;
    }
    private void loadMatchingRules() throws IOException, JsonDeserializeException {
        matchingRules = FXCollections.observableArrayList();
        
        String filePath = ConfigManager.getStringProperty("MatchingRulesPath");
        int lineCnt = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineCnt++;
                matchingRules.add(gsonDeserializer.fromJson(line, MatchingRule.class));
            }
        } catch (IOException e) {
            
        } catch (Exception e) {
            throw new JsonDeserializeException(lineCnt, e);
        }
    }
    private void saveMatchingRules() throws IOException {
        String filePath = ConfigManager.getStringProperty("MatchingRulesPath");
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"))) {
            for (int i = 0; i < matchingRules.size(); i++) {
                bw.write(gsonSerializer.toJson(matchingRules.get(i), MatchingRule.class));
                if (i < matchingRules.size() - 1) bw.write("\n");
            }
        } catch (IOException e) {
            throw e;
        }
    }
    private MatchingRule findMatchingRule(AccountTransaction transaction) throws IOException, JsonDeserializeException {
        for (MatchingRule mr : getAllMatchingRules()) {
            if ((mr.getField().equals(TRANSACTION_COMMENT_NAME) && transaction.getComment().toLowerCase().contains(mr.getPattern().toLowerCase())) ||
                    (mr.getField().equals(TRANSACTION_TYPE_NAME) && transaction.getTransactionType().toLowerCase().contains(mr.getPattern().toLowerCase())) ||
                    (mr.getField().equals(TRANSACTION_OWNER_NAME) && transaction.getOwner().toLowerCase().contains(mr.getPattern().toLowerCase()))) {
                return mr;
            }
        }
        return null;
    }
    public void addMatchingRule(MatchingRule matchingRule) throws IOException, JsonDeserializeException {
        matchingRules.add(matchingRule);
        saveMatchingRules();
        categorize();
    }
    
    //</editor-fold>
    
    //<editor-fold desc="Categorie method" defaultstate="collapsed">
    
    public ObservableList<String> getAllCategories() {
        return categories;
    }
    public ObservableList<String> getAllSubCategories() {
        return subCategories;
    }
    public void categorize() throws IOException, JsonDeserializeException {
        for (DailyBalance db : dailyBalanceCache) {
            for (AccountTransaction t : db.getTransactions()) {
                if (t.getCategory() == null) {
                    MatchingRule mr = findMatchingRule(t);
                    if (mr != null) {
                        t.setCategory(mr.getCategory());
                        t.setSubCategory(mr.getSubCategory());
                    }
                } 
                if (t.getCategory() != null) {
                    if (!categories.contains(t.getCategory())) {
                        categories.add(t.getCategory());
                        FXCollections.sort(categories);
                    }
                    if (!subCategories.contains(t.getSubCategory())) {
                        subCategories.add(t.getSubCategory());
                        FXCollections.sort(subCategories);
                    }
                }
            }
        }
    }
    public TreeMap<String, Integer> getStatistics(Integer year, Month month) throws Exception {
        TreeMap<String, Integer> rtn = new TreeMap<>();
        
        Optional<DailyBalance> firstDb = dailyBalanceCache.stream().filter(db -> db.getDate().isEqual(LocalDate.of(year, month, 1))).findFirst();
        if (!firstDb.isPresent()) throw new Exception("Data for given date does not exists");
        int firstDbIndex = dailyBalanceCache.indexOf(firstDb.get());
        Integer previousBalance = null;
        if (firstDbIndex != 0)
            previousBalance = dailyBalanceCache.get(firstDbIndex - 1).getTotalMoney();
        
        List<DailyBalance> dailyBalancesOfMonth = dailyBalanceCache.stream().filter(db -> db.getDate().getYear() == year && db.getDate().getMonth() == month).collect(Collectors.toList());
        
        Map<String, List<Correction>> cList = dailyBalancesOfMonth.stream().flatMap(db -> db.getCorrections().stream()).collect(Collectors.groupingBy(c -> c.getType()));
        
        for (String key : cList.keySet()) {
            rtn.put(key, cList.get(key).stream().mapToInt(c -> c.getAmount()).sum());
        }
        
        if (previousBalance != null)
            rtn.put(DataManager.GENERAL_TEXT, dailyBalancesOfMonth.get(dailyBalancesOfMonth.size() - 1).getTotalMoney() - previousBalance - rtn.values().stream().mapToInt(i -> i).sum());
        
        return rtn;
    }
    
    //</editor-fold>
    
    //<editor-fold desc="Helper methods" defaultstate="collapsed">
    
    public void addOneMonth() throws IOException, JsonDeserializeException, NotEnoughPastDataException {
        LocalDate actDate = getLastDailyBalance().getDate().plusDays(1);
        LocalDate endDate = getLastDailyBalance().getDate().plusMonths(2).withDayOfMonth(1);
        
        while (actDate.isBefore(endDate)) {
            DailyBalance db = new DailyBalance.Builder()
                                        .setDate(actDate)
                                        .setPredicted(Boolean.TRUE)
                                        .setReviewed(Boolean.FALSE)
                                        .build();
            
            getSavings(actDate).stream().forEach(s -> db.addSaving(s));
            final LocalDate ld = actDate;
            getAllDailyBalances().stream().filter(d -> d.getDate()
                    .isEqual(ld.minusMonths(1)))
                    .findFirst().get().getCorrections().stream().forEach(c -> db.addCorrection(c));
            dailyBalanceCache.add(db);
            actDate = actDate.plusDays(1);
        }
        calculatePredictions();
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
    private Integer getDayAverage(LocalDate date) throws NotEnoughPastDataException, IOException {
        Boolean exportDataForDebug = ConfigManager.getBooleanProperty("ExportDataForDebug");
        
        if (weeklyAverages == null) weeklyAverages = new HashMap<>();
        
        BufferedWriter bw = null;
        if (exportDataForDebug) {
            try {
                String exportDataPath = ConfigManager.getStringProperty("ExportDataPath");
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportDataPath, true), "UTF-8"));
            } catch (UnsupportedEncodingException | FileNotFoundException ex) {
                Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            if (exportDataForDebug) line = weeklyAverage.toString().concat(";" + line);
        }
        Integer average = Math.round(averageSum / 6f);
        if (exportDataForDebug) {
            line = line + ";" + average;
            
            if (bw != null) {
                try {
                    bw.write(line);
                    bw.close();
                } catch (IOException ex) {
                    Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }        
        
        return average;
    }
    
    public void calculatePredictions() throws NotEnoughPastDataException, IOException {
        Boolean exportDataForDebug = ConfigManager.getBooleanProperty("ExportDataForDebug");
        String exportDataPath ="";
        if (exportDataForDebug) {
            exportDataPath = ConfigManager.getStringProperty("ExportDataPath");
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportDataPath), "UTF-8"))) {

            } catch (IOException ex) {
                Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        weeklyAverages = new HashMap<>();
        List<DailyBalance> dbList = new ArrayList<>();
        List<DailyBalance> notPredictedList = dailyBalanceCache.stream().filter(d -> !d.isPredicted()).collect(Collectors.toList());
        dbList.add(notPredictedList.get(notPredictedList.size() - 1));
        dbList.addAll(dailyBalanceCache.stream().filter(d -> d.isPredicted()).collect(Collectors.toList()));
        for (int i = 1; i < dbList.size(); i++) {
            dbList.get(i).setBalance(dbList.get(i - 1).getTotalMoney() + getDayAverage(dbList.get(i).getDate()) + dbList.get(i).getTotalCorrections());
            if (exportDataForDebug) {
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportDataPath, true), "UTF-8"))) {
                    bw.write(";;" + dbList.get(i).getBalance().toString() + "\n");
                } catch (IOException ex) {
                    Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public boolean needSave() throws IOException, JsonDeserializeException {
        String dataPath = ConfigManager.getStringProperty("DataPath");
        ObservableList<DailyBalance> original = loadDailyBalances(dataPath);
        
        if (original.size() != dailyBalanceCache.size()) return true;
        
        for (int i = 0; i < original.size(); i++) {
            if (!original.get(i).equals(dailyBalanceCache.get(i))) return true;
        }
        
        return false;
    }
    
    //</editor-fold>
}
