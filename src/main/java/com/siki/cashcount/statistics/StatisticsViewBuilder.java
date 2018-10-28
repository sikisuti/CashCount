package com.siki.cashcount.statistics;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Map;
import java.util.SortedMap;
import java.util.Map.Entry;

import com.siki.cashcount.config.ConfigManager;

public class StatisticsViewBuilder {
    public GridPane getStatisticsView(SortedMap<LocalDate, Map<String, StatisticsModel>> statisticsModels) {
        GridPane grid = new GridPane();

        int colCnt = 0;

        for (Entry<LocalDate, Map<String, StatisticsModel>> monthEntry : statisticsModels.entrySet()) {
            colCnt++;
            LocalDate date = monthEntry.getKey();
            if (date.plusYears(1).isBefore(LocalDate.now().withDayOfMonth(1))) {
                continue;
            }

            GridPane headerBg = new GridPane();
            headerBg.setPrefSize(100, 30);
            headerBg.setAlignment(Pos.CENTER);
            Label colHeader = new Label(date.getYear() + "." + date.getMonthValue() + ".");
            colHeader.setStyle("-fx-font-weight: bold;");
            if (date.isEqual(LocalDate.now().withDayOfMonth(1))) {
                headerBg.setBorder(new Border(new BorderStroke(Color.BLACK, Color.GRAY, Color.TRANSPARENT, Color.BLACK, 
                        BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, 
                        CornerRadii.EMPTY, new BorderWidths(1, 2, 0, 1), Insets.EMPTY)));
                headerBg.setAlignment(Pos.TOP_CENTER);                        
                headerBg.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
            }

            headerBg.getChildren().add(colHeader);
            GridPane.setColumnIndex(headerBg, colCnt);
            GridPane.setRowIndex(headerBg, 0);
            grid.getChildren().add(headerBg);

            for (Entry<String, StatisticsModel> categoryEntry : monthEntry.getValue().entrySet()) {
            	String category = categoryEntry.getKey();
            	StatisticsModel actStatisticModel = categoryEntry.getValue();
                int rowNo = 0;
                try {
                    rowNo = ConfigManager.getIntegerProperty(category);
                } catch (NumberFormatException ex) {
                    continue;
                }

                if (colCnt == 1) {
                    Label rowHeader = new Label(category);
                    rowHeader.setMinWidth(150);
                    rowHeader.setPrefWidth(150);
                    rowHeader.setMaxWidth(150);
                    if (!category.startsWith("  -- ")) rowHeader.setStyle("-fx-font-weight: bold;");
                    GridPane.setColumnIndex(rowHeader, colCnt - 1);
                    GridPane.setRowIndex(rowHeader, rowNo);
                    grid.getChildren().add(rowHeader);
                }

                GridPane cell = new GridPane();
                cell.setPrefSize(100, 30);
                cell.setAlignment(Pos.CENTER_RIGHT);
                Integer value;
                Label lblValue;
                value = categoryEntry.getValue().getAmount();
                lblValue = new Label(NumberFormat.getCurrencyInstance().format(value));
                StringBuilder tooltipBuilder = new StringBuilder(categoryEntry.getValue().getDetails());
                if (categoryEntry.getValue().getAverage() != null) {
                    tooltipBuilder.append("\nÉves átlag: " + NumberFormat.getCurrencyInstance().format(categoryEntry.getValue().getAverage()));
                }

                Tooltip tt = new Tooltip(tooltipBuilder.toString());
                lblValue.setTooltip(tt);

                if (date.isEqual(LocalDate.now().withDayOfMonth(1))) {
                    cell.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, Color.GRAY, Color.TRANSPARENT, Color.BLACK, 
                            BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, 
                            CornerRadii.EMPTY, new BorderWidths(0, 2, 0, 1), Insets.EMPTY)));
                    cell.setAlignment(Pos.TOP_RIGHT);                        
                    lblValue.setStyle("-fx-font-weight: bold;");
                }
                cell.getChildren().add(lblValue);
                double opacity;
                Color bgColor;
                // Instead of considering the diff between value and 0 I consider the diff between value and average
                /*
                double inLowerBound = ConfigManager.getDoubleProperty("IncomeDecoratorLowerBound");
                double inUpperBound = ConfigManager.getDoubleProperty("IncomeDecoratorUpperBound");
                double outLowerBound = ConfigManager.getDoubleProperty("OutcomeDecoratorLowerBound");
                double outUpperBound = ConfigManager.getDoubleProperty("OutcomeDecoratorUpperBound");
                */
                double diffBound = ConfigManager.getDoubleProperty("DifferenceDecoratorBound");

                if (actStatisticModel.getAverage() != null &&
                        actStatisticModel.getPreviousStatisticsModel() != null && actStatisticModel.getPreviousStatisticsModel().getAverage() != null &&
                        actStatisticModel.getPreviousStatisticsModel().getPreviousStatisticsModel() != null && actStatisticModel.getPreviousStatisticsModel().getPreviousStatisticsModel().getAverage() != null) {
                    int actAverageDelta = actStatisticModel.getAverage() - actStatisticModel.getPreviousStatisticsModel().getAverage();
                    int previousAverageDelta = actStatisticModel.getPreviousStatisticsModel().getAverage() - actStatisticModel.getPreviousStatisticsModel().getPreviousStatisticsModel().getAverage();
                    int diff = actAverageDelta - previousAverageDelta;
                    opacity = Math.abs(diff / diffBound);
                    if (opacity > 1) {
                        opacity = 1;
                    }

                    bgColor = diff > 0 ? Color.rgb(0, 200, 0, opacity) : Color.rgb(230, 0, 0, opacity);
                    cell.setBackground(new Background(new BackgroundFill(bgColor, CornerRadii.EMPTY, Insets.EMPTY)));
                }
                GridPane.setColumnIndex(cell, colCnt);
                GridPane.setRowIndex(cell, rowNo);
                grid.getChildren().add(cell);
            }
        }

        return grid;
    }
}
