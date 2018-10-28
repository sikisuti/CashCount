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
	private static final String HEADER_STYLE = "-fx-font-weight: bold;";
	
    public GridPane getStatisticsView(SortedMap<LocalDate, Map<String, StatisticsModel>> statisticsModels) {
        GridPane grid = new GridPane();

        int colCnt = 0;

        for (Entry<LocalDate, Map<String, StatisticsModel>> monthEntry : statisticsModels.entrySet()) {
            LocalDate date = monthEntry.getKey();
            if (date.plusYears(1).isBefore(LocalDate.now().withDayOfMonth(1))) {
                continue;
            }

        	colCnt++;
            buildMonthEntry(monthEntry, grid, colCnt);
        }

        return grid;
    }
    
    private void buildMonthEntry(Entry<LocalDate, Map<String, StatisticsModel>> monthEntry, GridPane grid, int colCnt) {
    	LocalDate date = monthEntry.getKey();
        
    	addMonthHeader(date, grid, colCnt);

        for (Entry<String, StatisticsModel> categoryEntry : monthEntry.getValue().entrySet()) {
        	addCategory(categoryEntry, grid, date, colCnt);
        }
    }
    
    private void addMonthHeader(LocalDate date, GridPane grid, int colCnt) {
    	GridPane headerBg = new GridPane();
        headerBg.setPrefSize(100, 30);
        headerBg.setAlignment(Pos.CENTER);
        Label colHeader = new Label(date.getYear() + "." + date.getMonthValue() + ".");
        colHeader.setStyle(HEADER_STYLE);
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
    }
    
    private void addCategory(Entry<String, StatisticsModel> categoryEntry, GridPane grid, LocalDate date, int colCnt) {
    	String category = categoryEntry.getKey();
    	try {
    		int rowNo = ConfigManager.getIntegerProperty(category);

	        if (colCnt == 1) {
	            addRowHeader(category, colCnt, rowNo, grid);
	        }
	
	        addStatisticsCell(categoryEntry, date, grid, colCnt, rowNo);
    	} catch (NumberFormatException e) {
		}
    }
    
    private void addRowHeader(String category, int colCnt, int rowNo, GridPane grid) {
    	Label rowHeader = new Label(category);
        rowHeader.setMinWidth(150);
        rowHeader.setPrefWidth(150);
        rowHeader.setMaxWidth(150);
        if (!category.startsWith("  -- ")) rowHeader.setStyle(HEADER_STYLE);
        GridPane.setColumnIndex(rowHeader, colCnt - 1);
        GridPane.setRowIndex(rowHeader, rowNo);
        grid.getChildren().add(rowHeader);
    }
    
    private void addStatisticsCell(Entry<String, StatisticsModel> categoryEntry, LocalDate date, GridPane grid, int colCnt, int rowNo) {
    	StatisticsModel actStatisticModel = categoryEntry.getValue();
    	GridPane cell = new GridPane();
        Integer value;
        Label lblValue;
        
        value = categoryEntry.getValue().getAmount();
        lblValue = new Label(NumberFormat.getCurrencyInstance().format(value));        
        setCellStyle(cell, lblValue, date);
        addToolTip(categoryEntry, lblValue);        
        cell.getChildren().add(lblValue);
        
        setCellColoring(actStatisticModel, cell);
        
        GridPane.setColumnIndex(cell, colCnt);
        GridPane.setRowIndex(cell, rowNo);
        grid.getChildren().add(cell);
    }
    
    private void setCellStyle(GridPane cell, Label lblValue, LocalDate date) {
        cell.setPrefSize(100, 30);
        cell.setAlignment(Pos.CENTER_RIGHT);    
        if (date.isEqual(LocalDate.now().withDayOfMonth(1))) {
            cell.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, Color.GRAY, Color.TRANSPARENT, Color.BLACK, 
                    BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, 
                    CornerRadii.EMPTY, new BorderWidths(0, 2, 0, 1), Insets.EMPTY)));
            cell.setAlignment(Pos.TOP_RIGHT);                        
            lblValue.setStyle(HEADER_STYLE);
        }
    }
    
    private void addToolTip(Entry<String, StatisticsModel> categoryEntry, Label lblValue) {
    	StringBuilder tooltipBuilder = new StringBuilder(categoryEntry.getValue().getDetails());
        if (categoryEntry.getValue().getAverage() != null) {
            tooltipBuilder.append("\nÉves átlag: " + NumberFormat.getCurrencyInstance().format(categoryEntry.getValue().getAverage()));
        }

        Tooltip tt = new Tooltip(tooltipBuilder.toString());
        lblValue.setTooltip(tt);
    }
    
    private void setCellColoring(StatisticsModel actStatisticModel, GridPane cell) {
    	double opacity;
        Color bgColor;
        double diffBound = ConfigManager.getDoubleProperty("DifferenceDecoratorBound");

        if (actStatisticModel.getAverage() != null &&
                actStatisticModel.getPreviousStatisticsModel() != null && actStatisticModel.getPreviousStatisticsModel().getAverage() != null &&
                actStatisticModel.getPreviousStatisticsModel().getPreviousStatisticsModel() != null && actStatisticModel.getPreviousStatisticsModel().getPreviousStatisticsModel().getAverage() != null) {
            int actAverageDelta = actStatisticModel.getAverage() - actStatisticModel.getPreviousStatisticsModel().getAverage();
            int previousAverageDelta = actStatisticModel.getPreviousStatisticsModel().getAverage() - actStatisticModel.getPreviousStatisticsModel().getPreviousStatisticsModel().getAverage();
            int diff = actAverageDelta - previousAverageDelta;
            opacity = Math.abs(diff / diffBound);
            if (opacity < 0.2) {
                opacity = 0;
            } else if (opacity < 0.5) {
                opacity = 0.2;
            } else if (opacity < 1) {
                opacity = 0.5;
            } else {
                opacity = 1;
            }

            bgColor = diff > 0 ? Color.rgb(0, 200, 0, opacity) : Color.rgb(230, 0, 0, opacity);
            cell.setBackground(new Background(new BackgroundFill(bgColor, CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }
}
