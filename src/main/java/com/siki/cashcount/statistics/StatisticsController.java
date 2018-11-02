package com.siki.cashcount.statistics;

import com.siki.cashcount.config.ConfigManager;
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
	
    private SortedMap<LocalDate, StatisticsMonthModel> statisticsMonthModels = new TreeMap<>();
    List<String> consideredCategories;

    public SortedMap<LocalDate, StatisticsMonthModel> getStatistics() throws IOException, JsonDeserializeException {
    	List<DailyBalance> dailyBalances =  DataManager.getInstance().getAllDailyBalances();
        getCorrections(dailyBalances);     
        setBackwardMonthReferences();
        setEndBalances(dailyBalances);
        setGeneralSpent();
        consideredCategories = Arrays.asList(ConfigManager.getStringProperty("ConsideredCategories").split(","));
        getConsideredTransactions(dailyBalances);
        getRestTransactions(dailyBalances);
        setBackwardCellReferences();
        setCashSpent(dailyBalances);
        fillEmptyStatisticsModels();
        calculateAverages();

        return statisticsMonthModels;
    }
    
    private void getCorrections(List<DailyBalance> dailyBalances) {
        Stream<Correction> allCorrections = dailyBalances.stream().map(DailyBalance::getCorrections).flatMap(Collection::stream);
        Map<LocalDate, List<Correction>> dateGroupedCorrections = 
        		allCorrections.collect(Collectors.groupingBy(c -> c.getDailyBalance().getDate().withDayOfMonth(1)));
        
        SortedMap<LocalDate, StatisticsMonthModel> dateAndTypeGroupedStatisticsModels = 
        		dateGroupedCorrections.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> {
        			List<Correction> corrections = e.getValue();
        			Map<String, List<Correction>> typeGroupedCorrections = corrections.stream().collect(Collectors.groupingBy(Correction::getType));
        			Map<String, StatisticsCellModel> dateAndTypeGroupedCorrections = typeGroupedCorrections.entrySet().stream().collect(Collectors.toMap(Entry::getKey, en -> {
        				StatisticsCellModel statisticsModel = new StatisticsCellModel();
        						statisticsModel.putAllCorrections(en.getValue());            	        		
            	        		return statisticsModel;
        						}));
        			StatisticsMonthModel monthModel = new StatisticsMonthModel();
        			monthModel.addAllCellModels(dateAndTypeGroupedCorrections);
        			return monthModel;
        		}, (v1,v2) ->{ throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));}, TreeMap::new));
        
        statisticsMonthModels.putAll(dateAndTypeGroupedStatisticsModels);  
    }
    
    private void setGeneralSpent() {
    	for (Entry<LocalDate, StatisticsMonthModel> monthModelEntry : statisticsMonthModels.entrySet()) {
    		if (monthModelEntry.getValue().getPreviousMonthModel() == null) {
    			continue;
    		}
    		
    		int allCorrections = monthModelEntry.getValue().getCellModels().entrySet().stream().mapToInt(c -> c.getValue().getAmount()).sum();
    		StatisticsCellModel cellModel = new StatisticsCellModel();
    		cellModel.putCorrection(new Correction.Builder()
    				.setAmount(monthModelEntry.getValue().getEndBalance() - monthModelEntry.getValue().getPreviousMonthModel().getEndBalance() - allCorrections)
    				.setComment("Máshová nem sorolható elemek")
    				.build());
    		monthModelEntry.getValue().addCellModel("Általános", cellModel);
    		
    	}
    }
    
    private void getConsideredTransactions(List<DailyBalance> dailyBalances) {
    	Stream<AccountTransaction> allTransactions = dailyBalances.stream().map(DailyBalance::getTransactions).flatMap(Collection::stream)
    			.filter(t -> consideredCategories.contains(t.getCategory()) && (!t.isPaired() || (t.isPaired() && t.getNotPairedAmount() != 0)));
        Map<LocalDate, List<AccountTransaction>> dateGroupedTransactions = 
        		allTransactions.collect(Collectors.groupingBy(t -> { 
        			LocalDate date = t.getDailyBalance().getDate().withDayOfMonth(1); 
        			return date;
        			}));
        
        SortedMap<LocalDate, StatisticsMonthModel> dateAndTypeGroupedStatisticsModels = 
        		dateGroupedTransactions.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> {
        			List<AccountTransaction> transactions = e.getValue();
        			Map<String, List<AccountTransaction>> typeGroupedTransactions = transactions.stream().collect(Collectors.groupingBy(AccountTransaction::getCategory));
        			Map<String, StatisticsCellModel> dateAndTypeGroupedTransactions = typeGroupedTransactions.entrySet().stream().collect(Collectors.toMap(en -> "  -- " + en.getKey(), en -> {
        				StatisticsCellModel statisticsModel = new StatisticsCellModel();
        						statisticsModel.putAllTransactions(en.getValue());            	        		
            	        		return statisticsModel;
        						}));
        			StatisticsMonthModel monthModel = new StatisticsMonthModel();
        			monthModel.addAllCellModels(dateAndTypeGroupedTransactions);
        			return monthModel;
        		}, (v1,v2) ->{ throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));}, TreeMap::new));
        
        for (Entry<LocalDate, StatisticsMonthModel> monthEntry : dateAndTypeGroupedStatisticsModels.entrySet()) {
        	if (statisticsMonthModels.containsKey(monthEntry.getKey())) {
        		statisticsMonthModels.get(monthEntry.getKey()).addAllCellModels(monthEntry.getValue().getCellModels());
        	} else {
        		statisticsMonthModels.put(monthEntry.getKey(), monthEntry.getValue());
        	}
        		
        }
    }
    
    private void getRestTransactions(List<DailyBalance> dailyBalances) {
    	Stream<AccountTransaction> allTransactions = dailyBalances.stream().map(DailyBalance::getTransactions).flatMap(Collection::stream)
    			.filter(t -> !consideredCategories.contains(t.getCategory()) && !"Készpénzfelvét".equalsIgnoreCase(t.getCategory()) && (!t.isPaired() || (t.isPaired() && t.getNotPairedAmount() != 0)));
        Map<LocalDate, List<AccountTransaction>> dateGroupedTransactions = 
        		allTransactions.collect(Collectors.groupingBy(t -> { 
        			LocalDate date = t.getDailyBalance().getDate().withDayOfMonth(1); 
        			return date;
        			}));
        
        for (Entry<LocalDate, List<AccountTransaction>> monthTransactions : dateGroupedTransactions.entrySet()) {
        	StatisticsCellModel cellModel = new StatisticsCellModel();
        	cellModel.putAllTransactions(monthTransactions.getValue());
        	statisticsMonthModels.get(monthTransactions.getKey()).addCellModel("  -- Maradék", cellModel);
        }
    }
    
    private void setEndBalances(List<DailyBalance> dailyBalances) {
    	Map<LocalDate, List<DailyBalance>> dateGroupedDailyBalances = dailyBalances.stream().collect(Collectors.groupingBy(db -> db.getDate().withDayOfMonth(1)));
    	for (Entry<LocalDate, List<DailyBalance>> monthDailyBalances : dateGroupedDailyBalances.entrySet()) {
    		int lastMonthBalance = monthDailyBalances.getValue().get(monthDailyBalances.getValue().size() - 1).getBalance();
    		statisticsMonthModels.get(monthDailyBalances.getKey()).setEndBalance(lastMonthBalance);
    	}
    }
    
    private void setCashSpent(List<DailyBalance> dailyBalances) {  	
    	for (Entry<LocalDate, StatisticsMonthModel> monthEntry : statisticsMonthModels.entrySet()) {
    		if (monthEntry.getValue().getPreviousMonthModel() == null) {
    			continue;
    		}
    		
    		int balanceDifference = monthEntry.getValue().getEndBalance() - monthEntry.getValue().getPreviousMonthModel().getEndBalance();
    		int allCorrections = monthEntry.getValue().getCellModels().entrySet().stream().flatMap(e -> e.getValue().getCorrections().stream()).mapToInt(c -> c.getAmount()).sum();
    		int allTransactions = monthEntry.getValue().getCellModels().entrySet().stream().flatMap(e -> e.getValue().getTransactions().stream()).mapToInt(t -> t.getAmount()).sum();
    		int cashSpent = balanceDifference - allCorrections - allTransactions;
    		StatisticsCellModel cellModel = new StatisticsCellModel();
    		cellModel.putCorrection(new Correction.Builder()
    				.setAmount(cashSpent)
    				.setComment("Készpénzköltés")
    				.build());
    		monthEntry.getValue().addCellModel("  -- Készpénzköltés", cellModel);
    	}
    }
    
    private void fillEmptyStatisticsModels() {
        Set<String> allCorrectionTypes = statisticsMonthModels.entrySet().stream().map(e -> e.getValue().getCellModels().entrySet()).flatMap(Collection::stream).map(e -> e.getKey()).distinct().collect(Collectors.toSet());
        
        for (String type : allCorrectionTypes) {
        	for (Entry<LocalDate, StatisticsMonthModel> monthEntry : statisticsMonthModels.entrySet()) {
        		Map<String, StatisticsCellModel> monthTypes = monthEntry.getValue().getCellModels();
        		if (!monthTypes.containsKey(type)) {
        			monthTypes.put(type, new StatisticsCellModel());
        		}
        	}
        }
    }
    
    private void setBackwardCellReferences() {
    	for (Entry<LocalDate, StatisticsMonthModel> monthEntry : statisticsMonthModels.entrySet()) {
    	    if (!statisticsMonthModels.containsKey(monthEntry.getKey().minusMonths(1))) {
    	        continue;
            }

    	    for (Entry<String, StatisticsCellModel> categoryEntry : monthEntry.getValue().getCellModels().entrySet()) {
    	    	StatisticsCellModel previousStatisticsCellModel =
                        statisticsMonthModels.get(monthEntry.getKey().minusMonths(1)).getCellModels()
                            .get(categoryEntry.getKey());
    	        categoryEntry.getValue().setPreviousStatisticsModel(previousStatisticsCellModel);
            }
        }
    }
    
    private void setBackwardMonthReferences() {
    	for (Entry<LocalDate, StatisticsMonthModel> monthEntry : statisticsMonthModels.entrySet()) {
    	    if (!statisticsMonthModels.containsKey(monthEntry.getKey().minusMonths(1))) {
    	        continue;
            }
    	    
    	    monthEntry.getValue().setPreviousMonthModel(statisticsMonthModels.get(monthEntry.getKey().minusMonths(1)));
        }
    }
    
    private void calculateAverages() {
    	Map<LocalDate, StatisticsMonthModel> filteredMonthStatistics = 
    			statisticsMonthModels.entrySet().stream().filter(e -> e.getKey().plusMonths(AVERAGE_OF_MONTHS).isAfter(LocalDate.now().withDayOfMonth(1).minusMonths(3)))
    			.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (v1,v2) ->{ throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));}, TreeMap::new));
    	for (Entry<LocalDate, StatisticsMonthModel> monthStatisticsEntry : filteredMonthStatistics.entrySet()) {    	
    		calculateMonthAverages(monthStatisticsEntry);
    	}
    }

    private void calculateMonthAverages(Entry<LocalDate, StatisticsMonthModel> monthStatisticsEntry) {
        for (Entry<String, StatisticsCellModel> statisticsEntry : monthStatisticsEntry.getValue().getCellModels().entrySet()) {
            long monthCount = statisticsMonthModels.entrySet().stream().filter(e -> e.getKey().plusMonths(AVERAGE_OF_MONTHS).isAfter(monthStatisticsEntry.getKey()) && !e.getKey().isAfter(monthStatisticsEntry.getKey())).count();
            if (monthCount != AVERAGE_OF_MONTHS) {
                return;
            }

            List<Integer> amounts = statisticsMonthModels.entrySet().stream().filter(e -> e.getKey().plusMonths(AVERAGE_OF_MONTHS + 5).isAfter(monthStatisticsEntry.getKey()) && !e.getKey().isAfter(monthStatisticsEntry.getKey()))
                    .map(e -> e.getValue().getCellModels().entrySet()).flatMap(Collection::stream)
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
