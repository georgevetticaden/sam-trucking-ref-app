package hortonworks.hdf.sam.refapp.trucking;


import java.sql.Timestamp;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class DateUtilsTest {

	private static final Logger LOG = LoggerFactory.getLogger(DateUtilsTest.class);
	
	@Test
	public void longtoStringDate() {
		long dateLong = 1506455684563L;
		Timestamp geoDateTimeStamp2 = new Timestamp(dateLong);
		LOG.info("Date String is: " + geoDateTimeStamp2.toString()); 
	}	
	
	@Test
	public void stringDateToLong() {
		String dateString = "2017-09-26 14:54:44.900";
		Timestamp geoDateTimeStamp2 = Timestamp.valueOf(dateString);
		LOG.info("Long is : " + geoDateTimeStamp2.getTime()); 
	}	
	
	
}
