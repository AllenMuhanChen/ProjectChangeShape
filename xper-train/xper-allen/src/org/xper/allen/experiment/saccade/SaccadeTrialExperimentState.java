package org.xper.allen.experiment.saccade;

import java.util.List;

import org.xper.Dependency;
import org.xper.classic.SlideEventListener;
import org.xper.classic.vo.TrialExperimentState;

public class SaccadeTrialExperimentState extends TrialExperimentState {
	@Dependency
	List<? extends SlideEventListener> slideEventListeners;
	@Dependency
	protected int slidePerTrial;
	@Dependency
	int slideLength;
	@Dependency
	int interSlideInterval;
	@Dependency
	boolean doEmptyTask = true;
	@Dependency
	boolean repeatTrialIfEyeBreak = false; //TODO; add injection
	/**
	 * in ms
	 */
	public static final int NO_TASK_SLEEP_INTERVAL = 10;
	
	public boolean isDoEmptyTask() {
		return doEmptyTask;
	}

	public void setDoEmptyTask(boolean doEmptyTask) {
		this.doEmptyTask = doEmptyTask;
	}

	public int getInterSlideInterval() {
		return interSlideInterval;
	}

	public void setInterSlideInterval(int interSlideInterval) {
		this.interSlideInterval = interSlideInterval;
	}

	public int getSlideLength() {
		return slideLength;
	}

	public void setSlideLength(int slideLength) {
		this.slideLength = slideLength;
	}

	public int getSlidePerTrial() {
		return slidePerTrial;
	}

	public void setSlidePerTrial(int slidePerTrial) {
		this.slidePerTrial = slidePerTrial;
	}

	public List<? extends SlideEventListener> getSlideEventListeners() {
		return slideEventListeners;
	}

	public void setSlideEventListeners(
			List<? extends SlideEventListener> slideEventListeners) {
		this.slideEventListeners = slideEventListeners;
	}
	
	public SaccadeExperimentTask getCurrentTask() {
		return (SaccadeExperimentTask) currentTask;
	}
	
	public void setCurrentTask(SaccadeExperimentTask currentTask) {
		this.currentTask = currentTask;
	}
}
