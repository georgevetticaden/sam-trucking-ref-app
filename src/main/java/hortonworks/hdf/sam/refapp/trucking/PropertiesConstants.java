package hortonworks.hdf.sam.refapp.trucking;

public class PropertiesConstants {
	
	/* Properties for DeployerApp and EnvBuilder App */
	public static final String SAM_REST_URL="sam.rest.url";
	public static final String SAM_APP_NAME = "sam.app.name";
	public static final String SAM_APP_DEPLOY_TIMEOUT = "sam.app.deploy.timeout.seconds";
	public static final String SAM_SERVICE_POOL_HDF_AMBARI_URL = "sam.service.pool.hdf.ambari.url";
	public static final String SAM_SERVICE_POOL_HDP_AMBARI_URL = "sam.service.pool.hdp.ambari.url";
	
	/* Properties specific to Env Builder App */
	public static final String SAM_EXTENSIONS_HOME = "sam.extensions.home";
	public static final String SAM_CUSTOM_ARTIFACT_SUFFIX = "sam.custom.artifact.suffix";
	public static final String SAM_SCHEMA_REGISTRY_URL = "sam.schema.registry.url";
	public static final String SAM_REGISTER_CUSTOM_SOURCES_SINKS = "sam.register.custom.sources.sinks";
		
	/* Properties speciic to Deployer App */
	public static final String SAM_SERVICE_POOL_HDF_NAME = "sam.service.pool.hdf";
	public static final String SAM_SERVICE_POOL_HDP_NAME = "sam.service.pool.hdp";
	public static final String SAM_ENV_NAME ="sam.env.name";
	public static final String SAM_DEPLOY_REF_APPS = "sam.deploy.refapps";
	
	/* Constants used by Test Cases for Streaming Ref Advanced App */
	public static final String TEST_1_NORMAL_EVENT_NO_PREDICTION_TEST_CASE = "Test-Normal-Event-No-Violation-Prediction-JUNIT";
	public static final String TEST_1_SPEED_STREAM_TEST_DATA = "test-cases-source-data/streaming-ref-advanced-app/normal-event-no-violation-prediction-test/speed-stream-test-data.json";
	public static final String TEST_1_GEO_STREAM_TEST_DATA = "test-cases-source-data/streaming-ref-advanced-app/normal-event-no-violation-prediction-test/geo-stream-test-data.json";
	

	public static final String TEST_2_NORMAL_EVENT_YES_PREDICTION_TEST_CASE = "Test-Normal-Event-Yes-Violation-Prediction-JUNIT";
	public static final String TEST_2_SPEED_STREAM_TEST_DATA = "test-cases-source-data/streaming-ref-advanced-app/normal-event-yes-violation-prediction-test/speed-stream-test-data.json";
	public static final String TEST_2_GEO_STREAM_TEST_DATA = "test-cases-source-data/streaming-ref-advanced-app/normal-event-yes-violation-prediction-test/geo-stream-test-data.json";
	
	public static final String TEST_3_TEST_VIOLATION_EVENT_TEST_CASE = "Test-Violation-Event-JUNIT";
	public static final String TEST_3_SPEED_STREAM_TEST_DATA = "test-cases-source-data/streaming-ref-advanced-app/violation-event-test/speed-stream-test-data.json";
	public static final String TEST_3_GEO_STREAM_TEST_DATA = "test-cases-source-data/streaming-ref-advanced-app/violation-event-test/geo-stream-test-data.json";	
	
	public static final String TEST_4_TEST_VIOLATION_EVENT_TEST_CASE = "Multiple-Speeding-Events";
	public static final String TEST_4_SPEED_STREAM_TEST_DATA = "test-cases-source-data/streaming-ref-advanced-app/multiple-speeding-event-test/speed-stream-test-data.json";
	public static final String TEST_4_GEO_STREAM_TEST_DATA = "test-cases-source-data/streaming-ref-advanced-app/multiple-speeding-event-test/geo-stream-test-data.json";	

	
	/* Other Constants */
	public static final String SAM_REF_APP_ADVANCE_FILE_LOCATION = "/3.1.0.0-501/streaming-ref-app-advanced.json";
	public static final String SAM_REF_APP_FILE_LOCATION = "/3.1.0.0-501/streaming-ref-app.json";

	
	
}
