package DeepLearning;

import org.apache.commons.math3.linear.BlockRealMatrix;

public class MatrixUtilities {

	public static BlockRealMatrix multiplyComponentwise(BlockRealMatrix m1, BlockRealMatrix m2)
	{
		assert m1.getRowDimension() == m2.getRowDimension();
		assert m1.getColumnDimension() == m2.getColumnDimension();
		
		int n = m1.getRowDimension();
		int m = m1.getColumnDimension();
		
		double [][] result = new double[n][m];
		for(int row=0; row < n; ++row)
		{
			for(int column=0; column < m; ++column)
			{
				result[row][column] = m1.getEntry(row, column) * m2.getEntry(row, column);
			}
		}
		return new BlockRealMatrix(result);
	}
	
}
