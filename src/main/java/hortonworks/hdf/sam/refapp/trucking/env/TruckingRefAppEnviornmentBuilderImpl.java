package hortonworks.hdf.sam.refapp.trucking.env;



import static org.junit.Assert.assertNotNull;
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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TruckingRefAppEnviornmentBuilderImpl implements TruckingRefAppEnviornmentBuilder {
	
	
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

	public TruckingRefAppEnviornmentBuilderImpl(String samRestURL, String extensionHomeDirectory, String extensensionsVersion, String extensionsArtifactSuffix) {
		this.samRESTUrl = samRestURL;
		this.udfSDK = new SAMUDFSDKUtils(samRESTUrl);
		this.processorSDK = new SAMProcessorComponentSDKUtils(samRESTUrl);
		this.sourceSinkSDK = new SAMSourceSinkComponentSDKUtils(samRESTUrl);
		this.modelRegistrySDK = new SAMModelRegistrySDKUtils(samRESTUrl);
		
		this.samCustomArtifactHomeDir = extensionHomeDirectory;
		this.samCustomArtifactsVersions = extensensionsVersion;
		
		if(StringUtils.isNotEmpty(extensionsArtifactSuffix)) {
			this.samCustomArtifactSuffix = extensionsArtifactSuffix;
			updateEntityNames();
		}
		
	}

	protected final Logger LOG = LoggerFactory.getLogger(TruckingRefAppEnviornmentBuilderImpl.class);
	
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
		assertNotNull(roundUdf);
		LOG.info("The Round UDF created is: " + roundUdf.toString());
	}	
	
	
	public void uploadTimestampLongUDF()  {
		String udfConfigFile = samCustomArtifactHomeDir
				+ "/custom-udf/config/timestamp-long-udf.json";
		String udfJar = samCustomArtifactHomeDir
				+ "/custom-udf/sam-custom-udf-"+ samCustomArtifactsVersions +".jar";
		SAMUDF timeStampLongUdf = udfSDK.uploadUDF(udfConfigFile, udfJar);
		assertNotNull(timeStampLongUdf);
		LOG.info("The TimestmapLong UDF created is: " + timeStampLongUdf.toString());
	}	
	
	public void uploadGetWeekUDF()  {
		String udfConfigFile = samCustomArtifactHomeDir
				+ "/custom-udf/config/get-week-udf.json";
		String udfJar = samCustomArtifactHomeDir
				+ "/custom-udf/sam-custom-udf-"+ samCustomArtifactsVersions +".jar";
		SAMUDF udfAdded = udfSDK.uploadUDF(udfConfigFile, udfJar);
		assertNotNull(udfAdded);
		LOG.info("The GetWeek UDF created is: " + udfAdded.toString());
	}	
	

	
	public void uploadWeatherEnrichmentSAMProcessor() {
		String fluxFile = samCustomArtifactHomeDir + "/custom-processor/config/weather-enrichment-processor-component.json";
		String customProcessorJar = samCustomArtifactHomeDir + "/custom-processor/sam-custom-processor-"+ samCustomArtifactsVersions +".jar";
		SAMProcessorComponent samComponent = processorSDK.uploadCustomProcessor(fluxFile, customProcessorJar);
		assertNotNull(samComponent);
		LOG.info("The Weather Enrichment Processor created is: " + samComponent.toString());
	}	
	
	public void uploadNormalizeModelSAMProcessor() {
		String fluxFile = samCustomArtifactHomeDir + "/custom-processor/config/normalize-model-features-processor-component.json";
		String customProcessorJar = samCustomArtifactHomeDir + "/custom-processor/sam-custom-processor-" + samCustomArtifactsVersions +"a.jar";
		SAMProcessorComponent samComponent = processorSDK.uploadCustomProcessor(fluxFile, customProcessorJar);
		assertNotNull(samComponent);
		LOG.info("The Normalize Model Processor created is: " + samComponent.toString());
	}
	
	public void uploadPhoenixEnrichmentSAMProcessor() {
		String fluxFile = samCustomArtifactHomeDir + "/custom-processor/config/phoenix-enrichment-processor-component.json";
		String customProcessorJar = samCustomArtifactHomeDir + "/custom-processor/sam-custom-processor-"+ samCustomArtifactsVersions +"-jar-with-dependencies.jar";
		SAMProcessorComponent samComponent = processorSDK.uploadCustomProcessor(fluxFile, customProcessorJar);
		assertNotNull(samComponent);
		LOG.info("The Phoenix EnrichmentProcessor created is: " + samComponent.toString());
	}	
	
	
	public void uploadCustomKinesisSource() {
		String fluxFileLocation = samCustomArtifactHomeDir + "/custom-source/kinesis/config/kinesis-source-topology-component.json";
		String customSourceJarLocation = samCustomArtifactHomeDir + "/custom-source/kinesis/sam-custom-source-kinesis-"+ samCustomArtifactsVersions +".jar";
		SAMComponent samCustomSource = sourceSinkSDK.uploadSAMComponent(ComponentType.SOURCE, fluxFileLocation, customSourceJarLocation);
		assertNotNull(samCustomSource);
		LOG.info("The Kinesis Source creteated is: " + samCustomSource.toString());
	}
	
	public void uploadCustomS3Sink() {
		String fluxFileLocation = samCustomArtifactHomeDir + "/custom-sink/s3/config/s3-sink-topology-component.json";
		String customSinkJarLocation = samCustomArtifactHomeDir + "/custom-sink/s3/sam-custom-sink-s3-"+ samCustomArtifactsVersions +".jar";		
		SAMComponent samCustomSink = sourceSinkSDK.uploadSAMComponent(ComponentType.SINK, fluxFileLocation, customSinkJarLocation);
		assertNotNull(samCustomSink);
		LOG.info(samCustomSink.toString());
		
	}		
	
	@Test
	public void updateViolationPredictionModelToModelRegistry() {
		String violationPredictionModelFile = samCustomArtifactHomeDir + "/custom-pmml-model/DriverViolationLogisticalRegessionPredictionModel-pmml.xml";
		String violationPredictionsModelConfigFile = samCustomArtifactHomeDir + "/custom-pmml-model/config/DriverViolationLogisticalRegessionPredictionModel-config.json";
		PMMLModel violatonModel = modelRegistrySDK.addModel(violationPredictionsModelConfigFile, violationPredictionModelFile);
		assertNotNull(violatonModel);;
		LOG.debug("Violation PMML Model created: " + violatonModel);
	}	

	public void deleteAllCustomUDFsForRefApp() {
		udfSDK.deleteUDF(roundUDFName);
		udfSDK.deleteUDF(timestampLogUDFName);
		udfSDK.deleteUDF(weekUDFName);
	}	
	
	@Test
	public void deleteAllCustomProcessorsRefApp() {
		processorSDK.deleteCustomSAMProcessor(enrichWeatherProcessorName);
		processorSDK.deleteCustomSAMProcessor(normalizeModelProcessorName);
		processorSDK.deleteCustomSAMProcessor(phoenixEnrichmentProcessorName);

	}	
	
	private void deleteAllCustomModels() {
		sourceSinkSDK.deleteCustomComponent(ComponentType.SOURCE,  kinesisSourceName);
		
	}

	private void deleteAllCustomSinks() {
		sourceSinkSDK.deleteCustomComponent(ComponentType.SINK,  s3SinkName);
		
	}

	private void deleteAllCustomSources() {
		modelRegistrySDK.deleteModel(violationPredictionPMMLModel);
		
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
		
	}	
	

}
