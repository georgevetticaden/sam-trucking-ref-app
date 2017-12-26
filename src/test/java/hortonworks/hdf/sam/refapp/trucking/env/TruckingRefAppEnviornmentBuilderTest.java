package hortonworks.hdf.sam.refapp.trucking.env;

import org.junit.Before;
import org.junit.Test;

import hortonworks.hdf.sam.refapp.trucking.env.TruckingRefAppEnviornmentBuilder;
import hortonworks.hdf.sam.refapp.trucking.env.TruckingRefAppEnviornmentBuilderImpl;

public class TruckingRefAppEnviornmentBuilderTest {
	
	
	protected static final String SAM_REST_URL = "http://hdf-3-1-build3.field.hortonworks.com:7777/api/v1";	
	protected static final String SAM_EXTENSIONS_HOME = "/Users/gvetticaden/Dropbox/Hortonworks/HDP-Emerging-Products/A-HDF/A-Master-Demo/all-custom-extensions/3.1/3.1.0.0-412/Sam-Custom-Extensions";
	protected static final String SAM_EXTENSIONS_VERSION = "3.1.0.0-412";
	protected static final String SAM_CUSTOM_ARTIFACT_SUFFIX="_AUTOCREATED";
	private static final String HDF_SERVICE_POOL_AMBRI_URL = "http://hdf-3-1-build0.field.hortonworks.com:8080/api/v1/clusters/streamanalytics";	
	private static final String HDP_LAKE_SERVICE_POOL_AMBRI_URL = "http://hdp-2-6-3-ga0.field.hortonworks.com:8080/api/v1/clusters/datalake";
	private static final String SCHEMA_REGISTRY_URL = "http://hdf-3-1-build3.field.hortonworks.com:7788/api/v1";	

	
	TruckingRefAppEnviornmentBuilder envBuilder;
	

	
	@Test
	public void buildEnvironment() {
		envBuilder.buildEnvironment();
	}
	
	@Test
	public void tearDownEnvironment() {
		envBuilder.tearDownEnvironment();
	}
	
	@Before
	public void setup() {
		envBuilder = new TruckingRefAppEnviornmentBuilderImpl(SAM_REST_URL, SAM_EXTENSIONS_HOME, SAM_EXTENSIONS_VERSION, SAM_CUSTOM_ARTIFACT_SUFFIX, HDF_SERVICE_POOL_AMBRI_URL, HDP_LAKE_SERVICE_POOL_AMBRI_URL, SCHEMA_REGISTRY_URL);
	}	

}
