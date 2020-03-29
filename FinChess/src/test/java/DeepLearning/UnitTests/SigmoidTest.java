package DeepLearning.UnitTests;

import static org.junit.Assert.*;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.junit.Test;

import DeepLearning.Sigmoid;

public class SigmoidTest {

	@Test
	public void compute_Test() {
		
		double result = Sigmoid.compute(0);
		assertEquals(0.5, result, 1e-30); 
		
		
	}
	
	@Test
	public void computeComponentwise_Test()
	{
		BlockRealMatrix m = new BlockRealMatrix(new double[5][7]);
		Sigmoid.applyComponentwise(m);
		
		for(int row= 0; row < m.getRowDimension(); ++row)
		{
			for(int column = 0; column < m.getColumnDimension(); ++column)
			{
				assertEquals(0.5,m.getEntry(row,  column), 1e-30);
			}
		}
	}
	
	@Test
	public void  computeDerivative_Test()
	{
		double result = Sigmoid.computeDerivative(0);
		assertEquals(0.25, result, 1e-30);
	}

	@Test
	public void computeDerivativeComponentwise_Test()
	{
		BlockRealMatrix m = new BlockRealMatrix(new double[5][7]);
		Sigmoid.applyDerivativeComponentwise(m);
		
		for(int row= 0; row < m.getRowDimension(); ++row)
		{
			for(int column = 0; column < m.getColumnDimension(); ++column)
			{
				assertEquals(0.25, m.getEntry(row,  column), 1e-30);
			}
		}
	}
}
