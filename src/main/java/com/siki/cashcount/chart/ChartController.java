package com.siki.cashcount.chart;

import com.siki.cashcount.control.DateHelper;
import com.siki.cashcount.data.DataManager;
import com.siki.cashcount.exception.JsonDeserializeException;
import com.siki.cashcount.model.DailyBalance;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ChartController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChartController.class);

    public static CashFlowChart flowChart = new CashFlowChart();

    private static LineChart.Series<Date, Integer> savingSeries = new LineChart.Series<>();
    private static LineChart.Series<Date, Integer> cashSeries = new LineChart.Series<>();
    private static LineChart.Series<Date, Integer> accountSeries = new LineChart.Series<>();

    private static Double maxUpperBound;
    private static Double YDistance;

    static {
        savingSeries.setName("Lekötések");
        cashSeries.setName("Készpénz");
        accountSeries.setName("Számla");
    }

    public static void refreshChart() {
        List<DailyBalance> series;
        try {
            series = DataManager.getInstance().getAllDailyBalances().stream()
                    .filter(db -> !db.getDate().plusYears(1).isBefore(LocalDate.now().withDayOfMonth(1))).collect(Collectors.toList());
        } catch (IOException | JsonDeserializeException e) {
            LOGGER.error("", e);
            return;
        }

        flowChart.getData().clear();
        savingSeries.getData().clear();
        cashSeries.getData().clear();
        accountSeries.getData().clear();

        series.forEach(db -> {
            Date date = DateHelper.toDate(db.getDate());
            Integer yValue = db.getTotalSavings();
            savingSeries.getData().add(new XYChart.Data(date, yValue));
            yValue = yValue + db.getCash();
            cashSeries.getData().add(new XYChart.Data(date, yValue));
            yValue = yValue + db.getBalance();
            accountSeries.getData().add(new XYChart.Data(date, yValue));
        });

        int max = accountSeries.getData().stream().mapToInt(s -> s.getYValue()).max().getAsInt();
        int min = series.stream().filter(s -> s.isPredicted()).mapToInt(s -> s.getTotalSavings() + s.getTotalMoney()).min().getAsInt();

        maxUpperBound = Math.ceil(max / 100000d) * 100000;
        double lowerBound = Math.floor((min - 350000) / 100000d) * 100000;
        double upperBound = lowerBound + 2000000;
        YDistance = upperBound - lowerBound;

        flowChart.getYAxis().setUpperBound(upperBound);
        flowChart.getYAxis().setLowerBound(lowerBound);

        flowChart.getXAxis().setLowerBound(Date.from(series.get(0).getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        flowChart.getXAxis().setUpperBound(Date.from(series.get(series.size() - 1).getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        flowChart.getData().addAll(savingSeries, cashSeries, accountSeries);
    }

    public static void scroll(double amount) {
        double min = flowChart.getYAxis().getLowerBound();
        double max = flowChart.getYAxis().getUpperBound();
        if (amount < 0) {
            min += amount;
            if (min < 0) {
                min = 0;
            }

            max = min + YDistance;
        } else {
            max += amount;
            if (max > maxUpperBound) {
                max = maxUpperBound;
            }

            min = max - YDistance;
        }

        flowChart.getYAxis().setLowerBound(min);
        flowChart.getYAxis().setUpperBound(max);
    }
}
