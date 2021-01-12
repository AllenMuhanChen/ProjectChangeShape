package org.xper.allen.experiment.twoac;

import org.xper.Dependency;
import org.xper.classic.MarkStimTrialDrawingController;
import org.xper.classic.TrialDrawingController;
import org.xper.classic.vo.TrialContext;
import org.xper.drawing.Context;
import org.xper.experiment.ExperimentTask;

public class TwoACMarkStimTrialDrawingController extends MarkStimTrialDrawingController{

	@Dependency
	protected TwoACTaskScene taskScene;
	
	@Override
	public void slideFinish(ExperimentTask task, TrialContext context) {
		taskScene.nextMarker();
		taskScene.drawBlank(context, false, false);
		window.swapBuffers();
	}
	
	protected void prepareSample(TwoACExperimentTask task, Context context) {
		if (task != null) {
			taskScene.setSample(task);
			taskScene.drawSample(context, true);
		} else {
			taskScene.drawBlank(context, false, false);
		}
	}
	
	protected void prepareChoice(TwoACExperimentTask task, Context context) {
		if (task != null) {
			taskScene.setChoice(task);
			taskScene.drawChoice(context, false);
		} else {
			taskScene.drawBlank(context, false, false);
		}
	}
	
	
	
	
}
