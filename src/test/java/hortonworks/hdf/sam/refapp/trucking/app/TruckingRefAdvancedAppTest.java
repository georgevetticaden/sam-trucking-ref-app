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
import hortonworks.hdf.sam.sdk.app.manager.SAMAppManagerImpl;
import hortonworks.hdf.sam.sdk.environment.manager.SAMEnvironmentManagerImpl;
import hortonworks.hdf.sam.sdk.servicepool.manager.SAMServicePoolManagerImpl;
import hortonworks.hdf.sam.sdk.testcases.manager.SAMTestCaseManager;
import hortonworks.hdf.sam.sdk.testcases.manager.SAMTestCaseManagerImpl;
import hortonworks.hdf.sam.sdk.testcases.model.SamTestComponent;

import org.junit.After;
import org.junit.AfterClass;
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
public class TruckingRefAdvancedAppTest extends BaseTest{

	
	private static final String SAM_APP_NAME = "streaming-ref-advacned-app-junit";
	
	private static Logger LOG = LoggerFactory.getLogger(TruckingRefAdvancedAppTest.class);
	
	private static final String TEST_1_NORMAL_EVENT_TEST_CASE = "Test-Normal-Event";
	private static final String TEST_1_SPEED_STREAM_TEST_DATA = "test-cases-source-data/streaming-ref-advanced-app/normal-event-no-violation-prediction-test/speed-stream-test-data.json";
	private static final String TEST_1_GEO_STREAM_TEST_DATA = "test-cases-source-data/streaming-ref-advanced-app/normal-event-no-violation-prediction-test/geo-stream-test-data.json";
	
		
	
	static {
		loadAppPropertiesFile("/app-properties/junit-trucking-ref-app-advanced.properties");
		String samRestUrl = appProperties.getProperty(AppPropertiesConstants.SAM_REST_URL);
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
		Resource appResource = new ClassPathResource(AppPropertiesConstants.SAM_REF_APP_FILE_LOCATION);
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
		assertNull( testCaseExecutionResults.get("EventType"));		
	}
	
	


	public void createNormalEventTestCase() {

		/* Create map of test data for each source in the app */
		Map<String, Resource> testDataForSources = new HashMap<String, Resource>();
		Resource geoStreamTestData = createClassPathResource(TEST_1_GEO_STREAM_TEST_DATA, LOG);	
		testDataForSources.put("TruckGeoEvent", geoStreamTestData);
		
		Resource speedStreamTestData = createClassPathResource(TEST_1_SPEED_STREAM_TEST_DATA, LOG);	
		testDataForSources.put("TruckSpeedEvent", speedStreamTestData);
		
		createTestCase(SAM_APP_NAME, TEST_1_NORMAL_EVENT_TEST_CASE, testDataForSources, LOG);
		

	}		
	
	

}
