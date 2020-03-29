package DeepLearning.UnitTests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.junit.Test;

import DeepLearning.ConsumerProducerPattern;
import DeepLearning.Producer;

public class ConsumerProducerPatternTest {

	class TestProducer implements Producer<Integer>
	{
		private LinkedBlockingQueue<Integer> queue;
		private boolean slow;
		
		public TestProducer(boolean slow)
		{
			this.slow = slow;	
		}
		
		@Override
		public void setQueue(LinkedBlockingQueue<Integer> queue) {
			this.queue = queue;
		}

		@Override
		public void produce() {
			for(int i=0; i < 100; ++i)
			{
				try {
					queue.put(i);
					if(slow) Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	class TestConsumer implements Consumer<Integer> {

		private int sum = 0;
		private boolean slow;
		
		public TestConsumer(boolean slow)
		{
			this.slow = slow;
		}
		@Override
		public void accept(Integer t) {
			// TODO Auto-generated method stub
			sum += t;
			try {
				if(slow) Thread.sleep(10);
			} catch (InterruptedException e){
				e.printStackTrace();
			}
		}
		
		public int getSum()
		{
			return sum;
		}

	};
	
	@Test
	public void produceAndConsume_WhereTheConsumerIsALotSlowerThanTheProducer_WhenProduceAndConsumerReturnsEverythingIsDone() {

		TestConsumer consumer = new TestConsumer(true);
		
		ConsumerProducerPattern<Integer> cpp = new ConsumerProducerPattern<Integer>(new TestProducer(false), consumer);
		cpp.produceAndConsume();
		
		assertEquals(4950, consumer.getSum());
		
	}
	
	@Test
	public void produceAndConsume_WhereTheProducerIsALotSlowerThanTheConsumer_WhenProduceAndConsumerReturnsEverythingIsDone() {
		
		
		
		TestConsumer consumer = new TestConsumer(false);
		ConsumerProducerPattern<Integer> cpp = new ConsumerProducerPattern<Integer>(
				new TestProducer(true), consumer);
	
		cpp.produceAndConsume();
		assertEquals(4950, consumer.getSum());
	}
	
	

}
