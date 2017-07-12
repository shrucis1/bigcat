package bdv.labels.labelset;

import java.nio.ByteBuffer;

import gnu.trove.list.array.TIntArrayList;
import net.imglib2.RandomAccessible;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class RandomAccessibleLoader extends AbstractLabelMultisetLoader {

	private final RandomAccessible<? extends IntegerType<?>> randomAccessible;

	public RandomAccessibleLoader(CellGrid grid, RandomAccessible<? extends IntegerType<?>> randomAccessible) {
		super(grid);
		this.randomAccessible = randomAccessible;
	}
	
	public RandomAccessibleLoader(long[] dimensions, int[] cellDimensions, RandomAccessible<? extends IntegerType<?>> randomAccessible) {
		super(new CellGrid(dimensions, cellDimensions));
		this.randomAccessible = randomAccessible;
	}

	@Override
	protected byte[] getData(long... gridPosition) {
		
		long[] cellMin = new long[grid.numDimensions()];
		int[] cellDims = new int[grid.numDimensions()];
		this.grid.getCellDimensions( gridPosition, cellMin, cellDims );
		
		long[] minmax = new long[2*grid.numDimensions()];
		for(int i = 0; i < grid.numDimensions(); ++i)
		{
			minmax[i] = cellMin[i];
			minmax[grid.numDimensions() + i] = cellMin[i] + cellDims[i] - 1; // max, inclusive
		}

		IntervalView<? extends IntegerType<?>> rai = Views.interval(this.randomAccessible, Intervals.createMinMax(minmax));
		
		int numPixels = 1;
		for(int i : cellDims)
			numPixels *= i;
		
		final int[] data = new int[numPixels];

		final LongMappedAccessData listData = LongMappedAccessData.factory.createStorage( 32 );
		
		final LabelMultisetEntryList list = new LabelMultisetEntryList( listData, 0 );
		final LabelMultisetEntryList list2 = new LabelMultisetEntryList();
		final TIntArrayList listHashesAndOffsets = new TIntArrayList();
		final LabelMultisetEntry entry = new LabelMultisetEntry( 0, 1 );
		int nextListOffset = 0;
		
		int j = 0;
		for(IntegerType<?> l : Views.flatIterable(rai))
		{
			list.createListAt( listData, nextListOffset );
			entry.setId( l.getIntegerLong() );
			entry.setCount( 1 );
			list.add( entry );
			
			boolean makeNewList = true;
			final int hash = list.hashCode();
			for ( int i = 0; i < listHashesAndOffsets.size(); i += 2 )
			{
				if ( hash == listHashesAndOffsets.get( i ) )
				{
					list2.referToDataAt( listData, listHashesAndOffsets.get( i + 1 ) );
					if ( list.equals( list2 ) )
					{
						makeNewList = false;
						data[ j++ ] = listHashesAndOffsets.get( i + 1 ) ;
						break;
					}
				}
			}
			if ( makeNewList )
			{
				data[ j++ ] = nextListOffset;
				listHashesAndOffsets.add( hash );
				listHashesAndOffsets.add( nextListOffset );
				nextListOffset += list.getSizeInBytes();
			}
		}


		final byte[] bytes = new byte[ 4 * data.length + nextListOffset ];
		ByteBuffer bb = ByteBuffer.wrap( bytes );
		
		
		for ( final int d : data )
		{
			bb.putInt(d);
		}
		for ( int i = 0; i < nextListOffset; ++i )
			bb.put(ByteUtils.getByte( listData.data, i ));
		
		return bytes;
	}

}
