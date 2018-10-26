package com.siki.cashcount.statistics;

import com.siki.cashcount.data.DataManager;
import com.siki.cashcount.exception.JsonDeserializeException;
import com.siki.cashcount.model.Correction;
import com.siki.cashcount.model.DailyBalance;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class StatisticsController {
	private static final int AVERAGE_OF_MONTHS = 12;
	
    private SortedMap<LocalDate, Map<String, StatisticsModel>> statisticsModels = new TreeMap<>();
    private Set<String> allCorrectionTypes = new HashSet<>();
    private int lastTotalAmount;

    public SortedMap<LocalDate, Map<String, StatisticsModel>> getStatistics() throws IOException, JsonDeserializeException {
        List<DailyBalance> dailyBalances =  DataManager.getInstance().getAllDailyBalances();
        for (DailyBalance dailyBalance : dailyBalances) {
            parseDailyBalance(dailyBalance);
        }

        setBackwardReferences();
        calculateAverages();

        return statisticsModels;
    }

    private void parseDailyBalance(DailyBalance dailyBalance) {
        Map<String, StatisticsModel> monthStatistics = getMonthStatistics(dailyBalance.getDate().withDayOfMonth(1));
        List<Correction> corrections = dailyBalance.getCorrections();
        for (Correction correction : corrections) {
            parseCorrection(correction, monthStatistics);
        }
    }

    private Map<String, StatisticsModel> getMonthStatistics(LocalDate date) {
        if (!statisticsModels.containsKey(date)) {
            Map<String, StatisticsModel> monthStatistics = new HashMap<>();
            for (String correctionType : allCorrectionTypes) {
                monthStatistics.put(correctionType, new StatisticsModel());
            }

            statisticsModels.put(date, monthStatistics);
        }

        return statisticsModels.get(date);
    }

    private void parseCorrection(Correction correction, Map<String, StatisticsModel> monthStatistics) {
        StatisticsModel statisticsModel = getStatisticModel(correction.getType(), monthStatistics);
        statisticsModel.putCorrection(correction);
    }

    private StatisticsModel getStatisticModel(String correctionType, Map<String, StatisticsModel> currentMonthStatistics) {
        if (!currentMonthStatistics.containsKey(correctionType)) {
            for (Map.Entry<LocalDate, Map<String, StatisticsModel>> monthStatisticsEntry : statisticsModels.entrySet()) {
                monthStatisticsEntry.getValue().put(correctionType, new StatisticsModel());
            }

            allCorrectionTypes.add(correctionType);
        }

        return currentMonthStatistics.get(correctionType);
    }
    
    private void calculateAverages() {
    	Map<LocalDate, Map<String, StatisticsModel>> filteredMonthStatistics = 
    			statisticsModels.entrySet().stream().filter(e -> e.getKey().plusMonths(AVERAGE_OF_MONTHS).isAfter(LocalDate.now().withDayOfMonth(1)))
    			.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (v1,v2) ->{ throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));}, TreeMap::new));
    	for (Entry<LocalDate, Map<String, StatisticsModel>> monthStatisticsEntry : filteredMonthStatistics.entrySet()) {    	
    		calculateMonthAverages(monthStatisticsEntry);
    	}
    }

    private void calculateMonthAverages(Entry<LocalDate, Map<String, StatisticsModel>> monthStatisticsEntry) {
        for (Entry<String, StatisticsModel> statisticsEntry : monthStatisticsEntry.getValue().entrySet()) {
            List<Integer> amounts = statisticsModels.entrySet().stream().filter(e -> e.getKey().plusMonths(AVERAGE_OF_MONTHS).isAfter(monthStatisticsEntry.getKey()) && !e.getKey().isAfter(monthStatisticsEntry.getKey()))
                    .map(e -> e.getValue().entrySet()).flatMap(Collection::stream)
                    .filter(e -> e.getKey().equals(statisticsEntry.getKey()))
                    .mapToInt(e -> {
                        // Smooth amount peaks
                        Integer amount = e.getValue().getAmount();
                        int previousAverage = e.getValue().getPreviousStatisticsModel() == null || e.getValue().getPreviousStatisticsModel().getAverage() == 0  ?
                                amount : e.getValue().getPreviousStatisticsModel().getAverage();
                        Integer diffFromAverage = amount - previousAverage;
                        return amount - (int) (diffFromAverage * 0.5);
                    }).boxed().collect(Collectors.toList());

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
}
