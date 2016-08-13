/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siki.cashcount.control;

import extfx.scene.chart.DateAxis;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.shape.Line;
import javafx.util.StringConverter;

/**
 *
 * @author tamas.siklosi
 */
public final class CashFlowChart extends LineChart {
    
    Data<Date, Integer> nowLine;
    
    public CashFlowChart() {
        super(new DateAxis(), new NumberAxis());
        
        this.getXAxis().setLabel("Dátum");
        this.getXAxis().setAutoRanging(false);
        this.getXAxis().setLowerBound(new Date());
        this.getXAxis().setUpperBound(new Date());
        this.getXAxis().setTickLabelFormatter(new StringConverter<Date>() {
            @Override
            public String toString(Date object) {
                return new SimpleDateFormat("MMMM").format(object);
            }

            @Override
            public Date fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        
        this.getYAxis().setLabel("Összeg");
        this.getYAxis().setAutoRanging(false);
        this.getYAxis().setLowerBound(0);
        this.getYAxis().setUpperBound(0);
        this.getYAxis().setTickUnit(100000);
        
        this.setTitle("Cash Flow");
        this.setCreateSymbols(false);
        this.setPadding(new Insets(20d));
        
        nowLine = new Data<>(DateHelper.toDate(LocalDate.now()), 0);
        Line line = new Line();
        nowLine.setNode(line);
        getPlotChildren().add(line);
        setMaxHeight(Double.MAX_VALUE);
        setPrefHeight(900);
        this.setAnimated(false);
    }

    @Override
    public NumberAxis getYAxis() {
        return (NumberAxis)super.getYAxis();
    }

    @Override
    public DateAxis getXAxis() {
        return (DateAxis)super.getXAxis();
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        Line line = (Line) nowLine.getNode();
        line.setStartX(getXAxis().getDisplayPosition(nowLine.getXValue()));
        line.setEndX(line.getStartX());
        line.setStartY(0d);
        line.setEndY(getBoundsInLocal().getHeight());
        line.toFront();
    }
}
