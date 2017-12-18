package hortonworks.hdf.sam.refapp.trucking.deploy;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TruckingRefAppTest {

	private static final String TRUCKING_APP_PROPS_FILE = "/Users/gvetticaden/Dropbox/Hortonworks/Development/Git/sam-trucking-ref-app/src/test/resources/app-properties/trucking-ref-app.properties";
	Logger LOG = LoggerFactory.getLogger(TruckingRefAppTest.class);
	
	@Test
	public void testRefAppDeployment() {
		TruckingRefApp deployerApp = new TruckingRefApp(TRUCKING_APP_PROPS_FILE);
		deployerApp.deployNewAdvancedTruckingRefApp();;
	}
}
