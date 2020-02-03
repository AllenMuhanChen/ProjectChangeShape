package org.xper.allen.app.stimInserters;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.xper.allen.config.AllenDbUtil;
import org.xper.allen.experiment.EStimObjDataGenerator;


//Generates ands Inserts a Specified Gauss spec into StimObjData
public class InsertEStimSpec {

	public static void main(String[] args) {
		//Setting up DataSource
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3306/V1Microstim?useSSL=false");
		dataSource.setUsername("xper_rw");
		dataSource.setPassword("up2nite");
		AllenDbUtil dbutil = new AllenDbUtil();
		dbutil.setDataSource(dataSource);
		
		//Main Code
		//Generate EStim
		EStimObjDataGenerator egenerator = new EStimObjDataGenerator();
		
		//MODIFY PARAMS HERE
		//egenerator.setChan(0);
		
		
		//Insert
		dbutil.writeEStimObjData(0,egenerator.generate());
		System.out.println("Finished Generating");
	}

}