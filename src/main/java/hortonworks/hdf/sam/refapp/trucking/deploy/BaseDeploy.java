package hortonworks.hdf.sam.refapp.trucking.deploy;

import hortonworks.hdf.sam.refapp.trucking.PropertiesConstants;
import hortonworks.hdf.sam.sdk.app.manager.SAMAppManager;
import hortonworks.hdf.sam.sdk.app.manager.SAMAppManagerImpl;
import hortonworks.hdf.sam.sdk.testcases.manager.SAMTestCaseManager;
import hortonworks.hdf.sam.sdk.testcases.manager.SAMTestCaseManagerImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public abstract class BaseDeploy {
	
	protected Logger LOG = LoggerFactory.getLogger(this.getClass());	
	protected Properties appProperties;
	protected SAMAppManager samAppManager;
	protected SAMTestCaseManager samTestCaseManager;
	protected String samRestUrl;
	protected String samAppName;
	protected Integer deployTimeOut;
	protected String samEnvName;
		
	
	public BaseDeploy(String propFileLocation) {
		loadAppPropertiesFile(propFileLocation);
		samAppManager = new SAMAppManagerImpl(samRestUrl);
		samTestCaseManager = new SAMTestCaseManagerImpl(samRestUrl);
	}


	protected Resource createClassPathResource(String filePath, Logger log) {
		
		Resource resource = new ClassPathResource(filePath);
		if(!resource.exists()) {
			String errMsg = "File["+filePath + "] cannot be found";
			log.error(errMsg);
			throw new RuntimeException(errMsg);
		}	
		return resource;
	}	
	
	
	
	protected void createTestCase(String appName, String testName, String geoTestData, String speedTestData) {

		/* Create map of test data for each source in the app */
		Map<String, Resource> testDataForSources = new HashMap<String, Resource>();
		Resource geoStreamTestData = createClassPathResource(geoTestData, LOG);	
		testDataForSources.put("TruckGeoEvent", geoStreamTestData);
		
		Resource speedStreamTestData = createClassPathResource(speedTestData, LOG);	
		testDataForSources.put("TruckSpeedEvent", speedStreamTestData);
		
		createTestCase(appName, testName, testDataForSources, LOG);
	}	
		
	private void createTestCase(String appName, String testCase, Map<String, Resource> testDataSources, Logger logger) {

		/* delete the testcase if it exists */
		logger.info("Deleting Test Case["+testCase + "] for App["+ appName + "] if it exists");
		samTestCaseManager.deleteTestCase(appName, testCase);		
		
		/* Create the Test Case */
		logger.info("Creating Test Case["+testCase + "] for App["+ appName + "]");
		samTestCaseManager.createTestCase(appName, testCase, testDataSources);
	}
	
	private void loadAppPropertiesFile(String propFileLocation) {
		appProperties = new Properties();
		Resource appPropResource = new FileSystemResource(propFileLocation);
		
		if(!appPropResource.exists()) {
			String errMsg = "App Properties file["+propFileLocation + "] doesn't exist or cannot be loaded";
			LOG.error(errMsg);
			throw new RuntimeException(errMsg);
		}

		try {
			appProperties.load(appPropResource.getInputStream());
		} catch (IOException e) {
			String errMsg = "Cannot load App Properties file["+propFileLocation + "]";
			LOG.error(errMsg);
			throw new RuntimeException(errMsg, e);
		}
		
		this.samRestUrl = appProperties.getProperty(PropertiesConstants.SAM_REST_URL);
		if(StringUtils.isEmpty(samRestUrl)) {
			String errMsg = "Property["+PropertiesConstants.SAM_REST_URL +"] is required";
			throw new RuntimeException(errMsg);
		}		
		
		this.samAppName = appProperties.getProperty(PropertiesConstants.SAM_APP_NAME);
		if(StringUtils.isEmpty(samAppName)) {
			String errMsg = "Property["+PropertiesConstants.SAM_APP_NAME +"] is required";
			throw new RuntimeException(errMsg);
		}		
		
		String deployTimeOutString = appProperties.getProperty(PropertiesConstants.SAM_APP_DEPLOY_TIMEOUT);
		if(StringUtils.isEmpty(deployTimeOutString)) {
			String errMsg = "Property["+PropertiesConstants.SAM_APP_DEPLOY_TIMEOUT +"] is required";
			throw new RuntimeException(errMsg);
		}		
		this.deployTimeOut = Integer.valueOf(deployTimeOutString);
		
		this.samEnvName = appProperties.getProperty(PropertiesConstants.SAM_ENV_NAME);
		if(StringUtils.isEmpty(samEnvName)) {
			String errMsg = "Property["+PropertiesConstants.SAM_ENV_NAME +"] is required";
			throw new RuntimeException(errMsg);
		}			
	}		

}
