package hortonworks.hdf.sam.refapp.trucking.deploy;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TruckingRefAppDeployerAppTest {

	private static final String TRUCKING_APP_PROPS_FILE = "/Users/gvetticaden/Dropbox/Hortonworks/Development/Git/sam-trucking-ref-app/src/test/resources/app-properties/trucking-ref-app.properties";
	Logger LOG = LoggerFactory.getLogger(TruckingRefAppDeployerAppTest.class);
	
	@Test
	public void testRefAppDeployment() {
		TruckingRefAppDeployerApp deployerApp = new TruckingRefAppDeployerApp(TRUCKING_APP_PROPS_FILE);
		deployerApp.deploy();
	}
}
