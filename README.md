# SAM Trucking Reference Application

This module is a reference application for Hortonworks Data Flow's Streaming Analytics Manager. The goal of this module is to showcase the different capabilities of HDF's Streaming Analytics Manager.

This module consists of the following:

* Versioned Artifacts of SAM Applications related the Trucking Reference Application

## HDF Version Supported
The current version of the reference application supports HDF 3.1. 


## Building the Module

### Prequisites

* Install an HDF 3.1 Cluster. Follow instructions [here](https://docs.hortonworks.com/HDPDocuments/HDF3/HDF-3.0.2/bk_installing-hdf/content/ch_install-ambari.html). 
* Install an HDP 2.6.3 Cluster. Follow instructions [here](https://docs.hortonworks.com/HDPDocuments/Ambari-2.6.0.0/bk_ambari-installation/content/ch_Getting_Ready.html)
* Create the necessary Kafka Topics and Schemas in Schema Registry. Follow instruction [here](https://docs.hortonworks.com/HDPDocuments/HDF3/HDF-3.0.2/bk_getting-started-with-stream-analytics/content/ch_prepare-your-environment.html#d6e97). 
* This project requires the [SAM Java SDK](https://github.com/georgevetticaden/sam-java-sdk) module. You will need to build it.
	* git clone https://github.com/georgevetticaden/sam-java-sdk.git
	* cd sam-java-sdk
	* mvn clean install -DskipTests=true
* This project also requires [SAM Trucking Data Utils](https://github.com/georgevetticaden/sam-trucking-data-utils) module. You will need to build it. 
	* git clone https://github.com/georgevetticaden/sam-trucking-data-utils.git
	* mvn clean install -DskipTests=true

### Setup the SAM Environment to Run the Trucking Reference App

The Trucking application requires number of SAM components including custom UDFs, custom processors, etc. To setup this environment, perform the following:

* Download the [SAM_EXTENSIONS](https://drive.google.com/file/d/1CNYcfT0yoBHbsjziikzO_W2lN3Lrtas3/view) zip file. Unzip the contents. We will call the unzipped folder $SAM_EXTENSIONS
* Modify the [ref-app-env.properties](https://github.com/georgevetticaden/sam-trucking-ref-app/blob/master/jenkins/env-properties/ref-app-env.properties) file based on your env
	* sam.rest.url = the REST url of the SAM instance in your env
	* sam.extensions.home = location of $SAM_EXTENSIONS that you unzipped
	* hdf.service.pool.ambari.url = The rest endpoint for the HDF cluster you installed
	* hdp.service.pool.ambari.url = The rest endpoint for the HDP cluster you installed
	* sam.schema.registry.url = The url of the Schema Registry service in SAM you installed as part of the HDF cluster
* Build the project
	* mvn clean package -DskipTests=true
	* cd target
	* 

	
	

	

	

	

