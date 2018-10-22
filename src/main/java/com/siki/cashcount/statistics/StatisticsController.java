package com.siki.cashcount.statistics;

import com.siki.cashcount.control.DailyBalancesTitledPane;
import com.siki.cashcount.data.DataManager;
import com.siki.cashcount.model.Correction;
import com.siki.cashcount.model.DailyBalance;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsController {
    public static List<StatisticsModel> getStatistics(Pane monthlyGrouppedDailyBalances) {
        List<StatisticsModel> statisticsModels = new ArrayList<>();

        /*for (Node node : monthlyGrouppedDailyBalances.getChildren()) {
            if (node.getClass() != DailyBalancesTitledPane.class) {
                continue;
            }

            DailyBalancesTitledPane entry = (DailyBalancesTitledPane)node;

            TreeMap<String, Map.Entry<Integer, String>> monthCorrectionData = getStatisticsFromCorrections(entry.getPeriod().getYear(), entry.getPeriod().getMonth());
            TreeMap<String, Map.Entry<Integer, String>> monthTransactionData = DataManager.getInstance().getStatisticsFromTransactions(entry.getPeriod().getYear(), entry.getPeriod().getMonth());
            Integer allTransactionAmount = 0;
            for (Map.Entry<String, Map.Entry<Integer, String>> transactionEntry : monthTransactionData.entrySet()) {
                allTransactionAmount += transactionEntry.getValue().getKey();
            }

            if (monthCorrectionData.containsKey(DataManager.GENERAL_TEXT) && LocalDate.of(entry.getPeriod().getYear(), entry.getPeriod().getMonthValue(), 1).isBefore(LocalDate.now().withDayOfMonth(1))) {
                int cashSpent = monthCorrectionData.get(DataManager.GENERAL_TEXT).getKey() - allTransactionAmount;
                if (cashSpent != 0)
                    monthTransactionData.put("  -- Készpénzköltés", new AbstractMap.SimpleEntry<>(cashSpent, "Költés készpénzből"));
            }

            monthCorrectionData.putAll(monthTransactionData);
            data.put(entry.getPeriod(), monthCorrectionData);
        }*/

        return statisticsModels;
    }
}
