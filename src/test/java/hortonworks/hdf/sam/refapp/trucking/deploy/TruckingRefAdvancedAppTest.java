package hortonworks.hdf.sam.refapp.trucking.deploy;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TruckingRefAdvancedAppTest {

	private static final String TRUCKING_APP_PROPS_FILE = "/Users/gvetticaden/Dropbox/Hortonworks/Development/Git/sam-trucking-ref-app/src/test/resources/app-properties/trucking-ref-app-advanced.properties";
	Logger LOG = LoggerFactory.getLogger(TruckingRefAdvancedAppTest.class);
	
	@Test
	public void testRefAppDeployment() {
		TruckingRefAdvancedApp deployerApp = new TruckingRefAdvancedApp(TRUCKING_APP_PROPS_FILE);
		deployerApp.deployNewAdvancedTruckingRefApp();;
	}
}
