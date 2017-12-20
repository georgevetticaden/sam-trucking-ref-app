package hortonworks.hdf.sam.refapp.trucking.env;

import org.junit.Before;
import org.junit.Test;

import hortonworks.hdf.sam.refapp.trucking.env.TruckingRefAppEnviornmentBuilder;
import hortonworks.hdf.sam.refapp.trucking.env.TruckingRefAppEnviornmentBuilderImpl;

public class TruckingRefAppEnviornmentBuilderTest {
	
	
	protected static final String SAM_REST_URL = "http://hdf-3-1-build3.field.hortonworks.com:7777/api/v1";	
	protected static final String SAM_EXTENSIONS_HOME = "/Users/gvetticaden/Dropbox/Hortonworks/HDP-Emerging-Products/A-HDF/A-Master-Demo/all-custom-extensions/3.1/3.1.0.0-404/Sam-Custom-Extensions";
	protected static final String SAM_EXTENSIONS_VERSION = "3.1.0.0-404";
	protected static final String SAM_CUSTOM_ARTIFACT_SUFFIX="_AUTOCREATED";
	
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
		envBuilder = new TruckingRefAppEnviornmentBuilderImpl(SAM_REST_URL, SAM_EXTENSIONS_HOME, SAM_EXTENSIONS_VERSION, SAM_CUSTOM_ARTIFACT_SUFFIX);
	}	

}
