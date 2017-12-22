package hortonworks.hdf.sam.refapp.trucking.env;



import hortonworks.hdf.sam.refapp.trucking.deploy.AppPropertiesConstants;
import hortonworks.hdf.sam.sdk.app.manager.SAMAppManagerImpl;
import hortonworks.hdf.sam.sdk.app.model.SAMApplicationStatus;
import hortonworks.hdf.sam.sdk.component.SAMProcessorComponentSDKUtils;
import hortonworks.hdf.sam.sdk.component.SAMSourceSinkComponentSDKUtils;
import hortonworks.hdf.sam.sdk.component.model.ComponentType;
import hortonworks.hdf.sam.sdk.component.model.SAMComponent;
import hortonworks.hdf.sam.sdk.component.model.SAMProcessorComponent;
import hortonworks.hdf.sam.sdk.modelregistry.SAMModelRegistrySDKUtils;
import hortonworks.hdf.sam.sdk.modelregistry.model.PMMLModel;
import hortonworks.hdf.sam.sdk.udf.SAMUDFSDKUtils;
import hortonworks.hdf.sam.sdk.udf.model.SAMUDF;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class TruckingRefAppEnviornmentBuilderImpl implements TruckingRefAppEnviornmentBuilder {
	
	
	protected final Logger LOG = LoggerFactory.getLogger(TruckingRefAppEnviornmentBuilderImpl.class);
	private static final int KILL_TIMEOUT_SECONDS = 35;
	private static final int DEPLOY_TIMEOUT_SECONDS = 35;
	
	
	/* Constructor Args Required for teh Builder */
	private String samRESTUrl;
	private String samCustomArtifactHomeDir;
	private String samCustomArtifactSuffix = "";
	private String samCustomArtifactsVersions;
	
	/* SDK clients to different SAM Entities */
	private SAMUDFSDKUtils udfSDK;
	private SAMProcessorComponentSDKUtils processorSDK;
	private SAMSourceSinkComponentSDKUtils sourceSinkSDK;
	private SAMModelRegistrySDKUtils modelRegistrySDK;	
	private SAMAppManagerImpl samAppManager;	
	
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
	private String samEnvName = "Dev";	

	public TruckingRefAppEnviornmentBuilderImpl(String samRestURL, String extensionHomeDirectory, String extensensionsVersion, String extensionsArtifactSuffix) {
		this.samRESTUrl = samRestURL;
		this.udfSDK = new SAMUDFSDKUtils(samRESTUrl);
		this.processorSDK = new SAMProcessorComponentSDKUtils(samRESTUrl);
		this.sourceSinkSDK = new SAMSourceSinkComponentSDKUtils(samRESTUrl);
		this.modelRegistrySDK = new SAMModelRegistrySDKUtils(samRESTUrl);
		this.samAppManager = new SAMAppManagerImpl(samRESTUrl);
		
		this.samCustomArtifactHomeDir = extensionHomeDirectory;
		this.samCustomArtifactsVersions = extensensionsVersion;
		
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
		
		
		uploadAllCustomUDFsForRefApp();
		uploadAllCustomSources();
		uploadAllCustomSinks();
		uploadAllModels();
		uploadAllCustomProcessorsForRefApp();
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
				+ "/custom-udf/sam-custom-udf-"+ samCustomArtifactsVersions +".jar";
		SAMUDF roundUdf = udfSDK.uploadUDF(udfConfigFile, udfJar);
		LOG.info("The Round UDF created is: " + roundUdf.toString());
	}	
	
	
	public void uploadTimestampLongUDF()  {
		String udfConfigFile = samCustomArtifactHomeDir
				+ "/custom-udf/config/timestamp-long-udf.json";
		String udfJar = samCustomArtifactHomeDir
				+ "/custom-udf/sam-custom-udf-"+ samCustomArtifactsVersions +".jar";
		SAMUDF timeStampLongUdf = udfSDK.uploadUDF(udfConfigFile, udfJar);
		LOG.info("The TimestmapLong UDF created is: " + timeStampLongUdf.toString());
	}	
	
	public void uploadGetWeekUDF()  {
		String udfConfigFile = samCustomArtifactHomeDir
				+ "/custom-udf/config/get-week-udf.json";
		String udfJar = samCustomArtifactHomeDir
				+ "/custom-udf/sam-custom-udf-"+ samCustomArtifactsVersions +".jar";
		SAMUDF udfAdded = udfSDK.uploadUDF(udfConfigFile, udfJar);
		LOG.info("The GetWeek UDF created is: " + udfAdded.toString());
	}	
	

	
	public void uploadWeatherEnrichmentSAMProcessor() {
		String fluxFile = samCustomArtifactHomeDir + "/custom-processor/config/weather-enrichment-processor-component.json";
		String customProcessorJar = samCustomArtifactHomeDir + "/custom-processor/sam-custom-processor-"+ samCustomArtifactsVersions +".jar";
		SAMProcessorComponent samComponent = processorSDK.uploadCustomProcessor(fluxFile, customProcessorJar);
		LOG.info("The Weather Enrichment Processor created is: " + samComponent.toString());
	}	
	
	public void uploadNormalizeModelSAMProcessor() {
		String fluxFile = samCustomArtifactHomeDir + "/custom-processor/config/normalize-model-features-processor-component.json";
		String customProcessorJar = samCustomArtifactHomeDir + "/custom-processor/sam-custom-processor-" + samCustomArtifactsVersions +"a.jar";
		SAMProcessorComponent samComponent = processorSDK.uploadCustomProcessor(fluxFile, customProcessorJar);
		LOG.info("The Normalize Model Processor created is: " + samComponent.toString());
	}
	
	public void uploadPhoenixEnrichmentSAMProcessor() {
		
		LOG.info("Starting uploading of PHoenixEnirchment Processer. This will take a few minutes");
		String fluxFile = samCustomArtifactHomeDir + "/custom-processor/config/phoenix-enrichment-processor-component.json";
		String customProcessorJar = samCustomArtifactHomeDir + "/custom-processor/sam-custom-processor-"+ samCustomArtifactsVersions +"-jar-with-dependencies.jar";
		SAMProcessorComponent samComponent = processorSDK.uploadCustomProcessor(fluxFile, customProcessorJar);
		LOG.info("The Phoenix EnrichmentProcessor created is: " + samComponent.toString());
	}	
	
	
	public void uploadCustomKinesisSource() {
		String fluxFileLocation = samCustomArtifactHomeDir + "/custom-source/kinesis/config/kinesis-source-topology-component.json";
		String customSourceJarLocation = samCustomArtifactHomeDir + "/custom-source/kinesis/sam-custom-source-kinesis-"+ samCustomArtifactsVersions +".jar";
		SAMComponent samCustomSource = sourceSinkSDK.uploadSAMComponent(ComponentType.SOURCE, fluxFileLocation, customSourceJarLocation);
		LOG.info("The Kinesis Source creteated is: " + samCustomSource.toString());
	}
	
	public void uploadCustomS3Sink() {
		String fluxFileLocation = samCustomArtifactHomeDir + "/custom-sink/s3/config/s3-sink-topology-component.json";
		String customSinkJarLocation = samCustomArtifactHomeDir + "/custom-sink/s3/sam-custom-sink-s3-"+ samCustomArtifactsVersions +".jar";		
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
		
		truckingAppAdvancedAppName = truckingAppAdvancedAppName + samCustomArtifactSuffix;
		
	}	
	

}
