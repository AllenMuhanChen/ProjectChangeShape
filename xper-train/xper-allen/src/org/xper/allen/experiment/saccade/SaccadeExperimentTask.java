package org.xper.allen.experiment.saccade;

import org.xper.allen.db.vo.EStimObjDataEntry;
import org.xper.allen.specs.GaussSpec;
import org.xper.drawing.Coordinates2D;
import org.xper.experiment.ExperimentTask;

/**
 * Holds information regarding the stimulus that does not go to the drawing controller, but something else within the experimental code. 
 * @author allenchen
 *
 */
public class SaccadeExperimentTask extends ExperimentTask {
	
	Coordinates2D targetEyeWinCoords;
	double targetEyeWinSize;
	double duration;
	EStimObjDataEntry eStimObjDataEntry;
	
	/*
	public Coordinates2D parseCoords() {
		GaussSpec g = GaussSpec.fromXml(this.getStimSpec());
		Coordinates2D coords = new Coordinates2D(g.getXCenter(),g.getYCenter());
		return coords;
	}
	*/
	public Coordinates2D getTargetEyeWinCoords() {
		return targetEyeWinCoords;
	}

	public void setTargetEyeWinCoords(Coordinates2D targetEyeWinCoords) {
		this.targetEyeWinCoords = targetEyeWinCoords;
	}
	
	public double getTargetEyeWinSize() {
		return targetEyeWinSize;
	}

	public void setTargetEyeWinSize(double targetEyeWinSize) {
		this.targetEyeWinSize = targetEyeWinSize;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	public double getDuration() {
		return duration;
	}

	public EStimObjDataEntry geteStimObjDataEntry() {
		return eStimObjDataEntry;
	}

	public void seteStimObjDataEntry(EStimObjDataEntry eStimObjDataEntry) {
		this.eStimObjDataEntry = eStimObjDataEntry;
	}
	
	

}
