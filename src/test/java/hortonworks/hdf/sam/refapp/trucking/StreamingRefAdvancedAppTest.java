package hortonworks.hdf.sam.refapp.trucking;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import hortonworks.hdf.sam.sdk.testcases.manager.SAMTestCaseManager;
import hortonworks.hdf.sam.sdk.testcases.manager.SAMTestCaseManagerImpl;
import hortonworks.hdf.sam.sdk.testcases.model.SamComponent;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the SAM App called: "streaming-ref-app" found here: /sam-ref-app/trucking-app/artificacts
 * @author gvetticaden
 *
 */
public class StreamingRefAdvancedAppTest {

	private static final String SAM_APP_NAME= "streaming-ref-app-advanced";
	private static final String SAM_REST_URL = "http://hdf-3-1-build3.field.hortonworks.com:7777/api/v1";	
	
	protected final Logger LOG = LoggerFactory.getLogger(StreamingRefAdvancedAppTest.class);
			
	private SAMTestCaseManager samTestCaseManager = new SAMTestCaseManagerImpl(SAM_REST_URL);
	
	
	
	//@Test
	public void testNormalTruckingEvents() throws Exception {
		String testName = "Test-Normal-Event";
		Integer testTimeOutInSeconds = 200;
		Map<String, List<SamComponent>> testCaseExecutionResults = samTestCaseManager.runTestCase(SAM_APP_NAME, testName, testTimeOutInSeconds);	
		LOG.info(testCaseExecutionResults.toString());
		
		/* Validate the fields from the two streams were joined */
		assertThat(testCaseExecutionResults.get("JOIN").size(), is(1));
		SamComponent joinComponentResult = testCaseExecutionResults.get("JOIN").get(0);
		assertNotNull(joinComponentResult);	
		Map<String, String> joinFieldAndValues = joinComponentResult.getFieldsAndValues();
		
		String speedString = joinFieldAndValues.get("speed");
		assertNotNull(speedString);
		Long speedLong = Long.valueOf(speedString);
		assertThat(speedLong, is(58L));
		
		String latitudeString = joinFieldAndValues.get("latitude");
		assertNotNull(latitudeString);
		Double latDouble = Double.valueOf(latitudeString);
		assertThat(latDouble,  is(40.7));
		
		/* Validate the that the filter worked in that no  events were considered violaiton events */
		assertNull( testCaseExecutionResults.get("Filter"));		
	}
	
	

}
