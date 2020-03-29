package DeepLearning;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.apache.logging.log4j.Logger;

public class ConsumerProducerPattern<T> {

	private Producer<T> producer;
	private Consumer<T> consumer;

	private LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<T>(500);

	public ConsumerProducerPattern(Producer<T> producer, Consumer<T> consumer) {
		this.producer = producer;
		this.consumer = consumer;
	}

	public void produceAndConsume() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				while (true) {
					T t = null;

					try {
						t = queue.take();
					} catch (InterruptedException e) {
						return;
					}
					consumer.accept(t);
				}
			}
		};
		thread.start();
		producer.setQueue(queue);
		producer.produce();
		
		waitUntilQueueIsEmpty();

		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void waitUntilQueueIsEmpty() {
		while (!queue.isEmpty()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
}
