package org.xper.allen.experiment.twoac;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.xper.Dependency;
import org.xper.allen.console.SaccadeEventUtil;
import org.xper.allen.console.TargetEventListener;
import org.xper.allen.db.vo.EStimObjDataEntry;
import org.xper.allen.experiment.saccade.SaccadeDatabaseTaskDataSource;
import org.xper.allen.eye.TwoACEyeTargetSelectorConcurrentDriver;
import org.xper.allen.eye.TwoACTargetSelectorResult;
import org.xper.allen.intan.SimpleEStimEventUtil;
import org.xper.allen.vo.TwoACTrialResult;
import org.xper.classic.SlideEventListener;
import org.xper.classic.SlideRunner;
import org.xper.classic.TrialDrawingController;
import org.xper.classic.TrialEventListener;
import org.xper.classic.TrialRunner;
import org.xper.classic.vo.TrialContext;
import org.xper.classic.vo.TrialExperimentState;
import org.xper.classic.vo.TrialResult;
import org.xper.experiment.ExperimentTask;
import org.xper.experiment.EyeController;
import org.xper.experiment.TaskDataSource;
import org.xper.experiment.TaskDoneCache;
import org.xper.eye.EyeMonitor;
import org.xper.eye.EyeTargetSelector;
import org.xper.eye.EyeTargetSelectorConcurrentDriver;
import org.xper.eye.TargetSelectorResult;
import org.xper.time.TimeUtil;
import org.xper.util.EventUtil;
import org.xper.util.ThreadHelper;
import org.xper.util.ThreadUtil;
import org.xper.util.TrialExperimentUtil;

import jssc.SerialPortException;

import org.xper.util.IntanUtil;
import org.xper.drawing.Coordinates2D;
import org.xper.allen.intan.EStimParameter;
import org.xper.allen.intan.SimpleEStimEventListener;

public class ChoiceInRFExperimentUtil extends TrialExperimentUtil{
	public static TwoACTrialResult doSlide(int i, TwoACExperimentState stateObject) {
		TwoACMarkStimTrialDrawingController drawingController = (TwoACMarkStimTrialDrawingController) stateObject.getDrawingController();
		TwoACExperimentTask currentTask = stateObject.getCurrentTask();
		TwoACTrialContext currentContext = (TwoACTrialContext) stateObject.getCurrentContext();
		List<? extends SlideEventListener> slideEventListeners = stateObject.getSlideEventListeners();
		List<? extends TargetEventListener> targetEventListeners = stateObject.getTargetEventListeners();
		List<? extends SimpleEStimEventListener> eStimEventListeners = stateObject.geteStimEventListeners();
		List<? extends TrialEventListener> trialEventListeners = stateObject.getTrialEventListeners();
		EyeTargetSelector targetSelector = stateObject.getTargetSelector();
		TimeUtil timeUtil = stateObject.getLocalTimeUtil();
		EyeController eyeController = stateObject.getEyeController();

		boolean fixationSuccess;
		
		TwoACTargetSelectorResult selectorResult;

		//show SAMPLE after delay
		long blankOnLocalTime = timeUtil.currentTimeMicros();
		do {
			//do nothing
		}while(timeUtil.currentTimeMicros()<blankOnLocalTime + stateObject.getBlankTargetScreenDisplayTime()*1000);

		//drawingController.prepareSample(currentTask, currentContext); //TODO: NEED THIS?
		drawingController.showSlide(currentTask, currentContext);
		long slideOnLocalTime = timeUtil.currentTimeMicros();
		currentContext.setCurrentSlideOnTime(slideOnLocalTime);
		TwoACEventUtil.fireSampleOnEvent(i, slideOnLocalTime, slideEventListeners);
		
		
		//HOLD FIXATION
		fixationSuccess = eyeController.waitEyeInAndHold(slideOnLocalTime
				+ stateObject.getSampleLength() * 1000 );

		if (!fixationSuccess) {
			// eye fail to hold
			long eyeInHoldFailLocalTime = timeUtil.currentTimeMicros();
			currentContext.setEyeInHoldFailTime(eyeInHoldFailLocalTime);
			drawingController.eyeInHoldFail(currentContext);
			EventUtil.fireEyeInHoldFailEvent(eyeInHoldFailLocalTime,
					trialEventListeners, currentContext);
			return TwoACTrialResult.EYE_IN_HOLD_FAIL;
		}
		
		
		//show CHOICE 	
		drawingController.prepareChoice(currentTask, currentContext);
		drawingController.showSlide(currentTask, currentContext);
		long choiceOnLocalTime = timeUtil.currentTimeMicros();
		TwoACEventUtil.fireChoiceOnEvent();
		
		
		//ESTIMULATOR
		sendEStimTrigger(stateObject);
		SimpleEStimEventUtil.fireEStimOn(timeUtil.currentTimeMicros(), eStimEventListeners, currentContext);
		System.out.println("Fired");
		//Eye on Target Logic
		//eye selector
		TwoACEyeTargetSelectorConcurrentDriver selectorDriver = new TwoACEyeTargetSelectorConcurrentDriver(targetSelector, timeUtil);
		currentContext.setTargetOnTime(currentContext.getCurrentSlideOnTime()); 


		//Sleep for the duration of the start delay
		//ThreadUtil.sleep(stateObject.getTargetSelectionStartDelay());

		//start(Coordinates2D[] targetCenter, double[] targetWinSize, long deadlineIntialEyeIn, long eyeHoldTime)
		
		
		selectorDriver.start(currentContext.getTargetPos(), currentContext.getTargetEyeWindowSize(),
				currentContext.getTargetOnTime() + stateObject.getTimeAllowedForInitialTargetSelection()*1000 
				+ stateObject.getTargetSelectionStartDelay() * 1000, stateObject.getRequiredTargetSelectionHoldTime() * 1000);
		SaccadeEventUtil.fireTargetOnEvent(timeUtil.currentTimeMicros(), targetEventListeners, currentContext);

		do {
			//Wait for Eye Target Selector To Finish
		}while(!selectorDriver.isDone());
		selectorDriver.stop();

		SaccadeEventUtil.fireTargetOffEvent(timeUtil.currentTimeMicros(), targetEventListeners);
		
		selectorResult = selectorDriver.getResult();
		if (selectorResult.getSelectionStatusResult() == TwoACTrialResult.TARGET_SELECTION_EYE_FAIL) {
			SaccadeEventUtil.fireTargetSelectionEyeFailEvent(timeUtil.currentTimeMicros(), targetEventListeners);
		}
		else if (selectorResult.getSelectionStatusResult() == TwoACTrialResult.TARGET_SELECTION_EYE_BREAK) {
			SaccadeEventUtil.fireTargetSelectionEyeBreakEvent(timeUtil.currentTimeMicros(), targetEventListeners);
		}
		//TODO: HANDLE BOTH ONE AND TWO EVENT UTILS
		else if (selectorResult.getSelectionStatusResult()== TwoACTrialResult.TARGET_SELECTION_ONE) {
			//TODO: New Event Util
			SaccadeEventUtil.fireTargetSelectionDoneEvent(timeUtil.currentTimeMicros(), targetEventListeners);
		}
		else if (selectorResult.getSelectionStatusResult()== TwoACTrialResult.TARGET_SELECTION_ONE) {
			//SaccadeEventUtil.fireTargetSelectionDoneEvent(timeUtil.currentTimeMicros(), targetEventListeners);
		}

		System.out.println("SelectionStatusResult = " + selectorResult.getSelectionStatusResult());
		do {
			//Wait for Slide to Finish
		}while(timeUtil.currentTimeMicros()<slideOnLocalTime+stateObject.getChoiceLength()*1000);
		//finish current slide
		drawingController.trialComplete(currentContext);
		long slideOffLocalTime = timeUtil.currentTimeMicros();
		currentContext.setCurrentSlideOffTime(slideOffLocalTime);
		EventUtil.fireSlideOffEvent(i, slideOffLocalTime,
				/*
				 * TODO: Animation frame stuff may not be needed 
				 */
				currentContext.getAnimationFrameIndex(),
				slideEventListeners);
		currentContext.setAnimationFrameIndex(0);


		return selectorResult.getSelectionStatusResult();

	}

	public static TwoACTrialResult runTrial (TwoACExperimentState stateObject, ThreadHelper threadHelper, TwoACSlideRunner runner){
		TwoACTrialResult result = ChoiceInRFExperimentUtil.getMonkeyFixation(stateObject, threadHelper);
		if (result != TwoACTrialResult.FIXATION_SUCCESS) {
			return result;
		}
		sendEStims(stateObject);
		result = runner.runSlide();
		if (result != TwoACTrialResult.TRIAL_COMPLETE) {
			return result;
		}

		ChoiceInRFExperimentUtil.completeTrial(stateObject, threadHelper);

		return TwoACTrialResult.TRIAL_COMPLETE;
	}

	public static void cleanupTrial (TwoACTrialExperimentState state) {
		TimeUtil timeUtil = state.getLocalTimeUtil();
		TwoACExperimentTask currentTask = state.getCurrentTask();
		TwoACTrialContext currentContext = (TwoACTrialContext) state.getCurrentContext();
		SaccadeDatabaseTaskDataSource taskDataSource = (SaccadeDatabaseTaskDataSource) state.getTaskDataSource();
		TaskDoneCache taskDoneCache = state.getTaskDoneCache();
		TrialDrawingController drawingController = state.getDrawingController();
		List<? extends TrialEventListener> trialEventListeners = state
				.getTrialEventListeners();

		// unget failed task
		
		if (currentTask != null) {
			taskDataSource.ungetTask(currentTask);
			
			state.setCurrentTask(null);
		}
		 

		// trial stop
		if (currentContext != null) {
			long trialStopLocalTime = timeUtil.currentTimeMicros();
			currentContext.setTrialStopTime(trialStopLocalTime);
			drawingController.trialStop(currentContext);
			EventUtil.fireTrialStopEvent(trialStopLocalTime,
					trialEventListeners, currentContext);
		}
		state.setCurrentContext(null);
	}
	/**
	 * ESTIMULATOR
	 * Send string of params for estim over to Intan
	 * @param state
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws UnknownHostException 
	 * @throws SocketException 
	 */
	public static void sendEStims (TwoACExperimentState state) {
		try {
		IntanUtil intanUtil = state.getIntanUtil();
		EStimObjDataEntry eStimObjData = state.getCurrentTask().geteStimObjDataEntry();
		System.out.println(eStimsToString(eStimObjData));
			//EStimObjDataEntry eStimObjData = state.getCurrentTask().geteStimObjDataEntry();
			System.out.println("Sending EStimSpecs to Intan");
			System.out.println(eStimsToString(eStimObjData));
			try {
				intanUtil.send(eStimsToString(eStimObjData));
				System.out.println("EStimSpecs Successfully Sent");
			} catch (IOException e) {
				System.out.println("Cannot Send EStimSpecs");
			}
		}
		catch (NullPointerException e){
		System.out.println("Cannot Send EStims Because There Is No Trial");
		}
	}

	/**
	 * ESTIMULATOR
	 * Send trigger for estim over to Intan
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws UnknownHostException 
	 * @throws SocketException 
	 * 
	 */
	public static void sendEStimTrigger(TwoACExperimentState state){
		IntanUtil intanUtil = state.getIntanUtil();
		System.out.println("Sending Trigger");
		try {
			intanUtil.trigger();	
			System.out.println("Trigger Successfully Sent");
		} catch (Exception e) {
			System.out.println("Cannot Send Trigger");
		}
		
	}
	
	private static String eStimsToString(EStimObjDataEntry eStimObjData){
		ArrayList<EStimParameter> eStimParams= new ArrayList<EStimParameter>();
		eStimParams.add(new EStimParameter("chans",eStimObjData.getChans()));
		eStimParams.add(new EStimParameter("stim_polarity",eStimObjData.get_stim_polarity()));
		eStimParams.add(new EStimParameter("trig_src",eStimObjData.get_trig_src()));
		eStimParams.add(new EStimParameter("stim_shape",eStimObjData.get_stim_shape()));
		eStimParams.add(new EStimParameter("post_trigger_delay",eStimObjData.get_post_trigger_delay()));
		eStimParams.add(new EStimParameter("pulse_repetition",eStimObjData.getPulse_repetition()));
		eStimParams.add(new EStimParameter("num_pulses",eStimObjData.get_num_pulses()));
		eStimParams.add(new EStimParameter("pulse_train_period",eStimObjData.get_pulse_train_period()));
		eStimParams.add(new EStimParameter("post_stim_refractory_period",eStimObjData.get_post_stim_refractory_period()));
		eStimParams.add(new EStimParameter("d1",eStimObjData.get_d1()));
		eStimParams.add(new EStimParameter("d2",eStimObjData.get_d2()));
		eStimParams.add(new EStimParameter("dp",eStimObjData.get_dp()));
		eStimParams.add(new EStimParameter("a1",eStimObjData.get_a1()));
		eStimParams.add(new EStimParameter("a2",eStimObjData.get_a2()));
		eStimParams.add(new EStimParameter("enable_amp_settle",eStimObjData.isEnable_amp_settle()));
		eStimParams.add(new EStimParameter("pre_stim_amp_settle",eStimObjData.get_pre_stim_amp_settle()));
		eStimParams.add(new EStimParameter("post_stim_amp_settle",eStimObjData.get_post_stim_amp_settle()));
		eStimParams.add(new EStimParameter("maintain_amp_settle_during_pulse_train",eStimObjData.get_maintain_amp_settle_during_pulse_train()));
		eStimParams.add(new EStimParameter("enable_charge_recovery",eStimObjData.isEnable_charge_recovery()));
		eStimParams.add(new EStimParameter("post_stim_charge_recovery_on",eStimObjData.get_post_stim_charge_recovery_on()));
		eStimParams.add(new EStimParameter("post_stim_charge_recovery_off",eStimObjData.get_post_stim_charge_recovery_off()));
		
		String output = new String();
		int loopindx = 0;
		for (EStimParameter param:eStimParams) {
			if(loopindx>0) {
				output = output.concat(",");
			}
			output = output.concat(param.getName());
			output = output.concat(",");
			output = output.concat(param.getValue());
			loopindx++;
			
		}
		return output;
	}
	
	//TODO: HAVE THIS SET Prepare first trial via sampleSpec and choiceSpec via new drawing controller. 
	public static TwoACTrialResult getMonkeyFixation(TwoACExperimentState state,
			ThreadHelper threadHelper) {
		TwoACMarkStimTrialDrawingController drawingController = (TwoACMarkStimTrialDrawingController) state.getDrawingController();
		TrialContext currentContext = state.getCurrentContext();
		TimeUtil timeUtil = state.getLocalTimeUtil();
		List<? extends TrialEventListener> trialEventListeners = state
				.getTrialEventListeners();
		EyeController eyeController = state.getEyeController();
		TwoACExperimentTask currentTask = state.getCurrentTask();
		
		// trial init
		long trialInitLocalTime = timeUtil.currentTimeMicros();
		currentContext.setTrialInitTime(trialInitLocalTime);
		EventUtil.fireTrialInitEvent (trialInitLocalTime, trialEventListeners, currentContext);

		// trial start
		drawingController.trialStart(currentContext);
		long trialStartLocalTime = timeUtil.currentTimeMicros();
		currentContext.setTrialStartTime(trialStartLocalTime);
		EventUtil.fireTrialStartEvent(trialStartLocalTime, trialEventListeners,
				currentContext);

		// prepare fixation point
		drawingController.prepareFixationOn(currentContext);

		// time before fixation point on
		ThreadUtil.sleepOrPinUtil(trialStartLocalTime
				+ state.getTimeBeforeFixationPointOn() * 1000, state,
				threadHelper);

		// fixation point on
		drawingController.fixationOn(currentContext);
		long fixationPointOnLocalTime = timeUtil.currentTimeMicros();
		currentContext.setFixationPointOnTime(fixationPointOnLocalTime);
		EventUtil.fireFixationPointOnEvent(fixationPointOnLocalTime,
				trialEventListeners, currentContext);

		// wait for initial eye in
		boolean success = eyeController
				.waitInitialEyeIn(fixationPointOnLocalTime
						+ state.getTimeAllowedForInitialEyeIn() * 1000);

		if (!success) {
			// eye fail to get in
			long initialEyeInFailLocalTime = timeUtil.currentTimeMicros();
			currentContext.setInitialEyeInFailTime(initialEyeInFailLocalTime);
			drawingController.initialEyeInFail(currentContext);
			EventUtil.fireInitialEyeInFailEvent(initialEyeInFailLocalTime,
					trialEventListeners, currentContext);
			return TwoACTrialResult.INITIAL_EYE_IN_FAIL;
		}

		// got initial eye in
		long eyeInitialInLoalTime = timeUtil.currentTimeMicros();
		currentContext.setInitialEyeInTime(eyeInitialInLoalTime);
		EventUtil.fireInitialEyeInSucceedEvent(eyeInitialInLoalTime,
				trialEventListeners, currentContext);

		// prepare first slide
		currentContext.setSlideIndex(0);
		currentContext.setAnimationFrameIndex(0);
		drawingController.prepareSample(currentTask, currentContext);

		// wait for eye hold
		success = eyeController.waitEyeInAndHold(eyeInitialInLoalTime
				+ state.getRequiredEyeInHoldTime() * 1000);

		if (!success) {
			// eye fail to hold
			long eyeInHoldFailLocalTime = timeUtil.currentTimeMicros();
			currentContext.setEyeInHoldFailTime(eyeInHoldFailLocalTime);
			drawingController.eyeInHoldFail(currentContext);
			EventUtil.fireEyeInHoldFailEvent(eyeInHoldFailLocalTime,
					trialEventListeners, currentContext);
			return TwoACTrialResult.EYE_IN_HOLD_FAIL;
		}

		// get fixation, start stimulus
		long eyeHoldSuccessLocalTime = timeUtil.currentTimeMicros();
		currentContext.setFixationSuccessTime(eyeHoldSuccessLocalTime);
		EventUtil.fireFixationSucceedEvent(eyeHoldSuccessLocalTime,
				trialEventListeners, currentContext);

		return TwoACTrialResult.FIXATION_SUCCESS;
	}

	public static void run(TwoACExperimentState state,
			ThreadHelper threadHelper, TwoACTrialRunner runner) {
		TimeUtil timeUtil = state.getLocalTimeUtil();
		try {
			threadHelper.started();
			System.out.println("SlideTrialExperiment started.");

			state.getDrawingController().init();
			EventUtil.fireExperimentStartEvent(timeUtil.currentTimeMicros(),
					state.getExperimentEventListeners());

			while (!threadHelper.isDone()) {
				pauseExperiment(state, threadHelper);
				if (threadHelper.isDone()) {
					break;
				}
				// one trial
				runner.runTrial();
				if (threadHelper.isDone()) {
					break;
				}
				// inter-trial interval
				long current = timeUtil.currentTimeMicros();
				ThreadUtil.sleepOrPinUtil(current
						+ state.getInterTrialInterval() * 1000, state,
						threadHelper);
			}
		} finally {
			// experiment stop event
			try {
				System.out.println("SlideTrialExperiment stopped.");
				EventUtil.fireExperimentStopEvent(timeUtil.currentTimeMicros(),
						state.getExperimentEventListeners());
				state.getDrawingController().destroy();

				threadHelper.stopped();
			} catch (Exception e) {
				logger.warn(e.getMessage());
				e.printStackTrace();
			}
		}
	}
}