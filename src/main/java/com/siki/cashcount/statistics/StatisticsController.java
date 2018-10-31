package com.siki.cashcount.statistics;

import com.siki.cashcount.data.DataManager;
import com.siki.cashcount.exception.JsonDeserializeException;
import com.siki.cashcount.helper.DebugWriter;
import com.siki.cashcount.model.AccountTransaction;
import com.siki.cashcount.model.Correction;
import com.siki.cashcount.model.DailyBalance;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.Locale.Category;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatisticsController {
	private static final int AVERAGE_OF_MONTHS = 12;
	
    private SortedMap<LocalDate, Map<String, StatisticsModel>> statisticsModels = new TreeMap<>();
    private int lastTotalAmount;

    public SortedMap<LocalDate, Map<String, StatisticsModel>> getStatistics() throws IOException, JsonDeserializeException {
    	List<DailyBalance> dailyBalances =  DataManager.getInstance().getAllDailyBalances();
        getCorrections(dailyBalances);     
        getTransactions(dailyBalances);

        fillEmptyStatisticsModels();
        setBackwardReferences();
        calculateAverages();

        return statisticsModels;
    }
    
    private void getCorrections(List<DailyBalance> dailyBalances) {
        Stream<Correction> allCorrections = dailyBalances.stream().map(DailyBalance::getCorrections).flatMap(Collection::stream);
        Map<LocalDate, List<Correction>> dateGroupedCorrections = 
        		allCorrections.collect(Collectors.groupingBy(c -> c.getDailyBalance().getDate().withDayOfMonth(1)));
        
        SortedMap<LocalDate, Map<String, StatisticsModel>> dateAndTypeGroupedStatisticsModels = 
        		dateGroupedCorrections.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> {
        			List<Correction> corrections = e.getValue();
        			Map<String, List<Correction>> typeGroupedCorrections = corrections.stream().collect(Collectors.groupingBy(Correction::getType));
        			return typeGroupedCorrections.entrySet().stream().collect(Collectors.toMap(Entry::getKey, en -> {
        						StatisticsModel statisticsModel = new StatisticsModel();
        						statisticsModel.putAllCorrections(en.getValue());            	        		
            	        		return statisticsModel;
        						}));
        		}, (v1,v2) ->{ throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));}, TreeMap::new));
        
        statisticsModels.putAll(dateAndTypeGroupedStatisticsModels);  
    }
    
    private void getTransactions(List<DailyBalance> dailyBalances) {
    	Stream<AccountTransaction> allTransactions = dailyBalances.stream().map(DailyBalance::getTransactions).flatMap(Collection::stream).filter(t -> (!t.isPaired() || (t.isPaired() && t.getNotPairedAmount()!= 0)));
        Map<LocalDate, List<AccountTransaction>> dateGroupedTransactions = 
        		allTransactions.collect(Collectors.groupingBy(t -> { 
        			LocalDate date = t.getDailyBalance().getDate().withDayOfMonth(1); 
        			return date;
        			}));
        
        SortedMap<LocalDate, Map<String, StatisticsModel>> dateAndTypeGroupedStatisticsModels = 
        		dateGroupedTransactions.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> {
        			List<AccountTransaction> transactions = e.getValue();
        			Map<String, List<AccountTransaction>> typeGroupedTransactions = transactions.stream().collect(Collectors.groupingBy(AccountTransaction::getCategory));
        			return typeGroupedTransactions.entrySet().stream().collect(Collectors.toMap(en -> "  -- " + en.getKey(), en -> {
        						StatisticsModel statisticsModel = new StatisticsModel();
        						statisticsModel.putAllTransactions(en.getValue());            	        		
            	        		return statisticsModel;
        						}));
        		}, (v1,v2) ->{ throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));}, TreeMap::new));
        
        for (Entry<LocalDate, Map<String, StatisticsModel>> monthEntry : dateAndTypeGroupedStatisticsModels.entrySet()) {
        	if (statisticsModels.containsKey(monthEntry.getKey())) {
        		statisticsModels.get(monthEntry.getKey()).putAll(monthEntry.getValue());
        	} else {
        		statisticsModels.put(monthEntry.getKey(), monthEntry.getValue());
        	}
        		
        }
    }
    
    private void fillEmptyStatisticsModels() {
        Set<String> allCorrectionTypes = statisticsModels.entrySet().stream().map(e -> e.getValue().entrySet()).flatMap(Collection::stream).map(e -> e.getKey()).distinct().collect(Collectors.toSet());
        
        for (String type : allCorrectionTypes) {
        	for (Entry<LocalDate, Map<String, StatisticsModel>> monthEntry : statisticsModels.entrySet()) {
        		Map<String, StatisticsModel> monthTypes = monthEntry.getValue();
        		if (!monthTypes.containsKey(type)) {
        			monthTypes.put(type, new StatisticsModel());
        		}
        	}
        }
    }
    
    private void setBackwardReferences() {
    	for (Entry<LocalDate, Map<String, StatisticsModel>> monthEntry : statisticsModels.entrySet()) {
    	    if (!statisticsModels.containsKey(monthEntry.getKey().minusMonths(1))) {
    	        continue;
            }

    	    for (Entry<String, StatisticsModel> categoryEntry : monthEntry.getValue().entrySet()) {
    	        StatisticsModel previousStatisticsModel =
                        statisticsModels.get(monthEntry.getKey().minusMonths(1))
                            .get(categoryEntry.getKey());
    	        categoryEntry.getValue().setPreviousStatisticsModel(previousStatisticsModel);
            }
        }
    }
    
    private void calculateAverages() {
    	Map<LocalDate, Map<String, StatisticsModel>> filteredMonthStatistics = 
    			statisticsModels.entrySet().stream().filter(e -> e.getKey().plusMonths(AVERAGE_OF_MONTHS).isAfter(LocalDate.now().withDayOfMonth(1).minusMonths(3)))
    			.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (v1,v2) ->{ throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));}, TreeMap::new));
    	for (Entry<LocalDate, Map<String, StatisticsModel>> monthStatisticsEntry : filteredMonthStatistics.entrySet()) {    	
    		calculateMonthAverages(monthStatisticsEntry);
    	}
    }

    private void calculateMonthAverages(Entry<LocalDate, Map<String, StatisticsModel>> monthStatisticsEntry) {
        for (Entry<String, StatisticsModel> statisticsEntry : monthStatisticsEntry.getValue().entrySet()) {
            long monthCount = statisticsModels.entrySet().stream().filter(e -> e.getKey().plusMonths(AVERAGE_OF_MONTHS).isAfter(monthStatisticsEntry.getKey()) && !e.getKey().isAfter(monthStatisticsEntry.getKey())).count();
            if (monthCount != AVERAGE_OF_MONTHS) {
                return;
            }

            List<Integer> amounts = statisticsModels.entrySet().stream().filter(e -> e.getKey().plusMonths(AVERAGE_OF_MONTHS + 5).isAfter(monthStatisticsEntry.getKey()) && !e.getKey().isAfter(monthStatisticsEntry.getKey()))
                    .map(e -> e.getValue().entrySet()).flatMap(Collection::stream)
                    .filter(e -> e.getKey().equals(statisticsEntry.getKey()))
                    .mapToInt(e -> e.getValue().getAmount()).boxed().collect(Collectors.toList());

            // calculate weighted average
            Double amountSum = 0d;
            Double divider = 0d;
            for (int i = 0; i < amounts.size(); i++) {
                amountSum += amounts.get(i) * (i + 1);
                divider += (i + 1);
            }

            Double averageDbl = divider != 0 ? (amountSum / divider) : 0;
            statisticsEntry.getValue().setAverage(averageDbl.intValue());
        }
    }
}
