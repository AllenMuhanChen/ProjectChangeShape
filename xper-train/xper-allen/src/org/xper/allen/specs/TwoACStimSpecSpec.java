package org.xper.allen.specs;

import org.xper.allen.experiment.twoac.RewardPolicy;
import org.xper.drawing.Coordinates2D;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Fields correspond with xml entries in the "spec" column in "stimspec" database table. 
 * Contains toXML and fromXML methods. 
 * @author Allen Chen
 *
 */
public class TwoACStimSpecSpec {
	//@XStreamAlias("targetEyeWinCoords")
	protected Coordinates2D[] targetEyeWinCoords;
	//@XStreamAlias("targetEyeWinSize")
	protected double[] targetEyeWinSize;
	//@XStreamAlias("sampleObjData")
	protected long sampleObjData;
	//@XStreamAlias("choiceObjData")
	protected long[] choiceObjData;
	//@XStreamAlias("eStimObjData")
	protected long[] eStimObjData;
	//@XStreamAlias("rewardPolicy")
	protected RewardPolicy rewardPolicy;
	
	//@XStreamAlias("eStimObjChans")
	//protected int[] eStimObjChans;
	
	protected transient static XStream s;
	
	static {
		s = new XStream();
		s.alias("StimSpec", TwoACStimSpecSpec.class);
	}
	
	public TwoACStimSpecSpec(Coordinates2D[] targetEyeWinCoords, double targetEyeWinSize[], long sampleObjData, 
			long[] choiceObjData, long[] eStimObjData, RewardPolicy rewardPolicy) {
		this.targetEyeWinCoords = targetEyeWinCoords;
		this.targetEyeWinSize = targetEyeWinSize;
		this.sampleObjData = sampleObjData;
		this.choiceObjData = choiceObjData;
		this.eStimObjData = eStimObjData;
		this.rewardPolicy = rewardPolicy;
	}
/*	
	public TwoACStimSpecSpec(Coordinates2D targetEyeWinCoords, double targetEyeWinSize, double duration, long stimObjData,
			 long eStimObjData) {
		//super();
		this.targetEyeWinCoords = targetEyeWinCoords;
		this.targetEyeWinSize = targetEyeWinSize;
		this.duration = duration;
		long[] stimObjDataArr = new long[1];
		stimObjDataArr[0] = stimObjData;
		this.stimObjData = stimObjDataArr;
		long[]eStimObjDataArr = new long[1];
		eStimObjDataArr[0] = eStimObjData;
		this.eStimObjData = eStimObjDataArr;
	}
*/
	
	public TwoACStimSpecSpec() {
	}


	public RewardPolicy getRewardPolicy() {
		return rewardPolicy;
	}

	public void setRewardPolicy(RewardPolicy rewardPolicy) {
		this.rewardPolicy = rewardPolicy;
	}

	public Coordinates2D[] getTargetEyeWinCoords() {
		return targetEyeWinCoords;
	}

	public void setTargetEyeWinCoords(Coordinates2D[] targetEyeWinCoords) {
		this.targetEyeWinCoords = targetEyeWinCoords;
	}

	public double[] getTargetEyeWinSize() {
		return targetEyeWinSize;
	}

	public void setTargetEyeWinSize(double[] targetEyeWinSize) {
		this.targetEyeWinSize = targetEyeWinSize;
	}

	public long getSampleObjData() {
		return sampleObjData;
	}

	public void setSampleObjData(long sampleObjData) {
		this.sampleObjData = sampleObjData;
	}

	public long[] getChoiceObjData() {
		return choiceObjData;
	}

	public void setChoiceObjData(long[] choiceObjData) {
		this.choiceObjData = choiceObjData;
	}

	public static String toXml (TwoACStimSpecSpec spec) {
		return s.toXML(spec);
	}
	
	public String toXml() {
		return TwoACStimSpecSpec.toXml(this);
	}
	
	public static TwoACStimSpecSpec fromXml (String xml) {
		TwoACStimSpecSpec ss = (TwoACStimSpecSpec)s.fromXML(xml);
		return ss;
	}


	public long[] geteStimObjData() {
		return eStimObjData;
	}

	public void seteStimObjData(long[] eStimObjData) {
		this.eStimObjData = eStimObjData;
	}



}
