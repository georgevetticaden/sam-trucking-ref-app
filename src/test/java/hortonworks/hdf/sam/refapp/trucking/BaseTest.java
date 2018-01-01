package hortonworks.hdf.sam.refapp.trucking;

import hortonworks.hdf.sam.refapp.trucking.deploy.AppPropertiesConstants;
import hortonworks.hdf.sam.sdk.app.manager.SAMAppManager;
import hortonworks.hdf.sam.sdk.app.manager.SAMAppManagerImpl;
import hortonworks.hdf.sam.sdk.environment.manager.SAMEnvironmentManager;
import hortonworks.hdf.sam.sdk.environment.manager.SAMEnvironmentManagerImpl;
import hortonworks.hdf.sam.sdk.environment.model.ServiceEnvironmentMapping;
import hortonworks.hdf.sam.sdk.servicepool.manager.SAMServicePoolManager;
import hortonworks.hdf.sam.sdk.servicepool.manager.SAMServicePoolManagerImpl;
import hortonworks.hdf.sam.sdk.testcases.manager.SAMTestCaseManager;
import hortonworks.hdf.sam.sdk.testcases.manager.SAMTestCaseManagerImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public abstract class BaseTest {
	
	
	
	protected static final Logger BASE_TEST_LOG = LoggerFactory.getLogger(BaseTest.class);
	
	protected static SAMAppManager samAppManager;
	protected static SAMTestCaseManager samTestCaseManager;
	protected static SAMServicePoolManager samServicePoolManager;
	protected static SAMEnvironmentManager samEnvironmentManager;
	
	protected static Properties appProperties;
	

	
	protected  static void loadAppPropertiesFile(String defaultPropFile) {
		appProperties = new Properties();
		
		String appPropertiesFile = System.getProperty("app.properties.file.location");
		Resource appPropResource = null;
		if(StringUtils.isEmpty(appPropertiesFile)) {
			appPropertiesFile = defaultPropFile;
			appPropResource = new ClassPathResource(appPropertiesFile);
		} else
			appPropResource = new FileSystemResource(appPropertiesFile);
		
		if(!appPropResource.exists()) {
			String errMsg = "App Properties file["+appPropResource.getFilename() + "] doesn't exist or cannot be loaded";
			BASE_TEST_LOG.error(errMsg);
			throw new RuntimeException(errMsg);
		}
		
		try {
			appProperties.load(appPropResource.getInputStream());
			BASE_TEST_LOG.info("App Properties is: " + appProperties);
		} catch (IOException e) {
			String errMsg = "Cannot load App Properties file["+appPropResource.getFilename() + "]";
			BASE_TEST_LOG.error(errMsg);
			throw new RuntimeException(errMsg, e);
		}		

		String samRestUrl = appProperties.getProperty(AppPropertiesConstants.SAM_REST_URL);
		if(StringUtils.isEmpty(samRestUrl)) {
			String errMsg = "Property["+AppPropertiesConstants.SAM_REST_URL +"] is required";
			throw new RuntimeException(errMsg);
		}	
		String samEnvName = appProperties.getProperty(AppPropertiesConstants.SAM_ENV_NAME);
		if(StringUtils.isEmpty(samEnvName)) {
			String errMsg = "Property["+AppPropertiesConstants.SAM_ENV_NAME +"] is required";
			throw new RuntimeException(errMsg);
		}	
		
		String samServicePoolHDFName = appProperties.getProperty(AppPropertiesConstants.SAM_SERVICE_POOL_HDF_NAME);
		if(StringUtils.isEmpty(samServicePoolHDFName)) {
			String errMsg = "Property["+AppPropertiesConstants.SAM_SERVICE_POOL_HDF_NAME +"] is required";
			throw new RuntimeException(errMsg);
		}			
		String samServicePoolHDPName = appProperties.getProperty(AppPropertiesConstants.SAM_SERVICE_POOL_HDP_NAME);
		if(StringUtils.isEmpty(samServicePoolHDPName)) {
			String errMsg = "Property["+AppPropertiesConstants.SAM_SERVICE_POOL_HDP_NAME +"] is required";
			throw new RuntimeException(errMsg);
		}			
		String samServicePoolHDFRestUrl = appProperties.getProperty(AppPropertiesConstants.SAM_SERVICE_POOL_HDF_AMBARI_URL);
		if(StringUtils.isEmpty(samServicePoolHDFRestUrl)) {
			String errMsg = "Property["+AppPropertiesConstants.SAM_SERVICE_POOL_HDF_AMBARI_URL +"] is required";
			throw new RuntimeException(errMsg);
		}	
		String samServicePoolHDPRestUrl = appProperties.getProperty(AppPropertiesConstants.SAM_SERVICE_POOL_HDP_AMBARI_URL);
		if(StringUtils.isEmpty(samServicePoolHDPRestUrl)) {
			String errMsg = "Property["+AppPropertiesConstants.SAM_SERVICE_POOL_HDP_AMBARI_URL +"] is required";
			throw new RuntimeException(errMsg);
		}		
	

		
	}
	

	public static void importSAMApp(String samAppName, Resource appResource) {
		String samEnvName = appProperties.getProperty(AppPropertiesConstants.SAM_ENV_NAME);
		samAppManager.importSAMApplication(samAppName, samEnvName, appResource);
		
	}	
	
	public static void deleteSAMApp(String samAppName) {
		samAppManager.deleteSAMApplication(samAppName);
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
	
	protected void createTestCase(String appName, String testCase, Map<String, Resource> testDataSources, Logger logger) {

		/* delete the testcase if it exists */
		logger.info("Deleting Test Case["+testCase + "] for App["+ appName + "] if it exists");
		samTestCaseManager.deleteTestCase(appName, testCase);		
		
		/* Create the Test Case */
		logger.info("Creating Test Case["+testCase + "] for App["+ appName + "]");
		samTestCaseManager.createTestCase(appName, testCase, testDataSources);
	}	
	
	protected static void createServicePools() {
		String samServicePoolHDFName = appProperties.getProperty(AppPropertiesConstants.SAM_SERVICE_POOL_HDF_NAME);
		String samServicePoolHDFRestUrl = appProperties.getProperty(AppPropertiesConstants.SAM_SERVICE_POOL_HDF_AMBARI_URL);
		samServicePoolManager.createServicePool(samServicePoolHDFName, samServicePoolHDFRestUrl, "admin", "admin");
		
		String samServicePoolHDPName = appProperties.getProperty(AppPropertiesConstants.SAM_SERVICE_POOL_HDP_NAME);
		String samServicePoolHDPRestUrl = appProperties.getProperty(AppPropertiesConstants.SAM_SERVICE_POOL_HDP_AMBARI_URL);
		samServicePoolManager.createServicePool(samServicePoolHDPName, samServicePoolHDPRestUrl, "admin", "admin");		
		
	}
	
	public static void deleteServicePools() {
		String samServicePoolHDFName = appProperties.getProperty(AppPropertiesConstants.SAM_SERVICE_POOL_HDF_NAME);
		samServicePoolManager.deleteServicePool(samServicePoolHDFName);
		
		String samServicePoolHDPName = appProperties.getProperty(AppPropertiesConstants.SAM_SERVICE_POOL_HDP_NAME);
		samServicePoolManager.deleteServicePool(samServicePoolHDPName);

		
	}		
	
	protected static void createEnv() {
		String samEnvName = appProperties.getProperty(AppPropertiesConstants.SAM_ENV_NAME);
		samEnvironmentManager.createSAMEnvironment(samEnvName, "junit env", createServiceMappings());
	}
	
	protected static void deleteEnv() {
		String samEnvName = appProperties.getProperty(AppPropertiesConstants.SAM_ENV_NAME);
		samEnvironmentManager.deleteSAMEnvironment(samEnvName);
	}
	
	private static List<ServiceEnvironmentMapping> createServiceMappings() {
		List<ServiceEnvironmentMapping> mappings = new ArrayList<ServiceEnvironmentMapping>();
		
		String hdfServicePool = appProperties.getProperty(AppPropertiesConstants.SAM_SERVICE_POOL_HDF_NAME);
		mappings.add(new ServiceEnvironmentMapping(hdfServicePool, "STORM"));
		mappings.add(new ServiceEnvironmentMapping(hdfServicePool, "KAFKA"));
		mappings.add(new ServiceEnvironmentMapping(hdfServicePool, "ZOOKEEPER"));
		mappings.add(new ServiceEnvironmentMapping(hdfServicePool, "AMBARI_INFRA"));
		mappings.add(new ServiceEnvironmentMapping(hdfServicePool, "AMBARI_METRICS"));
		
		String hdpServicePool = appProperties.getProperty(AppPropertiesConstants.SAM_SERVICE_POOL_HDP_NAME);
		mappings.add(new ServiceEnvironmentMapping(hdpServicePool, "DRUID"));
		mappings.add(new ServiceEnvironmentMapping(hdpServicePool, "HBASE"));
		mappings.add(new ServiceEnvironmentMapping(hdpServicePool, "HDFS"));		
		return mappings;
	}		

}
