package com.siki.cashcount.statistics;

import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.siki.cashcount.data.DataManager;
import com.siki.cashcount.exception.JsonDeserializeException;

import javafx.collections.FXCollections;

import java.io.IOException;
import java.util.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsControllerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsControllerTest.class);
	
	StatisticsController statisticsController;

	/*@Mock
	DataManager dataManager;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();*/

	@Before
	public void setUp() throws IOException, JsonDeserializeException {
		statisticsController = new StatisticsController();
		/*dataManager = mock(DataManager.class);
		when(dataManager.getAllDailyBalances()).thenReturn(FXCollections.observableArrayList());*/
	}

	@Test
	public void getStatisticsTest() throws IOException, JsonDeserializeException {
		//statisticsController.getStatistics();
		Assert.assertEquals(true, true);

		double n = 0.5;
		LOGGER.info("1: " + Math.pow(1, n));
		LOGGER.info("10: " + Math.pow(10, n));
		LOGGER.info("100: " + Math.pow(100, n));
		LOGGER.info("1 000: " + Math.pow(1000, n));
		LOGGER.info("10 000: " + Math.pow(10000, n));
		LOGGER.info("100 000: " + Math.pow(100000, n));

        n = 0.7;
        LOGGER.info("1: " + Math.pow(1, n));
        LOGGER.info("10: " + Math.pow(10, n));
        LOGGER.info("100: " + Math.pow(100, n));
        LOGGER.info("1 000: " + Math.pow(1000, n));
        LOGGER.info("10 000: " + Math.pow(10000, n));
        LOGGER.info("100 000: " + Math.pow(100000, n));

        n = 0.9;
        LOGGER.info("1: " + Math.pow(1, n));
        LOGGER.info("10: " + Math.pow(10, n));
        LOGGER.info("100: " + Math.pow(100, n));
        LOGGER.info("1 000: " + Math.pow(1000, n));
        LOGGER.info("10 000: " + Math.pow(10000, n));
        LOGGER.info("100 000: " + Math.pow(100000, n));
	}
}
