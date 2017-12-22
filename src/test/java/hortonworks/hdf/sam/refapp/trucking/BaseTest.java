package hortonworks.hdf.sam.refapp.trucking;

import hortonworks.hdf.sam.refapp.trucking.deploy.AppPropertiesConstants;
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

public class BaseTest {
	
	protected Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	protected SAMAppManager samAppManager;
	protected SAMTestCaseManager samTestCaseManager;
	
	protected Properties appProperties;
	protected String samRestUrl;
	private String samEnvName;
	
	
	
	protected  void loadAppPropertiesFile() {
		appProperties = new Properties();
		
		String appPropertiesFile = System.getProperty("app.properties.file.location");
		Resource appPropResource = null;
		if(StringUtils.isEmpty(appPropertiesFile)) {
			appPropertiesFile = "/app-properties/trucking-ref-app.properties";
			appPropResource = new ClassPathResource(appPropertiesFile);
		} else
			appPropResource = new FileSystemResource(appPropertiesFile);
		
		if(!appPropResource.exists()) {
			String errMsg = "App Properties file["+appPropResource.getFilename() + "] doesn't exist or cannot be loaded";
			LOG.error(errMsg);
			throw new RuntimeException(errMsg);
		}
		
		try {
			appProperties.load(appPropResource.getInputStream());
			LOG.info("App Properties is: " + appProperties);
		} catch (IOException e) {
			String errMsg = "Cannot load App Properties file["+appPropResource.getFilename() + "]";
			LOG.error(errMsg);
			throw new RuntimeException(errMsg, e);
		}		

		samRestUrl = appProperties.getProperty(AppPropertiesConstants.SAM_REST_URL);
		if(StringUtils.isEmpty(samRestUrl)) {
			String errMsg = "Property["+AppPropertiesConstants.SAM_REST_URL +"] is required";
			throw new RuntimeException(errMsg);
		}	
		this.samEnvName = appProperties.getProperty(AppPropertiesConstants.SAM_ENV_NAME);
		if(StringUtils.isEmpty(samEnvName)) {
			String errMsg = "Property["+AppPropertiesConstants.SAM_ENV_NAME +"] is required";
			throw new RuntimeException(errMsg);
		}	
		
		this.samAppManager = new SAMAppManagerImpl(samRestUrl);
		this.samTestCaseManager = new SAMTestCaseManagerImpl(samRestUrl);
		
	}
	

	public void importSAMApp(String samAppName, Resource appResource) {
		samAppManager.importSAMApplication(samAppName, samEnvName, appResource);
		
	}	
	
	public void deleteSAMApp(String samAppName) {
		samAppManager.deleteSAMApplication(samAppName);
	}

 
	protected Resource createClassPathResource(String filePath) {
		
		Resource resource = new ClassPathResource(filePath);
		if(!resource.exists()) {
			String errMsg = "File["+filePath + "] cannot be found";
			LOG.error(errMsg);
			throw new RuntimeException(errMsg);
		}	
		return resource;
	}		
	
	protected void createTestCase(String appName, String testCase, Map<String, Resource> testDataSources) {

		/* delete the testcase if it exists */
		LOG.info("Deleting Test Case["+testCase + "] for App["+ appName + "] if it exists");
		samTestCaseManager.deleteTestCase(appName, testCase);		
		
		/* Create the Test Case */
		LOG.info("Creating Test Case["+testCase + "] for App["+ appName + "]");
		samTestCaseManager.createTestCase(appName, testCase, testDataSources);
	}		

}
