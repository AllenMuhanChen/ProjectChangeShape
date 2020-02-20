package org.xper.allen.experiment.saccade;

import java.util.List;

import org.apache.log4j.Logger;
import org.xper.Dependency;
import org.xper.allen.config.AllenDbUtil;
import org.xper.classic.SlideRunner;
import org.xper.classic.TrialDrawingController;
import org.xper.classic.TrialEventListener;
import org.xper.classic.TrialRunner;
import org.xper.classic.vo.SlideTrialExperimentState;
import org.xper.classic.vo.TrialResult;
import org.xper.drawing.Coordinates2D;
import org.xper.experiment.Experiment;
import org.xper.experiment.EyeController;
import org.xper.experiment.TaskDoneCache;
import org.xper.eye.EyeMonitor;
import org.xper.eye.EyeTargetSelector;
import org.xper.eye.EyeTargetSelectorConcurrentDriver;
import org.xper.eye.TargetSelectorResult;
import org.xper.time.TimeUtil;
import org.xper.util.ThreadHelper;
import org.xper.util.ThreadUtil;
import org.xper.util.TrialExperimentUtil;


/**
 * Format of StimSpec:
 * 
 * <StimSpec animation="true"> ... </StimSpec>
 * 
 * If attribute animation is false or missing, the stimulus is treated as a
 * static slide.
 * 
 * @author wang
 * 
 */
public class SaccadeTrialExperiment implements Experiment {
	static Logger logger = Logger.getLogger(SaccadeTrialExperiment.class);

	ThreadHelper threadHelper = new ThreadHelper("SaccadeTrialExperiment", this);
	@Dependency
	EyeMonitor eyeMonitor;
	@Dependency
	SaccadeExperimentState stateObject;
	@Dependency
	AllenDbUtil dbUtil;


	@Dependency
	int blankTargetScreenDisplayTime; //in ms
	
	public boolean isRunning() {
		return threadHelper.isRunning();
	}

	public void start() {
		threadHelper.start();
	}

	public void run() {
		SaccadeTrialExperimentUtil.run(stateObject, threadHelper, new TrialRunner() {
			public TrialResult runTrial() {
				try {
					// get a task
					SaccadeTrialExperimentUtil.getNextTask(stateObject);

					if (stateObject.getCurrentTask() == null && !stateObject.isDoEmptyTask()) {
						try {
							Thread.sleep(SlideTrialExperimentState.NO_TASK_SLEEP_INTERVAL);
						} catch (InterruptedException e) {
						}
						return TrialResult.NO_MORE_TASKS;
					}

					// initialize trial context
					SaccadeTrialContext context = new SaccadeTrialContext();
					context.setCurrentTask(stateObject.getCurrentTask());
					stateObject.setCurrentContext(context);
					stateObject.getCurrentContext().setCurrentTask(stateObject.getCurrentTask());
					/*
					TrialExperimentUtil.checkCurrentTaskAnimation(stateObject);
					*/
					//target info -AC
					Coordinates2D targetPosition = context.getCurrentTask().parseCoords();
					//TODO: when come back: add logic of getting target window size from stimSpec
					float targetEyeWinSize = dbUtil.ReadEyeWinSize(context.getCurrentTask().getStimId());
					context.setTargetPos(targetPosition);
					context.setTargetEyeWindowSize(targetEyeWinSize);
					
					// run trial
					return SaccadeTrialExperimentUtil.runTrial(stateObject, threadHelper, new SlideRunner() { //TODO: Possibly 		ret = TrialExperimentUtil.runTrial(stateObject, threadHelper, new SlideRunner() {

						public TrialResult runSlide() {
							int slidePerTrial = stateObject.getSlidePerTrial();
							TrialDrawingController drawingController = stateObject.getDrawingController();
							SaccadeExperimentTask currentTask = (SaccadeExperimentTask) stateObject.getCurrentTask();
							SaccadeTrialContext currentContext = (SaccadeTrialContext) stateObject.getCurrentContext();	
							TaskDoneCache taskDoneCache = stateObject.getTaskDoneCache();
							TimeUtil globalTimeClient = stateObject.getGlobalTimeClient();
							TimeUtil timeUtil = stateObject.getLocalTimeUtil();
							EyeTargetSelector targetSelector = stateObject.getTargetSelector();
							List<? extends TrialEventListener> trialEventListeners = stateObject.getTrialEventListeners();
							TrialResult result = TrialResult.FIXATION_SUCCESS;
							EyeController eyeController = stateObject.getEyeController();
							
							try {
								for (int i = 0; i < slidePerTrial; i++) {
									
									// draw the slide
									result = SaccadeTrialExperimentUtil.doSlide(i, stateObject);
									/*
									if (result != TrialResult.SLIDE_OK) {
										return result;
									}
									*/
									
									//Eye on Target Logic
									//eye selector
									EyeTargetSelectorConcurrentDriver selectorDriver = new EyeTargetSelectorConcurrentDriver(targetSelector, timeUtil);
									currentContext.setTargetOnTime(currentContext.getCurrentSlideOffTime()); 
									
									
									//Sleep for the duration of the start delay
									//ThreadUtil.sleep(stateObject.getTargetSelectionStartDelay());
									
									//start(Coordinates2D[] targetCenter, double[] targetWinSize, long deadlineIntialEyeIn, long eyeHoldTime)
									/*selectorDriver.start(new Coordinates2D[] {currentContext.getTargetPos()}, new double[] {currentContext.getTargetEyeWindowSize()},
													     currentContext.getTargetOnTime() + stateObject.getTimeAllowedForInitialTargetSelection()*1000 
													     + stateObject.getTargetSelectionStartDelay() * 1000, stateObject.getRequiredTargetSelectionHoldTime() * 1000);
									*/
									selectorDriver.start(new Coordinates2D[] {currentContext.getTargetPos()}, new double[] {10},
										     currentContext.getTargetOnTime() + stateObject.getTimeAllowedForInitialTargetSelection()*1000 
										     + stateObject.getTargetSelectionStartDelay() * 1000, stateObject.getRequiredTargetSelectionHoldTime() * 1000);
									/*
									System.out.println("getTargetPos: ["+ currentContext.getTargetPos().getX()+","+currentContext.getTargetPos().getY()+"]");
									System.out.println("getTargetOnTime: " + currentContext.getTargetOnTime());
									System.out.println("getTimeAllowedForInitialTargetSelection: " + stateObject.getTimeAllowedForInitialTargetSelection()*1000);
									System.out.println("getTargetSelectionStartDelay: " + stateObject.getTargetSelectionStartDelay() * 1000);
									System.out.println("getRequiredTargetSelectionHoldTime: " + stateObject.getRequiredTargetSelectionHoldTime() * 1000);
									*/
									do {
										//System.out.println("Selector Driver Is Working");
									}
									while(!selectorDriver.isDone());
									selectorDriver.stop();
									TargetSelectorResult selectorResult = selectorDriver.getResult();
									System.out.println("SelectionStatusResult = " + selectorResult.getSelectionStatusResult());
	
									if (selectorDriver.getResult().getSelectionStatusResult()!= TrialResult.TARGET_SELECTION_DONE) {
										return selectorDriver.getResult().getSelectionStatusResult();
									}
									
									// slide done successfully
									if (currentTask != null) {
										taskDoneCache.put(currentTask, globalTimeClient
												.currentTimeMicros(), false);
										currentTask = null;
										stateObject.setCurrentTask(currentTask);
									}

								}
								return TrialResult.TRIAL_COMPLETE;
								// end of SlideRunner.runSlide
							} finally {
								try {
									TrialExperimentUtil.cleanupTask(stateObject);
								} catch (Exception e) {
									logger.warn(e.getMessage());
									e.printStackTrace();
								}
							}
						}
						
					}); // end of TrialExperimentUtil.runTrial 
					// end of TrialRunner.runTrial	
				} finally {
					try {
						SaccadeTrialExperimentUtil.cleanupTrial(stateObject);
					} catch (Exception e) {
						logger.warn(e.getMessage());
						e.printStackTrace();
					}
				}
			}}
		);
	}

	public void stop() {
		System.out.println("Stopping SlideTrialExperiment ...");
		if (isRunning()) {
			threadHelper.stop();
			threadHelper.join();
		}
	}

	public SaccadeExperimentState getStateObject() {
		return stateObject;
	}

	public void setStateObject(SaccadeExperimentState stateObject) {
		this.stateObject = stateObject;
	}

	public void setPause(boolean pause) {
		stateObject.setPause(pause);
	}

	public int getBlankTargetScreenDisplayTime() {
		return blankTargetScreenDisplayTime;
	}

	public void setBlankTargetScreenDisplayTime(int blankTargetScreenDisplayTime) {
		this.blankTargetScreenDisplayTime = blankTargetScreenDisplayTime;
	}

	public AllenDbUtil getDbUtil() {
		return dbUtil;
	}

	public void setDbUtil(AllenDbUtil dbUtil) {
		this.dbUtil = dbUtil;
	}
	public EyeMonitor getEyeMonitor() {
		return eyeMonitor;
	}
	public void setEyeMonitor(EyeMonitor eyeMonitor) {
		this.eyeMonitor = eyeMonitor;
	}
}
