package hortonworks.hdf.sam.refapp.trucking.deploy;

import hortonworks.hdf.sam.refapp.trucking.PropertiesConstants;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class DeployTruckingRefAdvancedApp extends BaseDeploy {
	
	
	private static final String TEST_1_NORMAL_EVENT_NO_PREDICTION_TEST_CASE = "Test-Normal-Event-No-Violation-Prediction-AUTOCREATED";
	private static final String TEST_1_SPEED_STREAM_TEST_DATA = "test-cases-source-data/streaming-ref-advanced-app/normal-event-no-violation-prediction-test/speed-stream-test-data.json";
	private static final String TEST_1_GEO_STREAM_TEST_DATA = "test-cases-source-data/streaming-ref-advanced-app/normal-event-no-violation-prediction-test/geo-stream-test-data.json";
	

	private static final String TEST_2_NORMAL_EVENT_YES_PREDICTION_TEST_CASE = "Test-Normal-Event-Yes-Violation-Prediction-AUTOCREATED";
	private static final String TEST_2_SPEED_STREAM_TEST_DATA = "test-cases-source-data/streaming-ref-advanced-app/normal-event-yes-violation-prediction-test/speed-stream-test-data.json";
	private static final String TEST_2_GEO_STREAM_TEST_DATA = "test-cases-source-data/streaming-ref-advanced-app/normal-event-yes-violation-prediction-test/geo-stream-test-data.json";
	
	private static final String TEST_3_TEST_VIOLATION_EVENT_TEST_CASE = "Test-Violation-Event-AUTOCREATED";
	private static final String TEST_3_SPEED_STREAM_TEST_DATA = "test-cases-source-data/streaming-ref-advanced-app/violation-event-test/speed-stream-test-data.json";
	private static final String TEST_3_GEO_STREAM_TEST_DATA = "test-cases-source-data/streaming-ref-advanced-app/violation-event-test/geo-stream-test-data.json";	
	
	private static final String TEST_4_TEST_VIOLATION_EVENT_TEST_CASE = "Multiple-Speeding-Events-AUTOCREATED";
	private static final String TEST_4_SPEED_STREAM_TEST_DATA = "test-cases-source-data/streaming-ref-advanced-app/multiple-speeding-event-test/speed-stream-test-data.json";
	private static final String TEST_4_GEO_STREAM_TEST_DATA = "test-cases-source-data/streaming-ref-advanced-app/multiple-speeding-event-test/geo-stream-test-data.json";	
	

	public static void main(String args[]) {
		
		if(args == null || args.length != 1) {
			String errMsg = "One arg with location of properties file need to be passed to app";
			throw new RuntimeException(errMsg);
		}
		String propFileLocation = args[0];
		DeployTruckingRefAdvancedApp deployerApp = new DeployTruckingRefAdvancedApp(propFileLocation);
		deployerApp.deployNewAdvancedTruckingRefApp();;
	}
	
	
	public DeployTruckingRefAdvancedApp(String propFileLocation) {
		super(propFileLocation);

	}

	/**
	 * First undeploy and delete the app
	 * Then add the new app to SAM and deploy 
	 */
	public void deployNewAdvancedTruckingRefApp() {
		samAppManager.killSAMApplication(samAppName);
		samAppManager.deleteSAMApplication(samAppName);
		Resource appResource = new ClassPathResource(PropertiesConstants.SAM_REF_APP_ADVANCE_FILE_LOCATION);
		samAppManager.importSAMApplication(samAppName, samEnvName, appResource);
		samAppManager.deploySAMApplication(samAppName, deployTimeOut);
		createTestCases();
	}


	private void createTestCases() {
		createTestCase(samAppName, TEST_1_NORMAL_EVENT_NO_PREDICTION_TEST_CASE, TEST_1_GEO_STREAM_TEST_DATA, TEST_1_SPEED_STREAM_TEST_DATA);
		createTestCase(samAppName, TEST_2_NORMAL_EVENT_YES_PREDICTION_TEST_CASE, TEST_2_GEO_STREAM_TEST_DATA, TEST_2_SPEED_STREAM_TEST_DATA);
		createTestCase(samAppName, TEST_3_TEST_VIOLATION_EVENT_TEST_CASE, TEST_3_GEO_STREAM_TEST_DATA, TEST_3_SPEED_STREAM_TEST_DATA);
		createTestCase(samAppName, TEST_4_TEST_VIOLATION_EVENT_TEST_CASE, TEST_4_GEO_STREAM_TEST_DATA, TEST_4_SPEED_STREAM_TEST_DATA);
	}


}
