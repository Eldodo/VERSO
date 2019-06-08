package verso.representation.cubeLandscape;

import verso.representation.cubeLandscape.representationModel.SystemRepresentation;
import verso.representation.cubeLandscape.representationModel.link.EdgeBundleLinkRepresentation;
import verso.representation.cubeLandscape.representationModel.link.LinkRepresentation;

public class UpdateEdgeBundles extends Thread {
	private int timeToSleep;
	private SystemRepresentation sys;
	
	
	
	public UpdateEdgeBundles(SystemRepresentation sys, int timeToSleep) {
		this.sys = sys;
		this.timeToSleep = timeToSleep;
	}
	
	public SystemRepresentation getSystem() {
		return this.sys;
	}
	
	public void setSystem(SystemRepresentation sys) {
		this.sys = sys;
	}
	
	public int getTimeToSleep() {
		return this.timeToSleep;
	}
	
	public void setTimeToSleep(int timeToSleep) {
		this.timeToSleep = timeToSleep;
	}
	
	public void run() {
		int tempTimeToSleep;
		while (timeToSleep > 0 && !this.isInterrupted()) {
			try {
				tempTimeToSleep = timeToSleep;
				timeToSleep = 0;
				Thread.sleep(tempTimeToSleep);
			}
			catch (InterruptedException e) {
				System.out.println("Thread interrupted");
			}
		}
		
		
		int currNbreSegments = this.sys.minNbreSegments;
		
		while (!this.isInterrupted() && currNbreSegments < this.sys.maxNbreSegments) {
			this.sys.displayRoughEdgeBundles = false;
			
			currNbreSegments += this.sys.segmentsInterval;
			
			for (LinkRepresentation link : this.sys.getLinks()) {
				if (link instanceof EdgeBundleLinkRepresentation) {
					((EdgeBundleLinkRepresentation)link).setNbreSegments(currNbreSegments);
				}
				
				if (this.isInterrupted()) {
					break;
				}
			}
		}
	}
}
