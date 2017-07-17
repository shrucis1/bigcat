package bdv.labels.labelset;

import bdv.labels.labelset.Multiset.Entry;
import gnu.trove.list.array.TIntArrayList;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;

public class LabelMultisetTypeDownscaler {

	public static VolatileLabelMultisetArray createDownscaledCell(RandomAccessible<LabelMultisetType> source, Interval interval, int[] factor) {
		return createDownscaledCell(Views.interval(source, interval), factor);
	}
	
	public static VolatileLabelMultisetArray createDownscaledCell(RandomAccessibleInterval<LabelMultisetType> source, int[] factor) {
		
		RandomAccess<LabelMultisetType> randomAccess = source.randomAccess();
		
		final int numDim = source.numDimensions();
		final long[] maxOffset = new long[numDim];
		final long[] cellOffset = new long[numDim]; // not in units of cells
		final long[] totalOffset = new long[numDim]; // absolute, inside of cell
		
		for(int i = 0; i < numDim; i ++) {
			maxOffset[i] = source.max(i) + 1; // interval is inclusive
		}
		
		int numDownscaledLists = 1;
		for(int i = 0; i < numDim; i ++) {
			numDownscaledLists *= (long)Math.ceil((maxOffset[i])/factor[i]);
		}
		final int[] data = new int[numDownscaledLists];
		
		final LongMappedAccessData listData = LongMappedAccessData.factory.createStorage( 32 );
		
		final LabelMultisetEntryList list = new LabelMultisetEntryList( listData, 0 );
		final LabelMultisetEntryList list2 = new LabelMultisetEntryList();
		final TIntArrayList listHashesAndOffsets = new TIntArrayList();
		final LabelMultisetEntry entry = new LabelMultisetEntry( 0, 1 );
		int nextListOffset = 0;
		int o = 0;
		
		int searchIndex;
				
		for(int d = 0; d < numDim;)
		{
			
			list.createListAt( listData, nextListOffset );
			
			for(int i = 0; i < numDim; i ++)
				totalOffset[i] = cellOffset[i];
			
			for(int g = 0; g < numDim;)
			{
			
				randomAccess.setPosition(totalOffset);
				for(Entry<Label> sourceEntry : randomAccess.get().entrySet())
				{
					searchIndex = list.binarySearch( sourceEntry.getElement().id() );
					if(list.size()>0 && searchIndex >= 0) {
						// just add 1 to the count of existing label
						list.get(searchIndex).setCount(list.get(searchIndex).getCount() + 1);
					} else {
						entry.setId( sourceEntry.getElement().id() );
						entry.setCount( sourceEntry.getCount() );
						list.add( entry );
						list.sortById();
					}
				}
				
				for(g = 0; g < numDim; g++)
				{
					totalOffset[g] += 1;
					if(totalOffset[g] < cellOffset[g] + factor[g] && totalOffset[g] < maxOffset[g])
						break;
					else
						totalOffset[g] = cellOffset[g];
				}
			}
			
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
						data[ o++ ] = listHashesAndOffsets.get( i + 1 ) ;
						break;
					}
				}
			}
			if ( makeNewList )
			{
				data[ o++ ] = nextListOffset;
				listHashesAndOffsets.add( hash );
				listHashesAndOffsets.add( nextListOffset );
				nextListOffset += list.getSizeInBytes();
			}
			
			
			for(d = 0; d < numDim; d++ )
			{
				cellOffset[d] += factor[d];
				if(cellOffset[d] < maxOffset[d])
					break;
				else
					cellOffset[d] = 0;
			}
		}
		return new VolatileLabelMultisetArray( data, listData, true );
	}
}
