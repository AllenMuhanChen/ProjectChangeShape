package org.xper.allen.console;

import java.util.List;

import org.xper.classic.vo.TrialContext;
import org.xper.util.EventUtil;

public class SaccadeEventUtil extends EventUtil{
	
	public static void fireTargetOnEvent(long timestamp,
			List<?extends TargetEventListener> targetEventListeners,
			TrialContext currentContext) {
		
			for (TargetEventListener listener: targetEventListeners) {
				listener.targetOn(timestamp, currentContext);
			}
	}
	
	public static void fireTargetOffEvent(long timestamp, List<?extends TargetEventListener> targetEventListeners,TrialContext currentContext) {
		for (TargetEventListener listener: targetEventListeners) {
			listener.targetOff(timestamp, currentContext);
		}
	}
	
}
