package org.xper.allen.blockgen;

import org.xper.allen.db.vo.EStimObjDataEntry;
import org.xper.allen.specs.GaussSpec;
import org.xper.drawing.Coordinates2D;

import com.thoughtworks.xstream.XStream;

public class VisualTrial extends VEStimTrial implements Trial {
	
	transient XStream s = new XStream();
	
	public VisualTrial(GaussSpec gaussSpec, Coordinates2D targetEyeWinCoords, double targetEyeWinSize, double duration, String data) {
		super(null, gaussSpec, targetEyeWinCoords, targetEyeWinSize, duration, data);
		eStimSpec = catchEStimSpec();

	}
	
	private EStimObjDataEntry catchEStimSpec() {
		return new EStimObjDataEntry();
	}
	
	public String toXml() {
		return s.toXML(this);
	}


	
}


