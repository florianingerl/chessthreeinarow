package Model;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class MyThreadPool {

	private static Logger logger = Logger.getLogger(MyThreadPool.class);
	private static ExecutorService instance = null;

	public static ExecutorService getPool() {
		if (instance == null) {
			instance = new ThreadPoolExecutor(5, 20, 10000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					logger.info("Shutting down thread pool");
					instance.shutdownNow();
				}
			});
		}

		return instance;

	}

}
