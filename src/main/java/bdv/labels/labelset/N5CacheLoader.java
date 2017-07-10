package bdv.labels.labelset;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DatasetAttributes;
import org.janelia.saalfeldlab.n5.N5Reader;

import net.imglib2.cache.CacheLoader;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.util.Intervals;

public class N5CacheLoader implements CacheLoader< Long, Cell< VolatileLabelMultisetArray > >
{
	private final N5Reader n5;

	private final String dataset;

	private final long[] dimensions;

	private final int[] cellDimensions; // same as block size

	private final int numDimensions;

	private final DatasetAttributes attributes;

	private final CellGrid grid;


	public N5CacheLoader( final N5Reader n5, final String dataset) throws IOException
	{
		super();
		this.n5 = n5;
		this.dataset = dataset;
		this.attributes = n5.getDatasetAttributes( dataset );
		
		this.numDimensions = this.attributes.getNumDimensions();
		
		this.dimensions = attributes.getDimensions();
		this.cellDimensions = attributes.getBlockSize();

		this.grid = new CellGrid( dimensions, cellDimensions );
	}

	@Override
	public Cell< VolatileLabelMultisetArray > get( Long key ) throws Exception
	{
		long[] cellMin = new long[ numDimensions ];
		int[] cellSize = new int[ numDimensions ];
		long[] gridPosition = new long[ numDimensions ];
		
		for ( int i = 0; i < numDimensions; ++i )
		{
			gridPosition[ i ] = cellMin[ i ] / cellDimensions[ i ];
		}

		grid.getCellDimensions( key, cellMin, cellSize );

		final DataBlock< ? > block;
		try
		{
			block = n5.readBlock( dataset, attributes, gridPosition );
		}
		catch ( final IOException e )
		{
			throw new RuntimeException( e );
		}

		final byte[] bytes = ( byte[] ) block.getData();
		
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