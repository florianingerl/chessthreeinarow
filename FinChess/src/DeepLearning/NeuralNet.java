package DeepLearning;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.apache.commons.math3.linear.BlockRealMatrix;

public class NeuralNet {

	private BlockRealMatrix w2;
	private BlockRealMatrix w3;
	
	private BlockRealMatrix b2;
	private BlockRealMatrix b3;
	
	private Random random = new Random();
	
	private double nu = 0.01;
	
	private BlockRealMatrix oneVector;
	
	public NeuralNet(int L, int m, int n)
	{
		
		oneVector = new BlockRealMatrix(new double[n][1]);
		assert oneVector.getColumnDimension() == 1;
		for(int i=0; i < n; ++i)
		{
			oneVector.setEntry(i, 0, 1);
		}
				
		
		w2 = new BlockRealMatrix( getRandomMatrixInitializer(m, L));
		assert w2.getRowDimension() == m;
		print(w2);
		w3 = new BlockRealMatrix( getRandomMatrixInitializer(n, m));
		assert w3.getRowDimension() == n;
		
		b2 = new BlockRealMatrix( getRandomMatrixInitializer(m, 1));
		assert b2.getRowDimension() == m;
		b3 = new BlockRealMatrix( getRandomMatrixInitializer(n, 1) );
		assert b3.getRowDimension() == n;
		
	}
	
	public BlockRealMatrix computeOutputVector(BlockRealMatrix inputVector)
	{
		assert inputVector.getColumnDimension() == 1;
		assert inputVector.getRowDimension() == w2.getColumnDimension();
	
	    BlockRealMatrix a2 = w2.multiply(inputVector).add(b2);
	    Sigmoid.applyComponentwise(a2);
	    
	    BlockRealMatrix a3 = w3.multiply(a2).add(b3);
	    Sigmoid.applyComponentwise(a3);
	    
	    return a3;
	}
	
	public void learn(BlockRealMatrix inputVector, BlockRealMatrix desiredOutputVector )
	{
		
		assert inputVector.getColumnDimension() == 1;
		assert inputVector.getRowDimension() == w2.getColumnDimension();
		assert desiredOutputVector.getColumnDimension() == 1;
		assert desiredOutputVector.getRowDimension() == w3.getRowDimension();

		
		
		BlockRealMatrix a2 = w2.multiply(inputVector).add(b2);
		Sigmoid.applyComponentwise(a2);
		
		BlockRealMatrix z2 = a2.copy();
		Sigmoid.applyDerivativeComponentwise(z2);
		
		
		BlockRealMatrix a3 = computeOutputVector(inputVector);
		
		BlockRealMatrix delta3 = MatrixUtilities.multiplyComponentwise( MatrixUtilities.multiplyComponentwise(a3.subtract(desiredOutputVector), a3 ), oneVector.subtract(a3) ); 
		BlockRealMatrix delta2 = MatrixUtilities.multiplyComponentwise(w3.transpose().multiply(delta3), z2 );
		
		w3 = w3.subtract( delta3.multiply(a2.transpose()).scalarMultiply(nu) );
		w2 = w2.subtract(delta2.multiply(inputVector.transpose() ).scalarMultiply(nu) );
		
		b3 = b3.subtract( delta3.scalarMultiply(nu));
		b2 = b2.subtract( delta2.scalarMultiply(nu));
	}
	
	public void save(OutputStream stream) throws IOException
	{
		DataOutputStream out = new DataOutputStream(stream);
		saveB2(out);
		saveB3(out);
		saveW2(out);
		saveW3(out);
		
		print(w2);
	}
	
	public void load(InputStream stream) throws IOException
	{
		DataInputStream in = new DataInputStream(stream);
		loadB2(in);
		loadB3(in);
		loadW2(in);
		loadW3(in);
		
		print(w2);
	}
	
	
	

	private void print(BlockRealMatrix matrix) {
		int m = matrix.getRowDimension();
		int l = matrix.getColumnDimension();
		for(int row = 0; row < m; ++row )
		{
			for(int column = 0; column < l; ++column )
			{
				System.out.println(matrix.getEntry(row, column));
			}
		}
		
	}

	private void saveB3(DataOutputStream out) throws IOException {
		int n = b3.getRowDimension();
		for(int row = 0; row < n; ++row)
		{
			out.writeDouble(b3.getEntry(row, 0));
		}
	}
	
	private void loadB3(DataInputStream in) throws IOException {
		int n = b3.getRowDimension();
		for(int row = 0; row < n; ++row)
		{
			b3.setEntry(row, 0, in.readDouble() );
		}
	}
	
	

	private void saveB2(DataOutputStream out) throws IOException {
		int m = b2.getRowDimension();
		for(int row = 0; row < m; ++row)
		{
			out.writeDouble(b2.getEntry(row, 0));
		}
		
	}
	
	private void loadB2(DataInputStream in) throws IOException {
		int m = b2.getRowDimension();
		for(int row = 0; row < m; ++row)
		{
			b2.setEntry(row, 0, in.readDouble() );
		}
		
	}

	private void saveW3(DataOutputStream out) throws IOException {
		int n = w3.getRowDimension();
		int m = w3.getColumnDimension();
		for(int row = 0; row < n; ++row )
		{
			for(int column = 0; column < m; ++column )
			{
				out.writeDouble(w3.getEntry(row, column));
			}
		}
		
	}
	
	private void loadW3(DataInputStream in) throws IOException {
		int n = w3.getRowDimension();
		int m = w3.getColumnDimension();
		for(int row = 0; row < n; ++row )
		{
			for(int column = 0; column < m; ++column )
			{
				w3.setEntry(row, column, in.readDouble() );
			}
		}
		
	}

	private void saveW2(DataOutputStream out) throws IOException {
		int m = w2.getRowDimension();
		int l = w2.getColumnDimension();
		for(int row = 0; row < m; ++row )
		{
			for(int column = 0; column < l; ++column )
			{
				out.writeDouble(w2.getEntry(row, column));
			}
		}
		
	}
	
	private void loadW2(DataInputStream in) throws IOException {
		int m = w2.getRowDimension();
		int l = w2.getColumnDimension();
		for(int row = 0; row < m; ++row )
		{
			for(int column = 0; column < l; ++column )
			{
				w2.setEntry(row, column, in.readDouble());
			}
		}
		
	}

	
	private double [][] getRandomMatrixInitializer(int m, int l)
	{
		double [][] initializer = new double[m][l];
		for(int i=0; i < m; ++i)
		{
			for(int j=0; j < l; ++j)
			{
				initializer[i][j] = random.nextDouble() * 2 - 1;
			}
		}
		return initializer;
	}
	
}
