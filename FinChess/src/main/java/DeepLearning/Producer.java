package DeepLearning;

import java.util.concurrent.LinkedBlockingQueue;

public interface Producer<T> {

	public void setQueue(LinkedBlockingQueue<T> queue);
	public void produce();
}
