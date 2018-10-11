import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class ConwayLife 
{
	public static boolean[][] cellStatesOld;
	public static boolean[][] cellStates;
	public static CyclicBarrier cb;
	public static int turn;
	
	public static void main( String args[] )
	{		
		// Start at turn 1
		turn = 1;
		
		// Instantiate random bounds of the grid
		Random rand = new Random();
		int m = rand.nextInt(50), n = rand.nextInt(50);
		
		// Init the barrier
		cb = new CyclicBarrier( m * n, new TurnManagerThread( m, n ) );
		
		// Init the state arrays
		cellStates = new boolean[m][n];
		cellStatesOld = new boolean[m][n];
		
		for ( int i = 0; i < m; i++ )
		{
			for ( int j = 0; j < n; j++ )
			{
				// Instantiate a random value for this cell
				cellStatesOld[i][j] = rand.nextBoolean();
				cellStates = cellStatesOld;
				
				Thread t = new Thread( new Cell( m, n, i, j ) );
				t.start();
			}
		}
	}
}

// Thread for handling a single cell in the grid
class Cell implements Runnable
{
	// Bounds
	private int m;
	private int n;
	
	// Position
	private int x;
	private int y;
	
	public Cell( int m, int n, int x, int y )
	{
		this.m = m;
		this.n = n;
		this.x = x;
		this.y = y;
	}
	
	public void run()
	{
		while ( true )
		{
			ConwayLife.cellStates[x][y] = determineCellState( getLivingNeighborCount() );
			
			try 
			{
				// Wait for others to finish the turn
				ConwayLife.cb.await();
			} 
			catch ( InterruptedException | BrokenBarrierException e ) 
			{
				e.printStackTrace();
				break;
			}
			
			ThreadSleep( 1000 );
		}
	}
	
	private void ThreadSleep( int delayTime )
	{
		try
		{
			Thread.sleep( delayTime );
		}
		catch ( InterruptedException e ) {}
	}
	
	private int getLivingNeighborCount()
	{
		int count = 0;
		
		if ( x + 1 < m )
		{
			if ( y - 1 >= 0 )
			{
				if ( ConwayLife.cellStatesOld[x + 1][y - 1] ) // North-West
					count++;
			}
			
			if ( ConwayLife.cellStatesOld[x + 1][y] ) // North
				count++;
			
			if ( y + 1 < n )
			{
				if ( ConwayLife.cellStatesOld[x + 1][y + 1] ) // North-East			
					count++;
			}
		}
		
		if ( y - 1 >= 0 )
		{
			if ( ConwayLife.cellStatesOld[x][y - 1] ) // West
				count++;
		}
		
		if ( y + 1 < n )
		{
			if ( ConwayLife.cellStatesOld[x][y + 1] ) // East
				count++;
		}
		
		if ( x - 1 >= 0 )
		{
			if ( y - 1 >= 0 )
			{
				if ( ConwayLife.cellStatesOld[x - 1][y - 1] ) // South-West
					count++;
			}
			
			if ( ConwayLife.cellStatesOld[x - 1][y] ) // South
				count++;
			
			if ( y + 1 < n )
			{
				if ( ConwayLife.cellStatesOld[x - 1][y + 1] ) // South-East
					count++;
			}
		}
		
		return count;
	}
	
	private boolean determineCellState( int count )
	{
		boolean state = ConwayLife.cellStatesOld[x][y];
		
		if ( state )
		{
			return ( ( count == 2 || count == 3 ) ? true : false ); 
		}
		else
		{
			return ( count == 3 );
		}
	}
}

// Thread to make sure turns are handled in an orderly fashion
class TurnManagerThread implements Runnable
{
	private int m;
	private int n;
	
	public TurnManagerThread( int m, int n )
	{
		this.m = m;
		this.n = n;
	}
	
	public void run()
	{
		System.out.println( "\nTURN " + ConwayLife.turn + "\nPrinting grid...\n" );
		
		for ( int i = 0; i < m; i++ )
		{
			for ( int j = 0; j < n; j++ )
			{
				System.out.print( ConwayLife.cellStates[i][j] ? " 1 " : " 0 " );
				ConwayLife.cellStatesOld[i][j] = ConwayLife.cellStates[i][j];
			}			
			System.out.println();
		}
		ConwayLife.turn++;
	}
}
