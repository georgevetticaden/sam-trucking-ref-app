package hortonworks.hdf.sam.refapp.trucking.env;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import hortonworks.hdf.sam.refapp.trucking.env.TruckingRefAppEnviornmentBuilder;
import hortonworks.hdf.sam.refapp.trucking.env.TruckingRefAppEnviornmentBuilderImpl;

public class TruckingRefAppEnviornmentBuilderTest {

	
	private static TruckingRefAppEnviornmentBuilder envBuilder;

	@BeforeClass
	public static void setup() {
		Resource resource = new ClassPathResource("/app-properties/trucking-ref-app-advanced.properties");
		envBuilder = new TruckingRefAppEnviornmentBuilderImpl(resource);
	}	

	
	@Test
	public void buildEnvironment() {
		envBuilder.buildEnvironment();
	}
	
	@Test
	public void tearDownEnvironment() {
		envBuilder.tearDownEnvironment();
	}
	
	
	@Test
	public void deleteCustomSourcesAndSinks() {
		((TruckingRefAppEnviornmentBuilderImpl)envBuilder).deleteAllCustomSources();
		((TruckingRefAppEnviornmentBuilderImpl)envBuilder).deleteAllCustomSinks();
		
	}

}
