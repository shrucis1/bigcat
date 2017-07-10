package bdv.labels.labelset;

import java.nio.ByteBuffer;

import net.imglib2.cache.CacheLoader;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.util.Intervals;

public abstract class AbstractLabelMultisetLoader implements CacheLoader< Long, Cell< VolatileLabelMultisetArray > >
{
	
	protected final CellGrid grid;
	
	public AbstractLabelMultisetLoader(CellGrid grid) {
		this.grid = grid;
	}

	protected abstract byte[] getData(long... gridPosition);
	
	@Override
	public Cell<VolatileLabelMultisetArray> get(Long key) {

		int numDimensions = grid.numDimensions();
		
		long[] cellMin = new long[ numDimensions ];
		int[] cellSize = new int[ numDimensions ];
		long[] gridPosition = new long[ numDimensions ];
		int[] cellDimensions = new int[ numDimensions ]; 
		grid.cellDimensions(cellDimensions);
						
		for ( int i = 0; i < numDimensions; ++i )
		{
			gridPosition[ i ] = cellMin[ i ] / cellDimensions[ i ];
		}

		grid.getCellDimensions( key, cellMin, cellSize );
		
		byte[] bytes = this.getData(gridPosition);
		
		ByteBuffer bb = ByteBuffer.wrap( bytes );
		
		final int[] data = new int[( int ) Intervals.numElements( cellSize )];
		final int listDataSize = bytes.length - 4 * data.length;
		final LongMappedAccessData listData = LongMappedAccessData.factory.createStorage( listDataSize );
		
		for ( int i = 0; i < data.length; ++i )
		{
			data[i] = bb.getInt();
		}
		
		for ( int i = 0; i < listDataSize; ++i )
			ByteUtils.putByte( bb.get(), listData.data, i );
			
		return new Cell< VolatileLabelMultisetArray >( cellSize, cellMin, new VolatileLabelMultisetArray( data, listData, true ) );
	}
}
