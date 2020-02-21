package org.xper.allen.app.blockGenerators.trials;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

public abstract class Trial {
	long[] stimObjData;
	long[] eStimObjData;
	int[] eStimObjChans;

	public long[] getStimObjData() {
		return stimObjData;
	}
	public void setStimObjData(long[] stimObjData) {
		this.stimObjData = stimObjData;
	}
	public long[] getEStimObjData() {
		return eStimObjData;
	}
	public void setEStimObjData(long[] eStimObjData) {
		this.eStimObjData = eStimObjData;
	}
	
	public int[] geteStimObjChans() {
		return eStimObjChans;
	}
	public void seteStimObjChans(int[] eStimObjChans) {
		this.eStimObjChans = eStimObjChans;
	}
	
	

	transient static XStream s;
	
	static {
		s = new XStream();
		s.alias("StimSpec", Trial.class);
	}
	
	public String toXml() {
		return s.toXML(this);
	}

}
