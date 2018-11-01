package com.siki.cashcount.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class StatisticsMonthModel {
	private Map<String, StatisticsCellModel> statisticsCellModels = new HashMap<>();
	private Integer monthEndBalance;
	private StatisticsMonthModel previousMonthModel;
	
	public void addCellModel(String type, StatisticsCellModel cellModel) {
		statisticsCellModels.put(type, cellModel);
	}
	
	public void addAllCellModels(Map<String, StatisticsCellModel> cellModels) {
		for (Entry<String, StatisticsCellModel> cellModelEntry : cellModels.entrySet()) {
			statisticsCellModels.put(cellModelEntry.getKey(), cellModelEntry.getValue());
		}
	}
	
	public Map<String, StatisticsCellModel> getCellModels() {
		return statisticsCellModels;
	}
}
