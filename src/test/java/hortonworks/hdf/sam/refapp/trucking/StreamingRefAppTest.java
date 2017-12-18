package hortonworks.hdf.sam.refapp.trucking;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import hortonworks.hdf.sam.sdk.testcases.manager.SAMTestCaseManager;
import hortonworks.hdf.sam.sdk.testcases.manager.SAMTestCaseManagerImpl;
import hortonworks.hdf.sam.sdk.testcases.model.SamTestComponent;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the SAM App called: "streaming-ref-app" found here: /sam-ref-app/trucking-app/artificacts
 * @author gvetticaden
 *
 */
public class StreamingRefAppTest {

	private static final String SAM_APP_NAME= "streaming-ref-app";
	private static final String SAM_REST_URL = "http://hdf-3-1-build3.field.hortonworks.com:7777/api/v1";	
	
	protected final Logger LOG = LoggerFactory.getLogger(StreamingRefAppTest.class);
			
	private SAMTestCaseManager samTestCaseManager = new SAMTestCaseManagerImpl(SAM_REST_URL);
	
	
	
	//@Test
	public void testNormalTruckingEvents() throws Exception {
		String testName = "Test-Normal-Event";
		Integer testTimeOutInSeconds = 200;
		Map<String, List<SamTestComponent>> testCaseExecutionResults = samTestCaseManager.runTestCase(SAM_APP_NAME, testName, testTimeOutInSeconds);	
		LOG.info(testCaseExecutionResults.toString());
		
		/* Validate the fields from the two streams were joined */
		assertThat(testCaseExecutionResults.get("JOIN").size(), is(1));
		SamTestComponent joinComponentResult = testCaseExecutionResults.get("JOIN").get(0);
		assertNotNull(joinComponentResult);	
		Map<String, String> joinFieldAndValues = joinComponentResult.getFieldsAndValues();
		
		String speedString = joinFieldAndValues.get("speed");
		assertNotNull(speedString);
		Long speedLong = Long.valueOf(speedString);
		assertThat(speedLong, is(58L));
		
		String latitudeString = joinFieldAndValues.get("latitude");
		assertNotNull(latitudeString);
		Double latDouble = Double.valueOf(latitudeString);
		assertThat(latDouble,  is(40.7));
		
		/* Validate the that the filter worked in that no  events were considered violaiton events */
		assertNull( testCaseExecutionResults.get("Filter"));		
	}
	
	///@Test
	public void testViolationTruckingEvents() throws Exception {
		String testName = "Test-Violation-Event";
		Integer testTimeOutInSeconds = 200;
		Map<String, List<SamTestComponent>> testCaseExecutionResults = samTestCaseManager.runTestCase(SAM_APP_NAME, testName, testTimeOutInSeconds);	
		LOG.info(testCaseExecutionResults.toString());
		
		
		/* Validate the fields from the two streams were joined */
		assertThat(testCaseExecutionResults.get("JOIN").size(), is(1));
		SamTestComponent joinComponentResult = testCaseExecutionResults.get("JOIN").get(0);
		assertNotNull(joinComponentResult);	
		Map<String, String> joinFieldAndValues = joinComponentResult.getFieldsAndValues();
		
		String speedString = joinFieldAndValues.get("speed");
		assertNotNull(speedString);
		Long speedLong = Long.valueOf(speedString);
		assertThat(speedLong, is(79L));
		
		String latitudeString = joinFieldAndValues.get("latitude");
		assertNotNull(latitudeString);
		Double latDouble = Double.valueOf(latitudeString);
		assertThat(latDouble,  is(41.62));
		
		/* Validate the that the filter worked in that and the violation event got passed to filter */
		assertNotNull( testCaseExecutionResults.get("Filter"));	
		assertThat(testCaseExecutionResults.get("Filter").size(), is(1));
		SamTestComponent filterResult = testCaseExecutionResults.get("Filter").get(0);
		Map<String, String> filterFieldAndValues = filterResult.getFieldsAndValues();
		assertNotNull(filterFieldAndValues);
		
		String eventType  = filterFieldAndValues.get("eventType");
		assertThat(eventType, is("Lane Departure"));
		assertThat(testCaseExecutionResults.get("DriverAvgSpeed").size(), is(1));
		SamTestComponent driverAvgSpeedComponent = testCaseExecutionResults.get("DriverAvgSpeed").get(0);
		assertNotNull(driverAvgSpeedComponent);
		Map<String, String> driverAvgSpeedFieldAndValues = driverAvgSpeedComponent.getFieldsAndValues();
		String avgSpeed = driverAvgSpeedFieldAndValues.get("speed_AVG");
		assertNotNull(avgSpeed);
		Double avgSpeedFromWindow = Double.valueOf(avgSpeed);
		assertThat(avgSpeedFromWindow, is(79.0));
		
		/** Validate that the speeding violation Filter is correct. Filter shoudl recognize this event as not speeding since it is < 80 */
		assertNull(testCaseExecutionResults.get("Speeding"));
		
	}	
	
	//@Test
	public void testMultipleSpeedingEventsEvents() throws Exception {
		String testName = "Multiple-Speeding-Events";
		Integer testTimeOutInSeconds = 200;
		Map<String, List<SamTestComponent>> testCaseExecutionResults = samTestCaseManager.runTestCase(SAM_APP_NAME, testName, testTimeOutInSeconds);	
		LOG.info(testCaseExecutionResults.toString());
		
		assertThat(testCaseExecutionResults.get("JOIN").size(), is(4));
		
		/* Validate the window function that calculates average speed */
		assertThat(testCaseExecutionResults.get("DriverAvgSpeed").size(), is(1));
		SamTestComponent driverAvgSpeedComponent = testCaseExecutionResults.get("DriverAvgSpeed").get(0);
		assertNotNull(driverAvgSpeedComponent);
		Map<String, String> driverAvgSpeedFieldAndValues = driverAvgSpeedComponent.getFieldsAndValues();
		String avgSpeed = driverAvgSpeedFieldAndValues.get("speed_AVG");
		assertNotNull(avgSpeed);
		Double avgSpeedFromWindow = Double.valueOf(avgSpeed);
		assertThat(avgSpeedFromWindow, is(89.5));
		
		/* Validate that the speeding violation Filter is correct. Filter shoudl recognize this event as  speeding since it is > 80 */
		assertThat(testCaseExecutionResults.get("Speeding").size(), is(1));
		assertNotNull(testCaseExecutionResults.get("Speeding").get(0));
		SamTestComponent speedingFilterComponent = testCaseExecutionResults.get("Speeding").get(0);
		Map<String, String> speedingFilterComponentFieldAndValues = speedingFilterComponent.getFieldsAndValues();
		assertNotNull(speedingFilterComponentFieldAndValues);
		
		/* Validate the projection rounded correctly */
		assertNotNull(testCaseExecutionResults.get("PROJECTION").get(0));
		SamTestComponent projectionComponent = testCaseExecutionResults.get("PROJECTION").get(0);
		Map<String, String> projectionComponentFieldAndValues = projectionComponent.getFieldsAndValues();
		assertNotNull(projectionComponentFieldAndValues);
		String speedAvgRoundString = projectionComponentFieldAndValues.get("speed_AVG_Round");
		assertNotNull(speedAvgRoundString);
		Long speedAvgRoundLong = Long.valueOf(speedAvgRoundString);
		assertThat(speedAvgRoundLong, is(90L));
		
	}	

}
