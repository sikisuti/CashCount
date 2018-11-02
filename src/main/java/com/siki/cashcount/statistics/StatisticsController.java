package com.siki.cashcount.statistics;

import com.siki.cashcount.config.ConfigManager;
import com.siki.cashcount.data.DataManager;
import com.siki.cashcount.exception.JsonDeserializeException;
import com.siki.cashcount.model.AccountTransaction;
import com.siki.cashcount.model.Correction;
import com.siki.cashcount.model.DailyBalance;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatisticsController {
	private static final int AVERAGE_OF_MONTHS = 12;
	
    private SortedMap<LocalDate, StatisticsMonthModel> statisticsMonthModels = new TreeMap<>();
    List<String> consideredCategories;

    public SortedMap<LocalDate, StatisticsMonthModel> getStatistics() throws IOException, JsonDeserializeException {
    	List<DailyBalance> dailyBalances =  DataManager.getInstance().getAllDailyBalances();
    	initializeModels();   
        setEndBalances(dailyBalances);
        
        getCorrections(dailyBalances);  
        setGeneralSpent();      
        
        consideredCategories = Arrays.asList(ConfigManager.getStringProperty("ConsideredCategories").split(","));
        getConsideredTransactions(dailyBalances);
        getRestTransactions(dailyBalances);

        setCashSpent();
        
        setBackwardCellReferences();  
        fillEmptyStatisticsModels();
        calculateAverages();

        return statisticsMonthModels;
    }
    
    private void initializeModels() {
    	LocalDate date = LocalDate.now().withDayOfMonth(1).minusYears(2).minusMonths(1);
    	StatisticsMonthModel previousMonthModel = null;
    	
    	do {
    		StatisticsMonthModel actMonthModel = new StatisticsMonthModel(previousMonthModel);
    		statisticsMonthModels.put(date, actMonthModel);
    		previousMonthModel = actMonthModel;
    		date = date.plusMonths(1);
    	} while(date.isBefore(LocalDate.now().withDayOfMonth(1).plusYears(1)));
    }
    
    private void setEndBalances(List<DailyBalance> dailyBalances) {
    	Map<LocalDate, List<DailyBalance>> dateGroupedDailyBalances = dailyBalances.stream().collect(Collectors.groupingBy(db -> db.getDate().withDayOfMonth(1)));
    	for (Entry<LocalDate, List<DailyBalance>> monthDailyBalances : dateGroupedDailyBalances.entrySet()) {
    		if (statisticsMonthModels.containsKey(monthDailyBalances.getKey())) {
	    		int lastMonthBalance = monthDailyBalances.getValue().get(monthDailyBalances.getValue().size() - 1).getTotalMoney();
	    		statisticsMonthModels.get(monthDailyBalances.getKey()).setEndBalance(lastMonthBalance);
    		}
    	}
    }
    
    private void getCorrections(List<DailyBalance> dailyBalances) {
        Stream<Correction> allCorrections = dailyBalances.stream().map(DailyBalance::getCorrections).flatMap(Collection::stream);
        Map<LocalDate, List<Correction>> dateGroupedCorrections = 
        		allCorrections.collect(Collectors.groupingBy(c -> c.getDailyBalance().getDate().withDayOfMonth(1)));
        
        Map<LocalDate, Map<String, List<Correction>>> dateAndTypeGroupedCorrections = dateGroupedCorrections.entrySet().stream()
        		.collect(Collectors.toMap(Entry::getKey, e -> e.getValue().stream().collect(Collectors.groupingBy(Correction::getType))));
        
        for (Entry<LocalDate, Map<String, List<Correction>>> monthCorrectionEntry : dateAndTypeGroupedCorrections.entrySet()) {
        	for (Entry<String, List<Correction>> typeCorrectionEntry : monthCorrectionEntry.getValue().entrySet()) {
	        	if (statisticsMonthModels.containsKey(monthCorrectionEntry.getKey())) {
		        	StatisticsCellModel cellModel = new StatisticsCellModel();
		        	cellModel.putAllCorrections(typeCorrectionEntry.getValue());
	        		statisticsMonthModels.get(monthCorrectionEntry.getKey()).addCellModel(typeCorrectionEntry.getKey(), cellModel);
	        	}
        	}
        }
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
        		allTransactions.collect(Collectors.groupingBy(t -> t.getDailyBalance().getDate().withDayOfMonth(1)));
        
        Map<LocalDate, Map<String, List<AccountTransaction>>> dateAndTypeGroupedTransactions = dateGroupedTransactions.entrySet().stream()
        		.collect(Collectors.toMap(Entry::getKey, e -> e.getValue().stream().collect(Collectors.groupingBy(AccountTransaction::getCategory))));
        
        for (Entry<LocalDate, Map<String, List<AccountTransaction>>> monthTransactionEntry : dateAndTypeGroupedTransactions.entrySet()) {
        	for (Entry<String, List<AccountTransaction>> typeTransactionEntry : monthTransactionEntry.getValue().entrySet()) {
        		if (statisticsMonthModels.containsKey(monthTransactionEntry.getKey())) {
		        	StatisticsCellModel cellModel = new StatisticsCellModel();
		        	cellModel.putAllTransactions(typeTransactionEntry.getValue());
		        	statisticsMonthModels.get(monthTransactionEntry.getKey()).addCellModel("  -- " + typeTransactionEntry.getKey(), cellModel);
        		}
        	}
        }
    }
    
    private void getRestTransactions(List<DailyBalance> dailyBalances) {
    	Stream<AccountTransaction> allTransactions = dailyBalances.stream().map(DailyBalance::getTransactions).flatMap(Collection::stream)
    			.filter(t -> !consideredCategories.contains(t.getCategory()) && !"Készpénzfelvét".equalsIgnoreCase(t.getCategory()) && (!t.isPaired() || (t.isPaired() && t.getNotPairedAmount() != 0)));
        Map<LocalDate, List<AccountTransaction>> dateGroupedTransactions = 
        		allTransactions.collect(Collectors.groupingBy(t -> t.getDailyBalance().getDate().withDayOfMonth(1)));
        
        for (Entry<LocalDate, List<AccountTransaction>> monthTransactions : dateGroupedTransactions.entrySet()) {
        	if (statisticsMonthModels.containsKey(monthTransactions.getKey())) {
	        	StatisticsCellModel cellModel = new StatisticsCellModel();
	        	cellModel.putAllTransactions(monthTransactions.getValue());
	        	statisticsMonthModels.get(monthTransactions.getKey()).addCellModel("  -- Maradék", cellModel);
        	}
        }
    }
    
    private void setCashSpent() {  	
    	for (Entry<LocalDate, StatisticsMonthModel> monthEntry : statisticsMonthModels.entrySet()) {
    		if (monthEntry.getValue().getPreviousMonthModel() == null || !monthEntry.getKey().isBefore(LocalDate.now().withDayOfMonth(1))) {
    			continue;
    		}
    		
    		int balanceDifference = monthEntry.getValue().getEndBalance() - monthEntry.getValue().getPreviousMonthModel().getEndBalance();
    		int allCorrections = monthEntry.getValue().getCellModels().entrySet().stream()
    				.filter(c -> !"Általános".equalsIgnoreCase(c.getKey()))
    				.flatMap(e -> e.getValue().getCorrections().stream()).mapToInt(Correction::getAmount).sum();
    		int allTransactions = monthEntry.getValue().getCellModels().entrySet().stream().flatMap(e -> e.getValue().getTransactions().stream()).mapToInt(AccountTransaction::getNotPairedAmount).sum();
    		int cashSpent = balanceDifference - allCorrections - allTransactions;
    		StatisticsCellModel cellModel = new StatisticsCellModel();
    		cellModel.putCorrection(new Correction.Builder()
    				.setAmount(cashSpent)
    				.setComment("Készpénzköltés")
    				.build());
    		monthEntry.getValue().addCellModel("  -- Készpénzköltés", cellModel);
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
    
    private void fillEmptyStatisticsModels() {
        Set<String> allCorrectionTypes = statisticsMonthModels.entrySet().stream().map(e -> e.getValue().getCellModels().entrySet()).flatMap(Collection::stream).map(Entry::getKey).distinct().collect(Collectors.toSet());
        
        for (String type : allCorrectionTypes) {
        	for (Entry<LocalDate, StatisticsMonthModel> monthEntry : statisticsMonthModels.entrySet()) {
        		Map<String, StatisticsCellModel> monthTypes = monthEntry.getValue().getCellModels();
        		if (!monthTypes.containsKey(type)) {
        			monthTypes.put(type, new StatisticsCellModel());
        		}
        	}
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

            List<Integer> amounts = statisticsMonthModels.entrySet().stream().filter(e -> e.getKey().plusMonths(AVERAGE_OF_MONTHS + 5l).isAfter(monthStatisticsEntry.getKey()) && !e.getKey().isAfter(monthStatisticsEntry.getKey()))
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
