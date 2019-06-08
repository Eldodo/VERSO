package utils;

import genetic.Evolutioner;

public class EvolutionTimeNotifierThread extends Thread  {
	
	boolean end = false;
	public void end(){
		end = true;
	}
	public EvolutionTimeNotifierThread(long step, Evolutioner evo) {
		this(0, -1, step, evo);
	}
	
	/**
	 * Starts now (first stamp after 'step' millis.)
	 * @param runTime
	 * @param step
	 * @param evo
	 */
	public EvolutionTimeNotifierThread(long runTime, long step, Evolutioner evo) {
		this(step, runTime, step, evo);
	}
	/**
	 * Starts within a delay.
	 * @param delay
	 * @param runTime
	 * @param step
	 * @param evo
	 */
	public EvolutionTimeNotifierThread(long delay, long runTime, long step, Evolutioner evo) {
		this.delay = delay;
		this.runTime = runTime;
		this.step = step;
		this.evo = evo;
		if(delay <= 0 && step <= 0) end();//TODO move this in order to allow a single notification after the DELAY time
		else {
			setPriority(MIN_PRIORITY);
			this.start();
		}
	}
		
	Evolutioner evo;
	long delay = 0, runTime = -1, step = 0;
	@Override
	 public void run() {
			try {
				Thread.sleep(delay);//Starts after a delay
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		    long start = System.currentTimeMillis();
		    do {
		      try {
		    	 evo.notifyParetoListeners(System.currentTimeMillis()-start, true);
		    	 Thread.sleep(step); 
		      }
		      catch (InterruptedException ex) {}
		    } while( (runTime < 0 || System.currentTimeMillis() < ( start + runTime)) && !end );//run during <runTime> (or never stops if runTime \0)
//		    System.out.println("Thread ends !");
	 }
	
}
