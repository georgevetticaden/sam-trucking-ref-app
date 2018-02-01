package hortonworks.hdf.sam.refapp.trucking.app;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import hortonworks.hdf.sam.refapp.trucking.PropertiesConstants;
import hortonworks.hdf.sam.refapp.trucking.BaseTest;
import hortonworks.hdf.sam.sdk.app.manager.SAMAppManagerImpl;
import hortonworks.hdf.sam.sdk.environment.manager.SAMEnvironmentManagerImpl;
import hortonworks.hdf.sam.sdk.servicepool.manager.SAMServicePoolManagerImpl;
import hortonworks.hdf.sam.sdk.testcases.manager.SAMTestCaseManagerImpl;
import hortonworks.hdf.sam.sdk.testcases.model.SamTestComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
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
public class TruckingRefAdvancedAppTest extends BaseTest{

	
	private static final String SAM_APP_NAME = "streaming-ref-advacned-app-junit";
	
	private static Logger LOG = LoggerFactory.getLogger(TruckingRefAdvancedAppTest.class);
	
			
	
	static {
		loadAppPropertiesFile("/app-properties/junit-trucking-ref-app-advanced.properties");
		String samRestUrl = appProperties.getProperty(PropertiesConstants.SAM_REST_URL);
		samAppManager = new SAMAppManagerImpl(samRestUrl);
		samTestCaseManager = new SAMTestCaseManagerImpl(samRestUrl);
		samServicePoolManager = new SAMServicePoolManagerImpl(samRestUrl);
		samEnvironmentManager = new SAMEnvironmentManagerImpl(samRestUrl);
	}	
	
	/**
	 * For each test does the following:
	 * 	1. Import the SAM App you want to test
	 *  2. Create the TEst Case
	 */
	@BeforeClass
	public static void setup() {
		
		LOG.info("Setup for Test Started");
		
		LOG.info("Creating SAM ServicePools");
		createServicePools();
		
		LOG.info("Creating SAM Environments");
		createEnv();
		
		LOG.info("Importing App["+SAM_APP_NAME + "]");
		Resource appResource = new ClassPathResource(PropertiesConstants.SAM_REF_APP_ADVANCE_FILE_LOCATION);
		importSAMApp(SAM_APP_NAME, appResource);
		
		LOG.info("Setup for Test Completed");
	}
	

	@AfterClass
	public static void tearDown() {
		LOG.info("Teardown for Test Started");
		
		LOG.info("Deleting  SAM App[" + SAM_APP_NAME + "]");
		deleteSAMApp(SAM_APP_NAME);
		
		LOG.info("Deleting  SAM Envs");
		deleteEnv();
		
		LOG.info("Deleting  SAM Service Pools");
		deleteServicePools();
		
		LOG.info("Teardown for Test Completed");
	}
	
	
	
	@Test
	public void testNormalEventNoViolationPrediction() throws Exception {
		String testName = PropertiesConstants.TEST_1_NORMAL_EVENT_NO_PREDICTION_TEST_CASE;
		
		createTestCase(SAM_APP_NAME ,testName, PropertiesConstants.TEST_1_GEO_STREAM_TEST_DATA, PropertiesConstants.TEST_1_SPEED_STREAM_TEST_DATA);
		
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
		
		
		//Validate the non-violation event goes to the split component and the split fields are correct (week, splitJoinValue)
		String expectedSplitJoinValue = "1506437684451";
		assertNotNull(testCaseExecutionResults.get("Split"));
		assertThat(testCaseExecutionResults.get("Split").size(), is(1));
		SamTestComponent splitComponent = testCaseExecutionResults.get("Split").get(0);
		Map<String, String> splitComponentFieldAndValues = splitComponent.getFieldsAndValues();
		assertThat(splitComponentFieldAndValues.get("splitJoinValue"), is(expectedSplitJoinValue));
		assertThat(splitComponentFieldAndValues.get("week"), is("39"));
		
		//Validate the HR Enrichment values
		assertNotNull(testCaseExecutionResults.get("ENRICH-HR"));
		assertThat(testCaseExecutionResults.get("ENRICH-HR").size(), is(1));
		SamTestComponent HREnrichComponent = testCaseExecutionResults.get("ENRICH-HR").get(0);
		Map<String, String> hrEnrichmentComponentFieldAndValues = HREnrichComponent.getFieldsAndValues();
		String driverCert = "Y";
		String driverWagePlan = "hours";
		assertThat(hrEnrichmentComponentFieldAndValues.get("driverCertification"), is(driverCert));
		assertThat(hrEnrichmentComponentFieldAndValues.get("driverWagePlan"), is(driverWagePlan));
		
		//Validate the Timesheet Enrichment values
		assertNotNull(testCaseExecutionResults.get("ENRICH-Timesheet"));
		assertThat(testCaseExecutionResults.get("ENRICH-Timesheet").size(), is(1));
		SamTestComponent timeSheetEnrichComponent = testCaseExecutionResults.get("ENRICH-Timesheet").get(0);
		Map<String, String>timeSheetrEnrichmentComponentFieldAndValues = timeSheetEnrichComponent.getFieldsAndValues();
		String driverFatigueByHours = "48";
		String driverFatigueByMiles = "2796";
		assertThat(timeSheetrEnrichmentComponentFieldAndValues.get("driverFatigueByHours"), is(driverFatigueByHours));
		assertThat(timeSheetrEnrichmentComponentFieldAndValues.get("driverFatigueByMiles"), is(driverFatigueByMiles));	
		
		//Validate the Weather Enrichment values
		
		assertNotNull(testCaseExecutionResults.get("ENRICH-WEATHER"));
		assertThat(testCaseExecutionResults.get("ENRICH-WEATHER").size(), is(1));
		SamTestComponent weatherEnrichComponent = testCaseExecutionResults.get("ENRICH-WEATHER").get(0);
		Map<String, String>weatherEnrichmentComponentFieldAndValues = weatherEnrichComponent.getFieldsAndValues();
	
		assertNotNull(weatherEnrichmentComponentFieldAndValues.get("Model_Feature_FoggyWeather"));
		assertNotNull(weatherEnrichmentComponentFieldAndValues.get("Model_Feature_RainyWeather"));	
		assertNotNull(weatherEnrichmentComponentFieldAndValues.get("Model_Feature_WindyWeather"));	
	

		//Validate the joins of the three enrichments
		assertNotNull(testCaseExecutionResults.get("JOIN-ENRICHMENTS"));
		assertThat(testCaseExecutionResults.get("JOIN-ENRICHMENTS").size(), is(1));
		SamTestComponent joinEnrichComponent = testCaseExecutionResults.get("JOIN-ENRICHMENTS").get(0);
		Map<String, String>joinEnrichmentComponentFieldAndValues = joinEnrichComponent.getFieldsAndValues();
		assertThat(joinEnrichmentComponentFieldAndValues.get("splitJoinValue"), is(expectedSplitJoinValue));
		assertThat(joinEnrichmentComponentFieldAndValues.get("driverCertification"), is(driverCert));
		assertThat(joinEnrichmentComponentFieldAndValues.get("driverWagePlan"), is(driverWagePlan));
		assertThat(joinEnrichmentComponentFieldAndValues.get("driverFatigueByHours"), is(driverFatigueByHours));
		assertThat(joinEnrichmentComponentFieldAndValues.get("driverFatigueByMiles"), is(driverFatigueByMiles));
		assertNotNull(joinEnrichmentComponentFieldAndValues.get("Model_Feature_FoggyWeather"));
		assertNotNull(joinEnrichmentComponentFieldAndValues.get("Model_Feature_RainyWeather"));
		assertNotNull(joinEnrichmentComponentFieldAndValues.get("Model_Feature_WindyWeather"));
		

		//Validate the the Normalized values
		assertNotNull(testCaseExecutionResults.get("NORMALIZE-MODEL-FEATURES"));
		assertThat(testCaseExecutionResults.get("NORMALIZE-MODEL-FEATURES").size(), is(1));
		SamTestComponent normalizeModelComponent = testCaseExecutionResults.get("NORMALIZE-MODEL-FEATURES").get(0);
		Map<String, String>normlaizeModelComponentFieldAndValues = normalizeModelComponent.getFieldsAndValues();
		String driverCertNormalized = "1";
		String driverWagePlanNormalized = "0";
		String driverFatigueByMilesNormalized = "2.796";
		String driverFatigueByHoursNormalized = "0.48";
		assertThat(normlaizeModelComponentFieldAndValues.get("Model_Feature_Certification"), is(driverCertNormalized));
		assertThat(normlaizeModelComponentFieldAndValues.get("Model_Feature_WagePlan"), is(driverWagePlanNormalized));
		assertThat(normlaizeModelComponentFieldAndValues.get("Model_Feature_FatigueByHours"), is(driverFatigueByHoursNormalized));
		assertThat(normlaizeModelComponentFieldAndValues.get("Model_Feature_FatigueByMiles"), is(driverFatigueByMilesNormalized));
		assertNotNull(joinEnrichmentComponentFieldAndValues.get("Model_Feature_FoggyWeather"));
		assertNotNull(joinEnrichmentComponentFieldAndValues.get("Model_Feature_RainyWeather"));
		assertNotNull(joinEnrichmentComponentFieldAndValues.get("Model_Feature_WindyWeather"));
	}
			
	
	@Test
	public void testNormalEventYesViolationPrediction() throws Exception {
		String testName = PropertiesConstants.TEST_2_NORMAL_EVENT_YES_PREDICTION_TEST_CASE;
		
		createTestCase(SAM_APP_NAME ,testName, PropertiesConstants.TEST_2_GEO_STREAM_TEST_DATA, PropertiesConstants.TEST_2_SPEED_STREAM_TEST_DATA);
		
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
		assertThat(speedLong, is(60L));
		
		String latitudeString = joinFieldAndValues.get("latitude");
		assertNotNull(latitudeString);
		Double latDouble = Double.valueOf(latitudeString);
		assertThat(latDouble,  is(38.64));
		
		
		//Validate the non-violation event goes to the split component and the split fields are correct (week, splitJoinValue)
		String expectedSplitJoinValue = "1506437684451";
		assertNotNull(testCaseExecutionResults.get("Split"));
		assertThat(testCaseExecutionResults.get("Split").size(), is(1));
		SamTestComponent splitComponent = testCaseExecutionResults.get("Split").get(0);
		Map<String, String> splitComponentFieldAndValues = splitComponent.getFieldsAndValues();
		assertThat(splitComponentFieldAndValues.get("splitJoinValue"), is(expectedSplitJoinValue));
		assertThat(splitComponentFieldAndValues.get("week"), is("39"));
		
		//Validate the HR Enrichment values
		assertNotNull(testCaseExecutionResults.get("ENRICH-HR"));
		assertThat(testCaseExecutionResults.get("ENRICH-HR").size(), is(1));
		SamTestComponent HREnrichComponent = testCaseExecutionResults.get("ENRICH-HR").get(0);
		Map<String, String> hrEnrichmentComponentFieldAndValues = HREnrichComponent.getFieldsAndValues();
		String driverCert = "N";
		String driverWagePlan = "miles";
		assertThat(hrEnrichmentComponentFieldAndValues.get("driverCertification"), is(driverCert));
		assertThat(hrEnrichmentComponentFieldAndValues.get("driverWagePlan"), is(driverWagePlan));
		assertThat(hrEnrichmentComponentFieldAndValues.get("splitJoinValue"), is(expectedSplitJoinValue));

		
		//Validate the Timesheet Enrichment values
		assertNotNull(testCaseExecutionResults.get("ENRICH-Timesheet"));
		assertThat(testCaseExecutionResults.get("ENRICH-Timesheet").size(), is(1));
		SamTestComponent timeSheetEnrichComponent = testCaseExecutionResults.get("ENRICH-Timesheet").get(0);
		Map<String, String>timeSheetrEnrichmentComponentFieldAndValues = timeSheetEnrichComponent.getFieldsAndValues();
		String driverFatigueByHours = "70";
		String driverFatigueByMiles = "3300";
		assertThat(timeSheetrEnrichmentComponentFieldAndValues.get("driverFatigueByHours"), is(driverFatigueByHours));
		assertThat(timeSheetrEnrichmentComponentFieldAndValues.get("driverFatigueByMiles"), is(driverFatigueByMiles));	
		assertThat(timeSheetrEnrichmentComponentFieldAndValues.get("splitJoinValue"), is(expectedSplitJoinValue));
		
		//Validate the Weather Enrichment values
		assertNotNull(testCaseExecutionResults.get("ENRICH-WEATHER"));
		assertThat(testCaseExecutionResults.get("ENRICH-WEATHER").size(), is(1));
		SamTestComponent weatherEnrichComponent = testCaseExecutionResults.get("ENRICH-WEATHER").get(0);
		Map<String, String>weatherEnrichmentComponentFieldAndValues = weatherEnrichComponent.getFieldsAndValues();	
		assertNotNull(weatherEnrichmentComponentFieldAndValues.get("Model_Feature_FoggyWeather"));
		assertNotNull(weatherEnrichmentComponentFieldAndValues.get("Model_Feature_RainyWeather"));	
		assertNotNull(weatherEnrichmentComponentFieldAndValues.get("Model_Feature_WindyWeather"));	
		assertThat(weatherEnrichmentComponentFieldAndValues.get("splitJoinValue"), is(expectedSplitJoinValue));

	

		//Validate the joins of the three enrichments
		assertNotNull(testCaseExecutionResults.get("JOIN-ENRICHMENTS"));
		assertThat(testCaseExecutionResults.get("JOIN-ENRICHMENTS").size(), is(1));
		SamTestComponent joinEnrichComponent = testCaseExecutionResults.get("JOIN-ENRICHMENTS").get(0);
		Map<String, String>joinEnrichmentComponentFieldAndValues = joinEnrichComponent.getFieldsAndValues();
		assertThat(joinEnrichmentComponentFieldAndValues.get("splitJoinValue"), is(expectedSplitJoinValue));
		assertThat(joinEnrichmentComponentFieldAndValues.get("driverCertification"), is(driverCert));
		assertThat(joinEnrichmentComponentFieldAndValues.get("driverWagePlan"), is(driverWagePlan));
		assertThat(joinEnrichmentComponentFieldAndValues.get("driverFatigueByHours"), is(driverFatigueByHours));
		assertThat(joinEnrichmentComponentFieldAndValues.get("driverFatigueByMiles"), is(driverFatigueByMiles));
		assertNotNull(joinEnrichmentComponentFieldAndValues.get("Model_Feature_FoggyWeather"));
		assertNotNull(joinEnrichmentComponentFieldAndValues.get("Model_Feature_RainyWeather"));
		assertNotNull(joinEnrichmentComponentFieldAndValues.get("Model_Feature_WindyWeather"));
		

		//Validate the the Normalized values
		assertNotNull(testCaseExecutionResults.get("NORMALIZE-MODEL-FEATURES"));
		assertThat(testCaseExecutionResults.get("NORMALIZE-MODEL-FEATURES").size(), is(1));
		SamTestComponent normalizeModelComponent = testCaseExecutionResults.get("NORMALIZE-MODEL-FEATURES").get(0);
		Map<String, String>normlaizeModelComponentFieldAndValues = normalizeModelComponent.getFieldsAndValues();
		String driverCertNormalized = "0";
		String driverWagePlanNormalized = "1";
		String driverFatigueByMilesNormalized = "3.3";
		String driverFatigueByHoursNormalized = "0.7";
		assertThat(normlaizeModelComponentFieldAndValues.get("Model_Feature_Certification"), is(driverCertNormalized));
		assertThat(normlaizeModelComponentFieldAndValues.get("Model_Feature_WagePlan"), is(driverWagePlanNormalized));
		assertThat(normlaizeModelComponentFieldAndValues.get("Model_Feature_FatigueByHours"), is(driverFatigueByHoursNormalized));
		assertThat(normlaizeModelComponentFieldAndValues.get("Model_Feature_FatigueByMiles"), is(driverFatigueByMilesNormalized));
		assertNotNull(joinEnrichmentComponentFieldAndValues.get("Model_Feature_FoggyWeather"));
		assertNotNull(joinEnrichmentComponentFieldAndValues.get("Model_Feature_RainyWeather"));
		assertNotNull(joinEnrichmentComponentFieldAndValues.get("Model_Feature_WindyWeather"));;		
		
		//Validate that the Prediction Filter captured the predicted violation 
		assertNotNull(testCaseExecutionResults.get("Prediction"));
		assertThat(testCaseExecutionResults.get("Prediction").size(), is(1));
		SamTestComponent predictionFilterComponent = testCaseExecutionResults.get("Prediction").get(0);
		Map<String, String> predictionFilterComponentFieldAndValues = predictionFilterComponent.getFieldsAndValues();
		String expectedNormalizedYesPrediction = "yes";
		assertThat(predictionFilterComponentFieldAndValues.get("ViolationPredicted"), is(expectedNormalizedYesPrediction));

	}	
	
	@Test
	public void testViolationTruckingEvents() throws Exception {
		String testName = PropertiesConstants.TEST_3_TEST_VIOLATION_EVENT_TEST_CASE;
		
		createTestCase(SAM_APP_NAME, testName, PropertiesConstants.TEST_3_GEO_STREAM_TEST_DATA, PropertiesConstants.TEST_3_SPEED_STREAM_TEST_DATA);
		
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
		assertNotNull( testCaseExecutionResults.get("EventType"));	
		assertThat(testCaseExecutionResults.get("EventType").size(), is(1));
		SamTestComponent filterResult = testCaseExecutionResults.get("EventType").get(0);
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
		assertNull(testCaseExecutionResults.get("isDriverSpeeding"));
		
	}		
	
	@Test
	public void testMultipleSpeedingEvents() throws Exception {
		
		String testName = PropertiesConstants.TEST_4_TEST_VIOLATION_EVENT_TEST_CASE;
		createTestCase(SAM_APP_NAME, testName, PropertiesConstants.TEST_4_GEO_STREAM_TEST_DATA, PropertiesConstants.TEST_4_SPEED_STREAM_TEST_DATA);
		
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
		assertThat(testCaseExecutionResults.get("isDriverSpeeding").size(), is(1));
		assertNotNull(testCaseExecutionResults.get("isDriverSpeeding").get(0));
		SamTestComponent speedingFilterComponent = testCaseExecutionResults.get("isDriverSpeeding").get(0);
		Map<String, String> speedingFilterComponentFieldAndValues = speedingFilterComponent.getFieldsAndValues();
		assertNotNull(speedingFilterComponentFieldAndValues);
		
		/* Validate the projection rounded correctly */
		assertNotNull(testCaseExecutionResults.get("Round").get(0));
		SamTestComponent projectionComponent = testCaseExecutionResults.get("Round").get(0);
		Map<String, String> projectionComponentFieldAndValues = projectionComponent.getFieldsAndValues();
		assertNotNull(projectionComponentFieldAndValues);
		String speedAvgRoundString = projectionComponentFieldAndValues.get("speed_AVG_Round");
		assertNotNull(speedAvgRoundString);
		Long speedAvgRoundLong = Long.valueOf(speedAvgRoundString);
		assertThat(speedAvgRoundLong, is(90L));
		
	}		
	
	public void createTestCase(String appName, String testName, String geoTestData, String speedTestData) {

		/* Create map of test data for each source in the app */
		Map<String, Resource> testDataForSources = new HashMap<String, Resource>();
		Resource geoStreamTestData = createClassPathResource(geoTestData, LOG);	
		testDataForSources.put("TruckGeoEvent", geoStreamTestData);
		
		Resource speedStreamTestData = createClassPathResource(speedTestData, LOG);	
		testDataForSources.put("TruckSpeedEvent", speedStreamTestData);
		
		createTestCase(appName, testName, testDataForSources, LOG);
	}	
	

}
