
package RockManager.util.stopableThread;

/**
 * 可暂停、继续、停止的线程。
 */
public class StopableThread extends Thread {

	boolean suspended;

	boolean stopped;


	/**
	 * 让线程暂停。
	 */
	public synchronized void doSuspend() {

		suspended = true;
	}


	/**
	 * 让线程继续。
	 */
	public synchronized void doResume() {

		suspended = false;
		notifyAll();
	}


	/**
	 * 让线程停止。
	 */
	public synchronized void doStop() {

		stopped = true;
		suspended = false;
		notifyAll();
	}


	/**
	 * 是否暂停了。
	 */
	public boolean isSuspended() {

		return suspended;
	}


	/**
	 * 是否停止了。
	 */
	public boolean isStopped() {

		return stopped;
	}


	/**
	 * 在run方法的循环结构中使用此方法将使线程具有继续暂停功能。
	 */
	protected synchronized void doSuspendResumeControl() {

		if (isSuspended()) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}

	}


	/**
	 * 是否可以继续运行。(stopped==false)
	 */
	protected boolean canGoOn() {

		return stopped == false;
	}

	// public void run() {
	//
	// for (int i = 0; i < 50; i++) {
	//
	// doSuspendResumeControl();
	//
	// if (isStopped()) {
	// break;
	// }
	//
	// System.out.println(i);
	//
	// try {
	//
	// Thread.sleep(50);
	//
	// } catch (InterruptedException e) {
	// }
	//
	// }
	//
	// stopped = true;
	//
	// }

}