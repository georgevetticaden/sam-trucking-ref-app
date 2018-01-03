package hortonworks.hdf.sam.refapp.trucking.env;



import hortonworks.hdf.sam.refapp.trucking.deploy.AppPropertiesConstants;
import hortonworks.hdf.sam.refapp.trucking.simulator.schemaregistry.TruckSchemaRegistryLoader;
import hortonworks.hdf.sam.sdk.app.manager.SAMAppManagerImpl;
import hortonworks.hdf.sam.sdk.component.SAMProcessorComponentSDKUtils;
import hortonworks.hdf.sam.sdk.component.SAMSourceSinkComponentSDKUtils;
import hortonworks.hdf.sam.sdk.component.model.ComponentType;
import hortonworks.hdf.sam.sdk.component.model.SAMComponent;
import hortonworks.hdf.sam.sdk.component.model.SAMProcessorComponent;
import hortonworks.hdf.sam.sdk.environment.manager.SAMEnvironmentManagerImpl;
import hortonworks.hdf.sam.sdk.environment.model.SAMEnvironmentDetails;
import hortonworks.hdf.sam.sdk.environment.model.ServiceEnvironmentMapping;
import hortonworks.hdf.sam.sdk.modelregistry.SAMModelRegistrySDKUtils;
import hortonworks.hdf.sam.sdk.modelregistry.model.PMMLModel;
import hortonworks.hdf.sam.sdk.servicepool.manager.SAMServicePoolManagerImpl;
import hortonworks.hdf.sam.sdk.udf.SAMUDFSDKUtils;
import hortonworks.hdf.sam.sdk.udf.model.SAMUDF;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class TruckingRefAppEnviornmentBuilderImpl implements TruckingRefAppEnviornmentBuilder {
	
	
	protected final Logger LOG = LoggerFactory.getLogger(TruckingRefAppEnviornmentBuilderImpl.class);
	private static final int KILL_TIMEOUT_SECONDS = 50;
	private static final int DEPLOY_TIMEOUT_SECONDS = 50;
	
	
	/* Constructor Args Required for teh Builder */
	private String samRESTUrl;
	private String samCustomArtifactHomeDir;
	private String samCustomArtifactSuffix = "";
	private String hdfAmbariClusterEndpointUrl;	
	private String hdpAmbariClusterEndpointUrl;
	
	/* SDK clients to different SAM Entities */
	private SAMUDFSDKUtils udfSDK;
	private SAMProcessorComponentSDKUtils processorSDK;
	private SAMSourceSinkComponentSDKUtils sourceSinkSDK;
	private SAMModelRegistrySDKUtils modelRegistrySDK;	
	private SAMServicePoolManagerImpl samServicePoolManager;
	private SAMEnvironmentManagerImpl samEnvironmentManager;	
	private SAMAppManagerImpl samAppManager;	
	private TruckSchemaRegistryLoader schemaRegistryLoader;
	
	/* Names of different entities that will be created for SAM Trucking Ref App */
	private String roundUDFName = "ROUND";
	private String timestampLogUDFName = "TIMESTAMP_LONG";
	private String weekUDFName = "GET_WEEK";
	private String enrichWeatherProcessorName = "ENRICH-WEATHER";
	private String normalizeModelProcessorName = "NORMALIZE-MODEL-FEATURES";
	private String phoenixEnrichmentProcessorName = "ENRICH-PHOENIX";
	private String kinesisSourceName = "Kinesis";
	private String s3SinkName = "S3";	
	private String violationPredictionPMMLModel = "DriverViolationPredictionModel";
	private String truckingAppAdvancedAppName = "streaming-ref-app-advanced";
	private String hdfServicePoolName = "streamanalytics";
	private String hdpServicePoolName = "datalake";
	private String samEnvName = "Dev";
	
	
	public static void main(String args[]) {
		
		if(args == null || args.length != 1) {
			String errMsg = "One arg with location of properties file for environment creation needs to be passed to app";
			throw new RuntimeException(errMsg);
		}
		String propFileLocation = args[0];
		Resource envPropResource = new FileSystemResource(propFileLocation);
		TruckingRefAppEnviornmentBuilder envBuilder = new TruckingRefAppEnviornmentBuilderImpl(envPropResource);
		envBuilder.buildEnvironment();
		
	}
	
	public TruckingRefAppEnviornmentBuilderImpl(Resource resource) {
		Properties envProperties = loadAppPropertiesFile(resource);
		init(envProperties.getProperty(EnvPropertiesConstants.SAM_REST_URL), 
			 envProperties.getProperty(EnvPropertiesConstants.SAM_EXTENSIONS_HOME), 
			 envProperties.getProperty(EnvPropertiesConstants.SAM_CUSTOM_ARTIFACT_SUFFIX), 
			 envProperties.getProperty(EnvPropertiesConstants.HDF_SERVICE_POOL_AMBARI_URL), 
			 envProperties.getProperty(EnvPropertiesConstants.HDP_SERVICE_POOL_AMBARI_URL), 
			 envProperties.getProperty(EnvPropertiesConstants.SAM_SCHEMA_REGISTRY_URL));
	}
	
	public TruckingRefAppEnviornmentBuilderImpl(String samRestURL, String extensionHomeDirectory, String extensionsArtifactSuffix, 
											    String hdfAmbariClusterEndpointUrl, String hdpAmbariClusterEndpointUrl, String schemaRegistryUrl  ) {
		

		init(samRestURL, extensionHomeDirectory, extensionsArtifactSuffix,
				hdfAmbariClusterEndpointUrl, hdpAmbariClusterEndpointUrl,
				schemaRegistryUrl);
		
	}

	private void init(String samRestURL, String extensionHomeDirectory,
			String extensionsArtifactSuffix,
			String hdfAmbariClusterEndpointUrl,
			String hdpAmbariClusterEndpointUrl, String schemaRegistryUrl) {
		this.samRESTUrl = samRestURL;
		this.udfSDK = new SAMUDFSDKUtils(samRESTUrl);
		this.processorSDK = new SAMProcessorComponentSDKUtils(samRESTUrl);
		this.sourceSinkSDK = new SAMSourceSinkComponentSDKUtils(samRESTUrl);
		this.modelRegistrySDK = new SAMModelRegistrySDKUtils(samRESTUrl);
		this.samServicePoolManager = new SAMServicePoolManagerImpl(samRESTUrl);
		this.samEnvironmentManager = new SAMEnvironmentManagerImpl(samRESTUrl);
		this.samAppManager = new SAMAppManagerImpl(samRESTUrl);
		
		this.samCustomArtifactHomeDir = extensionHomeDirectory;
		
		this.hdfAmbariClusterEndpointUrl = hdfAmbariClusterEndpointUrl;
		this.hdpAmbariClusterEndpointUrl = hdpAmbariClusterEndpointUrl;
		this.schemaRegistryLoader = new TruckSchemaRegistryLoader(schemaRegistryUrl);
		
		if(StringUtils.isNotEmpty(extensionsArtifactSuffix)) {
			this.samCustomArtifactSuffix = extensionsArtifactSuffix;
			updateEntityNames();
		}
	}

	



	/**
	 * Builds the environment required to deploy the Trucking Ref App 
	 */
	@Override
	public void buildEnvironment() {
		DateTime startTime = new DateTime();
		LOG.info("Trucking Ref App Environment creation started[" + startTime.toString() + "]");
		
		createSchemasInSchemaRegistry();
		uploadAllCustomUDFsForRefApp();
		uploadAllCustomSources();
		uploadAllCustomSinks();
		uploadAllModels();
		uploadAllCustomProcessorsForRefApp();
		createServicePools();
		createEnvironments();
		//importRefApps();
		//deployRefApps();
		
		DateTime endTime = new DateTime();
		Seconds envCreationTime = Seconds.secondsBetween(startTime, endTime);
		LOG.info("Trucking Ref App Environment creation completed[ " + endTime.toString() + "]");
		LOG.info("Trucking Ref App environment creation time["+  + envCreationTime.getSeconds() + " seconds]");

		
	}






	/**
	 * Tears down the Trucking Ref App and the enviroment
	 */
	@Override
	public void tearDownEnvironment() {
		
		DateTime startTime = new DateTime();
		LOG.info("Trucking Ref App Environment teardown started[" + startTime.toString() + "]");
		
		killAllRefApps();
		deleteAllRefApps();
		deleteAllCustomUDFsForRefApp();
		deleteAllCustomSources();
		deleteAllCustomSinks();
		deleteAllCustomModels();
		deleteAllCustomProcessorsRefApp();
		deleteAllEnvironments();
		deleteAllServicePools();
		
		DateTime endTime = new DateTime();
		Seconds envCreationTime = Seconds.secondsBetween(startTime, endTime);
		LOG.info("Trucking Ref App Environment teardown completed[ " + endTime.toString() + "]");
		LOG.info("Trucking Ref App environment teardwon time["+  + envCreationTime.getSeconds() + " seconds]");		
		
	}
	



	public void importRefApps() {
		DateTime start = new DateTime();
		LOG.info("Starting to IMport all Ref Apps");
		
		importTruckingRefAppAdvanced();
		
		DateTime end = new DateTime();
		Seconds creationTime = Seconds.secondsBetween(start, end);
		LOG.info("Finished Importing all Ref Apps. Time taken[ "+creationTime.getSeconds() + " seconds ]");
	}
	
	public void deployRefApps() {
		DateTime start = new DateTime();
		LOG.info("Starting to Deploy All Ref Apps");
		
		deployTruckingRefApp();
		
		DateTime end = new DateTime();
		Seconds creationTime = Seconds.secondsBetween(start, end);
		LOG.info("Finished Deploying all Ref Apps. Time taken[ "+creationTime.getSeconds() + " seconds ]");
		
	}
	
	public void killAllRefApps() {
		DateTime start = new DateTime();
		LOG.info("Starting to Kill All Ref Apps");
		
		killTruckingRefApp();
		
		DateTime end = new DateTime();
		Seconds deleteTime = Seconds.secondsBetween(start, end);
		LOG.info("Finished Killing all Ref Apps. Time taken[ "+deleteTime.getSeconds() + " seconds ]");
	}
	
	public void deleteAllRefApps() {
		DateTime start = new DateTime();
		LOG.info("Starting to Delete All Ref Apps");
		
		deleteTruckingRefAppAdvanced();
		
		DateTime end = new DateTime();
		Seconds deleteTime = Seconds.secondsBetween(start, end);
		LOG.info("Finished Deletings all Ref Apps. Time taken[ "+deleteTime.getSeconds() + " seconds ]");		
	}
	
	
	public void deleteTruckingRefAppAdvanced() {
		samAppManager.deleteSAMApplication(truckingAppAdvancedAppName);
	}
	
	public void killTruckingRefApp() {
		samAppManager.killSAMApplication(truckingAppAdvancedAppName, KILL_TIMEOUT_SECONDS);
	}	
	
	public void importTruckingRefAppAdvanced() {
		Resource samImportResource = new ClassPathResource(AppPropertiesConstants.SAM_REF_APP_ADVANCE_FILE_LOCATION);
		samAppManager.importSAMApplication(truckingAppAdvancedAppName, samEnvName, samImportResource);
	}
	
	

	
	public void deployTruckingRefApp() {
		samAppManager.deploySAMApplication(truckingAppAdvancedAppName, DEPLOY_TIMEOUT_SECONDS);

	}	
	

	private void createSchemasInSchemaRegistry() {
		DateTime start = new DateTime();
		LOG.info("Starting to create All Schemas in Scehma Registry");
		
		schemaRegistryLoader.loadSchemaRegistry();
		
		DateTime end = new DateTime();
		Seconds creationTime = Seconds.secondsBetween(start, end);
		LOG.info("Finished creating All Schemas in SChema Registry. Time taken[ "+creationTime.getSeconds() + " seconds ]");		
	}
	
	
	private void createServicePools() {
		DateTime start = new DateTime();
		LOG.info("Starting to create All Service Pools");
		
		samServicePoolManager.createServicePool(hdfServicePoolName, hdfAmbariClusterEndpointUrl, "admin", "admin");
		LOG.info("Service Pool["+ hdfServicePoolName +"] created with Ambari Endpoint[" + hdfAmbariClusterEndpointUrl +"]");

		samServicePoolManager.createServicePool(hdpServicePoolName, hdpAmbariClusterEndpointUrl, "admin", "admin");
		LOG.info("Service Pool["+ hdpServicePoolName +"] created with Ambari Endpoint[" + hdpAmbariClusterEndpointUrl +"]");

		
		DateTime end = new DateTime();
		Seconds creationTime = Seconds.secondsBetween(start, end);
		LOG.info("Finished creating All Service Pools. Time taken[ "+creationTime.getSeconds() + " seconds ]");
		
	}

	private void createEnvironments() {
		DateTime start = new DateTime();
		LOG.info("Starting to create All Enviornemnts");
		
		SAMEnvironmentDetails samEnvDetails = samEnvironmentManager.createSAMEnvironment(samEnvName, "Enviornment created from automated scripts", createServiceMappings());
		LOG.info("Env["+ samEnvName +"] .Environment Details: " + samEnvDetails );
		
		DateTime end = new DateTime();
		Seconds creationTime = Seconds.secondsBetween(start, end);
		LOG.info("Finished creating Environments. Time taken[ "+creationTime.getSeconds() + " seconds ]");		

	}


	public void uploadAllCustomUDFsForRefApp() {
		DateTime start = new DateTime();
		LOG.info("Starting to Upload all Custom UDFs");
		
		uploadRoundUDF();
		uploadTimestampLongUDF();
		uploadGetWeekUDF();
		
		DateTime end = new DateTime();
		Seconds creationTime = Seconds.secondsBetween(start, end);
		LOG.info("Finished Uploading all Custom UDFs. Time taken[ "+creationTime.getSeconds() + " seconds ]");
	}	
	
	public void uploadAllCustomProcessorsForRefApp() {
		DateTime start = new DateTime();
		LOG.info("Starting to Upload all Custom Processors");
		
		uploadWeatherEnrichmentSAMProcessor();
		uploadNormalizeModelSAMProcessor();
		uploadPhoenixEnrichmentSAMProcessor();
		
		DateTime end = new DateTime();
		Seconds creationTime = Seconds.secondsBetween(start, end);
		LOG.info("Finished Uploading all Custom Processors. Time taken[ "+creationTime.getSeconds() + " seconds ]");		
	}		
	
	public void uploadAllCustomSources() {
		
		DateTime start = new DateTime();
		LOG.info("Starting to Upload all Custom Sources");
		
		uploadCustomKinesisSource();
		
		DateTime end = new DateTime();
		Seconds creationTime = Seconds.secondsBetween(start, end);
		LOG.info("Finished Uploading all Custom Sources. Time taken[ "+creationTime.getSeconds() + " seconds ]");		
	}
	
	public void uploadAllCustomSinks() {
		DateTime start = new DateTime();
		LOG.info("Starting to Upload all Custom Sinks");
		
		uploadCustomS3Sink();
		
		DateTime end = new DateTime();
		Seconds creationTime = Seconds.secondsBetween(start, end);
		LOG.info("Finished Uploading all Custom Sinks. Time taken[ "+creationTime.getSeconds() + " seconds ]");			
	}
	
	public void uploadAllModels() {
		DateTime start = new DateTime();
		LOG.info("Starting to Upload all Models to Model Registry");
		
		updateViolationPredictionModelToModelRegistry();
		
		DateTime end = new DateTime();
		Seconds creationTime = Seconds.secondsBetween(start, end);
		LOG.info("Finished Uploading all Models to Model Registry. Time taken[ "+creationTime.getSeconds() + " seconds ]");			
	}	
	
	public void uploadRoundUDF() {
		String udfConfigFile = samCustomArtifactHomeDir
				+ "/custom-udf/config/round-udf-config.json";
		String udfJar = samCustomArtifactHomeDir
				+ "/custom-udf/sam-custom-udf.jar";
		SAMUDF roundUdf = udfSDK.uploadUDF(udfConfigFile, udfJar);
		LOG.info("The Round UDF created is: " + roundUdf.toString());
	}	
	
	
	public void uploadTimestampLongUDF()  {
		String udfConfigFile = samCustomArtifactHomeDir
				+ "/custom-udf/config/timestamp-long-udf.json";
		String udfJar = samCustomArtifactHomeDir
				+ "/custom-udf/sam-custom-udf.jar";
		SAMUDF timeStampLongUdf = udfSDK.uploadUDF(udfConfigFile, udfJar);
		LOG.info("The TimestmapLong UDF created is: " + timeStampLongUdf.toString());
	}	
	
	public void uploadGetWeekUDF()  {
		String udfConfigFile = samCustomArtifactHomeDir
				+ "/custom-udf/config/get-week-udf.json";
		String udfJar = samCustomArtifactHomeDir
				+ "/custom-udf/sam-custom-udf.jar";
		SAMUDF udfAdded = udfSDK.uploadUDF(udfConfigFile, udfJar);
		LOG.info("The GetWeek UDF created is: " + udfAdded.toString());
	}	
	

	
	public void uploadWeatherEnrichmentSAMProcessor() {
		String fluxFile = samCustomArtifactHomeDir + "/custom-processor/config/weather-enrichment-processor-component.json";
		String customProcessorJar = samCustomArtifactHomeDir + "/custom-processor/sam-custom-processor.jar";
		SAMProcessorComponent samComponent = processorSDK.uploadCustomProcessor(fluxFile, customProcessorJar);
		LOG.info("The Weather Enrichment Processor created is: " + samComponent.toString());
	}	
	
	public void uploadNormalizeModelSAMProcessor() {
		String fluxFile = samCustomArtifactHomeDir + "/custom-processor/config/normalize-model-features-processor-component.json";
		String customProcessorJar = samCustomArtifactHomeDir + "/custom-processor/sam-custom-processor.jar";
		SAMProcessorComponent samComponent = processorSDK.uploadCustomProcessor(fluxFile, customProcessorJar);
		LOG.info("The Normalize Model Processor created is: " + samComponent.toString());
	}
	
	public void uploadPhoenixEnrichmentSAMProcessor() {
		
		LOG.info("Starting uploading of PHoenixEnirchment Processer. This will take a few minutes");
		String fluxFile = samCustomArtifactHomeDir + "/custom-processor/config/phoenix-enrichment-processor-component.json";
		String customProcessorJar = samCustomArtifactHomeDir + "/custom-processor/sam-custom-processor-jar-with-dependencies.jar";
		SAMProcessorComponent samComponent = processorSDK.uploadCustomProcessor(fluxFile, customProcessorJar);
		LOG.info("The Phoenix EnrichmentProcessor created is: " + samComponent.toString());
	}	
	
	
	public void uploadCustomKinesisSource() {
		String fluxFileLocation = samCustomArtifactHomeDir + "/custom-source/kinesis/config/kinesis-source-topology-component.json";
		String customSourceJarLocation = samCustomArtifactHomeDir + "/custom-source/kinesis/sam-custom-source-kinesis.jar";
		SAMComponent samCustomSource = sourceSinkSDK.uploadSAMComponent(ComponentType.SOURCE, fluxFileLocation, customSourceJarLocation);
		LOG.info("The Kinesis Source creteated is: " + samCustomSource.toString());
	}
	
	public void uploadCustomS3Sink() {
		String fluxFileLocation = samCustomArtifactHomeDir + "/custom-sink/s3/config/s3-sink-topology-component.json";
		String customSinkJarLocation = samCustomArtifactHomeDir + "/custom-sink/s3/sam-custom-sink-s3.jar";		
		SAMComponent samCustomSink = sourceSinkSDK.uploadSAMComponent(ComponentType.SINK, fluxFileLocation, customSinkJarLocation);
		LOG.info(samCustomSink.toString());
		
	}		
	
	public void updateViolationPredictionModelToModelRegistry() {
		String violationPredictionModelFile = samCustomArtifactHomeDir + "/custom-pmml-model/DriverViolationLogisticalRegessionPredictionModel-pmml.xml";
		String violationPredictionsModelConfigFile = samCustomArtifactHomeDir + "/custom-pmml-model/config/DriverViolationLogisticalRegessionPredictionModel-config.json";
		PMMLModel violatonModel = modelRegistrySDK.addModel(violationPredictionsModelConfigFile, violationPredictionModelFile);
		LOG.debug("Violation PMML Model created: " + violatonModel);
	}	

	public void deleteAllCustomUDFsForRefApp() {
		
		DateTime start = new DateTime();
		LOG.info("Starting to Delete all Custom UDFs");
		
		udfSDK.deleteUDF(roundUDFName);
		udfSDK.deleteUDF(timestampLogUDFName);
		udfSDK.deleteUDF(weekUDFName);
		
		
		DateTime end = new DateTime();
		Seconds creationTime = Seconds.secondsBetween(start, end);
		LOG.info("Finished Deleting all Custom UDFs. Time taken[ "+creationTime.getSeconds() + " seconds ]");		
	}	
	
	public void deleteAllCustomProcessorsRefApp() {
		
		DateTime start = new DateTime();
		LOG.info("Starting to Delete all Custom Processors");
		
		processorSDK.deleteCustomSAMProcessor(enrichWeatherProcessorName);
		processorSDK.deleteCustomSAMProcessor(normalizeModelProcessorName);
		processorSDK.deleteCustomSAMProcessor(phoenixEnrichmentProcessorName);

	}	
	
	private void deleteAllCustomModels() {
		
		DateTime start = new DateTime();
		LOG.info("Starting to Delete all Models in Model Registry");
		
		modelRegistrySDK.deleteModel(violationPredictionPMMLModel);
		
		DateTime end = new DateTime();
		Seconds deleteTime = Seconds.secondsBetween(start, end);
		LOG.info("Finished Deleting all Models from Model Registry. Time taken[ "+deleteTime.getSeconds() + " seconds ]");			
		
	}

	private void deleteAllCustomSinks() {
	
		DateTime start = new DateTime();
		LOG.info("Starting to Delete all Custom Sinks");	
		
		sourceSinkSDK.deleteCustomComponent(ComponentType.SINK,  s3SinkName);
		
		DateTime end = new DateTime();
		Seconds deleteTime = Seconds.secondsBetween(start, end);
		LOG.info("Finished Deleting all Custom Sinks. Time taken[ "+deleteTime.getSeconds() + " seconds ]");			
		
	}

	private void deleteAllCustomSources() {
		
		DateTime start = new DateTime();
		LOG.info("Starting to Delete all Custom Sources");
		
		sourceSinkSDK.deleteCustomComponent(ComponentType.SOURCE, kinesisSourceName);
		
		DateTime end = new DateTime();
		Seconds creationTime = Seconds.secondsBetween(start, end);
		LOG.info("Finished Deleting all Custom Processors. Time taken[ "+creationTime.getSeconds() + " seconds ]");		
		
	}	
	
	private void deleteAllEnvironments() {
		DateTime start = new DateTime();
		LOG.info("Starting to Delete all Environments");	
		
		samEnvironmentManager.deleteSAMEnvironment(samEnvName);
		
		DateTime end = new DateTime();
		Seconds deleteTime = Seconds.secondsBetween(start, end);
		LOG.info("Finished Deleting all Environments. Time taken[ "+deleteTime.getSeconds() + " seconds ]");
		
	}	
	
	private void deleteAllServicePools() {
		
		DateTime start = new DateTime();
		LOG.info("Starting to Delete all Service Pools");	
		
		samServicePoolManager.deleteServicePool(hdfServicePoolName);
		samServicePoolManager.deleteServicePool(hdpServicePoolName);
		
		DateTime end = new DateTime();
		Seconds deleteTime = Seconds.secondsBetween(start, end);
		LOG.info("Finished Deleting all Service Pools. Time taken[ "+deleteTime.getSeconds() + " seconds ]");			
	}



	
	private void updateEntityNames() {

		roundUDFName = roundUDFName + samCustomArtifactSuffix;
		timestampLogUDFName = timestampLogUDFName + samCustomArtifactSuffix;
		weekUDFName = weekUDFName + samCustomArtifactSuffix;
		
		enrichWeatherProcessorName = enrichWeatherProcessorName + samCustomArtifactSuffix;
		normalizeModelProcessorName = normalizeModelProcessorName + samCustomArtifactSuffix;
		phoenixEnrichmentProcessorName = phoenixEnrichmentProcessorName + samCustomArtifactSuffix;
		
		kinesisSourceName = kinesisSourceName + samCustomArtifactSuffix;
		s3SinkName = s3SinkName + samCustomArtifactSuffix;
		
		violationPredictionPMMLModel = violationPredictionPMMLModel + samCustomArtifactSuffix;
		
		samEnvName = samEnvName + samCustomArtifactSuffix;
		
		hdfServicePoolName = hdfServicePoolName + samCustomArtifactSuffix;
		hdpServicePoolName = hdpServicePoolName + samCustomArtifactSuffix;
		
		truckingAppAdvancedAppName = truckingAppAdvancedAppName + samCustomArtifactSuffix;
		
	}	
	
	private List<ServiceEnvironmentMapping> createServiceMappings() {
		List<ServiceEnvironmentMapping> mappings = new ArrayList<ServiceEnvironmentMapping>();
		
		mappings.add(new ServiceEnvironmentMapping(hdfServicePoolName, "STORM"));
		mappings.add(new ServiceEnvironmentMapping(hdfServicePoolName, "KAFKA"));
		mappings.add(new ServiceEnvironmentMapping(hdfServicePoolName, "ZOOKEEPER"));
		mappings.add(new ServiceEnvironmentMapping(hdfServicePoolName, "AMBARI_INFRA"));
		mappings.add(new ServiceEnvironmentMapping(hdfServicePoolName, "AMBARI_METRICS"));
		
		mappings.add(new ServiceEnvironmentMapping(hdpServicePoolName, "DRUID"));
		mappings.add(new ServiceEnvironmentMapping(hdpServicePoolName, "HBASE"));
		mappings.add(new ServiceEnvironmentMapping(hdpServicePoolName, "HDFS"));		
		return mappings;
	}	
	
	private Properties loadAppPropertiesFile(Resource envPropResource) {
		Properties envProperties = new Properties();
		
		
		if(!envPropResource.exists()) {
			String errMsg = "Env Properties file["+envPropResource.getFilename() + "] doesn't exist or cannot be loaded";
			LOG.error(errMsg);
			throw new RuntimeException(errMsg);
		}

		try {
			envProperties.load(envPropResource.getInputStream());
		} catch (IOException e) {
			String errMsg = "Cannot load App Properties file["+envPropResource.getFilename() + "]";
			LOG.error(errMsg);
			throw new RuntimeException(errMsg, e);
		}
		
		String samRestUrl = envProperties.getProperty(EnvPropertiesConstants.SAM_REST_URL);
		if(StringUtils.isEmpty(samRestUrl)) {
			String errMsg = "Property["+EnvPropertiesConstants.SAM_REST_URL +"] is required";
			throw new RuntimeException(errMsg);
		}		
		
		String samExtensionsHome = envProperties.getProperty(EnvPropertiesConstants.SAM_EXTENSIONS_HOME);
		if(StringUtils.isEmpty(samExtensionsHome)) {
			String errMsg = "Property["+EnvPropertiesConstants.SAM_EXTENSIONS_HOME +"] is required";
			throw new RuntimeException(errMsg);
		}		
		
		String samCustomArtifactSuffix = envProperties.getProperty(EnvPropertiesConstants.SAM_CUSTOM_ARTIFACT_SUFFIX);
		if(StringUtils.isEmpty(samCustomArtifactSuffix)) {
			String errMsg = "Property["+EnvPropertiesConstants.SAM_CUSTOM_ARTIFACT_SUFFIX +"] is required";
			throw new RuntimeException(errMsg);
		}		
		
		String hdfServicePoolAmbariUrl = envProperties.getProperty(EnvPropertiesConstants.HDF_SERVICE_POOL_AMBARI_URL);
		if(StringUtils.isEmpty(hdfServicePoolAmbariUrl)) {
			String errMsg = "Property["+EnvPropertiesConstants.HDF_SERVICE_POOL_AMBARI_URL +"] is required";
			throw new RuntimeException(errMsg);
		}		

		String hdpServicePoolAmbariUrl = envProperties.getProperty(EnvPropertiesConstants.HDP_SERVICE_POOL_AMBARI_URL);
		if(StringUtils.isEmpty(hdpServicePoolAmbariUrl)) {
			String errMsg = "Property["+EnvPropertiesConstants.HDP_SERVICE_POOL_AMBARI_URL +"] is required";
			throw new RuntimeException(errMsg);
		}			
		String schemaRegistryUrl = envProperties.getProperty(EnvPropertiesConstants.SAM_SCHEMA_REGISTRY_URL);
		if(StringUtils.isEmpty(schemaRegistryUrl)) {
			String errMsg = "Property["+EnvPropertiesConstants.SAM_SCHEMA_REGISTRY_URL +"] is required";
			throw new RuntimeException(errMsg);
		}		
		return envProperties;
		
	}		
	

}
