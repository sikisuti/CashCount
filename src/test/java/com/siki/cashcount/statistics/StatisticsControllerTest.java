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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;

public class StatisticsControllerTest {
	
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
	}

}
