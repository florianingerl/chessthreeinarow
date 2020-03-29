package DeepLearning;

import org.apache.commons.math3.linear.BlockRealMatrix;

public class Sigmoid {

	public static double compute(double x)
	{
		return 1/(1  + Math.exp(-x));
	}
	
	public static void applyComponentwise(BlockRealMatrix x)
	{
		int rowDimension = x.getRowDimension();
		int columnDimension = x.getColumnDimension();
		
		for(int row = 0; row < rowDimension; ++row)
		{
			for(int column = 0; column < columnDimension; ++column)
			{
				x.setEntry(row, column, compute(x.getEntry(row,  column)) );
			}
		}
		
		
	}
	
	public static double computeDerivative(double x)
	{
		return compute(x)*(1 - compute(x));
	}
	
	public static void applyDerivativeComponentwise(BlockRealMatrix x)
	{
		
		int rowDimension = x.getRowDimension();
		int columnDimension = x.getColumnDimension();
		
		for(int row = 0; row < rowDimension; ++row)
		{
			for(int column = 0; column < columnDimension; ++column)
			{
				x.setEntry(row, column, computeDerivative(x.getEntry(row,  column)) );
			}
		}
	}
	
}
