package com.siki.cashcount.statistics;

import com.siki.cashcount.data.DataManager;
import com.siki.cashcount.exception.JsonDeserializeException;
import com.siki.cashcount.model.Correction;
import com.siki.cashcount.model.DailyBalance;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class StatisticsController {
    private SortedMap<LocalDate, Map<String, StatisticsModel>> statisticsModels = new TreeMap<>();
    Set<String> allCorrectionTypes = new HashSet<>();
    private int lastTotalAmount;

    public SortedMap<LocalDate, Map<String, StatisticsModel>> getStatistics() throws IOException, JsonDeserializeException {
        List<DailyBalance> dailyBalances =  DataManager.getInstance().getAllDailyBalances();
        for (DailyBalance dailyBalance : dailyBalances) {
            parseDailyBalance(dailyBalance);
        }

        return statisticsModels;
    }

    private void parseDailyBalance(DailyBalance dailyBalance) {
        Map<String, StatisticsModel> monthStatistics = getMonthStatistics(dailyBalance.getDate());
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
}
