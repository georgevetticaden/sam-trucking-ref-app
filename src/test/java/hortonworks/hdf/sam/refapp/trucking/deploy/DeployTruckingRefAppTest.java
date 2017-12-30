package hortonworks.hdf.sam.refapp.trucking.deploy;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployTruckingRefAppTest {

	private static final String TRUCKING_APP_PROPS_FILE = "/Users/gvetticaden/Dropbox/Hortonworks/Development/Git/sam-trucking-ref-app/jenkins/app-properties/trucking-ref-app.properties";
	Logger LOG = LoggerFactory.getLogger(DeployTruckingRefAppTest.class);
	
	@Test
	public void testRefAppDeployment() {
		DeployTruckingRefApp deployerApp = new DeployTruckingRefApp(TRUCKING_APP_PROPS_FILE);
		deployerApp.deployNewTruckingRefApp();;
	}
}
