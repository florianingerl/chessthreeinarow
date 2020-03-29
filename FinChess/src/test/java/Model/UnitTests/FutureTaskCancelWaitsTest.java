package Model.UnitTests;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;

import Model.FutureTaskCancelWaits;
import Model.MyThreadPool;

public class FutureTaskCancelWaitsTest {

	private boolean wasInterrupted = false;
	
	private ReentrantLock rl = new ReentrantLock();
	private Condition cv = rl.newCondition();

	@Test
	public void cancelTrue_ShouldInterruptTheThread() {

		wasInterrupted = false;
		Future<?> future = MyThreadPool.getPool().submit(new FutureTaskCancelWaits(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(100000000);
				} catch (InterruptedException e) {
					rl.lock();
					wasInterrupted = true;
					cv.signalAll();
					rl.unlock();
					return;
				}
				wasInterrupted = false;

			}

		}));

		try {
			Thread.sleep(300);
			future.cancel(true);

			rl.lock();
			while(!wasInterrupted )
			{
				cv.await();
			}
			rl.unlock();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		assertTrue(wasInterrupted);
		wasInterrupted = false;

	}

}
