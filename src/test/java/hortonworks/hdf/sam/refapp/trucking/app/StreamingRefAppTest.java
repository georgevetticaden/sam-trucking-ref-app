package hortonworks.hdf.sam.refapp.trucking.app;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hortonworks.hdf.sam.refapp.trucking.BaseTest;
import hortonworks.hdf.sam.refapp.trucking.deploy.AppPropertiesConstants;
import hortonworks.hdf.sam.sdk.testcases.manager.SAMTestCaseManager;
import hortonworks.hdf.sam.sdk.testcases.manager.SAMTestCaseManagerImpl;
import hortonworks.hdf.sam.sdk.testcases.model.SamTestComponent;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Tests the SAM App called: "streaming-ref-app" found here: /sam-ref-app/trucking-app/artificacts
 * @author gvetticaden
 *
 */
public class StreamingRefAppTest extends BaseTest{

	
	private static final String SAM_APP_NAME = "streaming-ref-app";
	
	private static final String TEST_1_NORMAL_EVENT_TEST_CASE = "Test-Normal-Event";
	private static final String TEST_1_SPEED_STREAM_TEST_DATA = "test-cases-source-data/normal-event-test/speed-stream-test-data.json";
	private static final String TEST_1_GEO_STREAM_TEST_DATA = "test-cases-source-data/normal-event-test/geo-stream-test-data.json";
	
	
	private static final String TEST_2_TEST_VIOLATION_EVENT_TEST_CASE = "Test-Violation-Event";
	private static final String TEST_2_SPEED_STREAM_TEST_DATA = "test-cases-source-data/violation-event-test/speed-stream-test-data.json";
	private static final String TEST_2_GEO_STREAM_TEST_DATA = "test-cases-source-data/violation-event-test/geo-stream-test-data.json";	
	
	private static final String TEST_3_TEST_VIOLATION_EVENT_TEST_CASE = "Multiple-Speeding-Events";
	private static final String TEST_3_SPEED_STREAM_TEST_DATA = "test-cases-source-data/multiple-speeding-event-test/speed-stream-test-data.json";
	private static final String TEST_3_GEO_STREAM_TEST_DATA = "test-cases-source-data/multiple-speeding-event-test/geo-stream-test-data.json";		
	
	

	
	public StreamingRefAppTest() {
		loadAppPropertiesFile();
	}
	
	@Before
	/**
	 * For each test does the following:
	 * 	1. Import the SAM App you want to test
	 *  2. Create the TEst Case
	 */
	public void setup() {
		Resource appResource = new ClassPathResource(AppPropertiesConstants.SAM_REF_APP_FILE_LOCATION);
		LOG.info("Deleting App["+SAM_APP_NAME + "] if it exists");
		deleteSAMApp(SAM_APP_NAME);
		LOG.info("Importing App["+SAM_APP_NAME + "] if it exists");
		importSAMApp(SAM_APP_NAME, appResource);
		
	}
	
	
	
	@After
	public void tearDown() {
		deleteSAMApp(SAM_APP_NAME);
	}
	
	
	
	@Test
	public void testNormalTruckingEvents() throws Exception {
		String testName = TEST_1_NORMAL_EVENT_TEST_CASE;
		
		createNormalEventTestCase();
		
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
	
	@Test
	public void testViolationTruckingEvents() throws Exception {
		String testName = TEST_2_TEST_VIOLATION_EVENT_TEST_CASE;
		
		createViolationEventTestCase();
		
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
	


	@Test
	public void testMultipleSpeedingEvents() throws Exception {
		String testName = TEST_3_TEST_VIOLATION_EVENT_TEST_CASE;
		createMultipleSpeedingEventsTestCase();
		
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
	


	public void createNormalEventTestCase() {

		/* Create map of test data for each source in the app */
		Map<String, Resource> testDataForSources = new HashMap<String, Resource>();
		Resource geoStreamTestData = createClassPathResource(TEST_1_GEO_STREAM_TEST_DATA);	
		testDataForSources.put("GeoStream", geoStreamTestData);
		
		Resource speedStreamTestData = createClassPathResource(TEST_1_SPEED_STREAM_TEST_DATA);	
		testDataForSources.put("SpeedStream", speedStreamTestData);
		
		createTestCase(SAM_APP_NAME, TEST_1_NORMAL_EVENT_TEST_CASE, testDataForSources);
		

	}		
	
	private void createViolationEventTestCase() {
		/* Create map of test data for each source in the app */
		Map<String, Resource> testDataForSources = new HashMap<String, Resource>();
		Resource geoStreamTestData = createClassPathResource(TEST_2_GEO_STREAM_TEST_DATA);	
		testDataForSources.put("GeoStream", geoStreamTestData);
		
		Resource speedStreamTestData = createClassPathResource(TEST_2_SPEED_STREAM_TEST_DATA);	
		testDataForSources.put("SpeedStream", speedStreamTestData);
		
		createTestCase(SAM_APP_NAME, TEST_2_TEST_VIOLATION_EVENT_TEST_CASE, testDataForSources);
		
	}	
	
	private void createMultipleSpeedingEventsTestCase() {
		/* Create map of test data for each source in the app */
		Map<String, Resource> testDataForSources = new HashMap<String, Resource>();
		Resource geoStreamTestData = createClassPathResource(TEST_3_GEO_STREAM_TEST_DATA);	
		testDataForSources.put("GeoStream", geoStreamTestData);
		
		Resource speedStreamTestData = createClassPathResource(TEST_3_SPEED_STREAM_TEST_DATA);	
		testDataForSources.put("SpeedStream", speedStreamTestData);
		
		createTestCase(SAM_APP_NAME, TEST_3_TEST_VIOLATION_EVENT_TEST_CASE, testDataForSources);
		
	}	
	

}
