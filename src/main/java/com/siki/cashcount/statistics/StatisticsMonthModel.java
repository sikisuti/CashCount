package com.siki.cashcount.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class StatisticsMonthModel {
	private Map<String, StatisticsCellModel> statisticsCellModels = new HashMap<>();
	private Integer monthEndBalance;
	private StatisticsMonthModel previousMonthModel;
	
	public StatisticsMonthModel(StatisticsMonthModel previousMonthModel) {
		this.previousMonthModel = previousMonthModel;
	}
	
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
	
	public void setEndBalance(int endBalance) {
		this.monthEndBalance = endBalance;
	}
	
	public Integer getEndBalance() {
		return monthEndBalance;
	}
	
	public void setPreviousMonthModel(StatisticsMonthModel previousMonthModel) {
		this.previousMonthModel = previousMonthModel;
	}
	
	public StatisticsMonthModel getPreviousMonthModel() {
		return previousMonthModel;
	}
}
