package hortonworks.hdf.sam.refapp.trucking.deploy;


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployTruckingRefAdvancedAppTest {

	private static final String TRUCKING_APP_PROPS_FILE = "/Users/gvetticaden/Dropbox/Hortonworks/Development/Git/sam-trucking-ref-app/src/test/resources/app-properties/trucking-ref-app-advanced.properties";
	private static final Logger LOG = LoggerFactory.getLogger(DeployTruckingRefAdvancedAppTest.class);
	
	@Test
	public void testRefAppDeployment() {
		DeployTruckingRefAdvancedApp deployerApp = new DeployTruckingRefAdvancedApp(TRUCKING_APP_PROPS_FILE);
		deployerApp.deployNewAdvancedTruckingRefApp();;
	}
	

}
