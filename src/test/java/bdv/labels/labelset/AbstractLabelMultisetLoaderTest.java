package bdv.labels.labelset;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import gnu.trove.list.array.TIntArrayList;
import net.imglib2.cache.CacheLoader;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.ref.BoundedSoftRefLoaderCache;
import net.imglib2.cache.util.LoaderCacheAsCacheAdapter;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.CellRandomAccess;

public class AbstractLabelMultisetLoaderTest {
	

	@Test
	public void testCacheLoader() {
		
		final long[] dimensions = new long[] {2, 2, 1};
		final int[] cellDimensions = new int[] {2, 2, 1};
		
		final long[][] testIds = new long[][] { {2, 3, 1}, {1}, {2, 3}, {3, 1} };
		final int[][] testCounts = new int[][] { {4, 3, 2}, {9}, {5, 4}, {8, 1} };

		final int numEntries = 4;
		
		final int[] data = new int[4];
		final LongMappedAccessData listData = LongMappedAccessData.factory.createStorage( 32 );

		final LabelMultisetEntryList list = new LabelMultisetEntryList( listData, 0 );
		final LabelMultisetEntryList list2 = new LabelMultisetEntryList();
		final TIntArrayList listHashesAndOffsets = new TIntArrayList();
		final LabelMultisetEntry entry = new LabelMultisetEntry( 0, 1 );
		int nextListOffset = 0;
		
		for(int o = 0; o < numEntries; ++o) {
			
			list.createListAt( listData, nextListOffset );
			
			for(int i = 0; i < testIds[o].length; ++i) {
				entry.setId(testIds[o][i]);
				entry.setCount(testCounts[o][i]);
				list.add(entry);
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
						data[ o ] = listHashesAndOffsets.get( i + 1 ) ;
						break;
					}
				}
			}
			if ( makeNewList )
			{
				data[ o ] = nextListOffset;
				listHashesAndOffsets.add( hash );
				listHashesAndOffsets.add( nextListOffset );
				nextListOffset += list.getSizeInBytes();
			}
		}


		final byte[] bytes = new byte[ Integer.BYTES * data.length + nextListOffset ];

		ByteBuffer bb = ByteBuffer.wrap( bytes );

		for ( int i = 0; i < data.length; ++i )
		{
			bb.putInt(data[ i ]);
		}
		for ( int i = 0; i < nextListOffset; ++i )
			bb.put(ByteUtils.getByte( listData.data, i ) );

		// first test with just creating VolatileLabelMultisetArray straight from generated data
		assertMultisetArrayEquals(testIds, testCounts, new VolatileLabelMultisetArray( data, listData, true ));
		
		// now test with a cache loader and the byte data
		final CacheLoader< Long, Cell< VolatileLabelMultisetArray > > cacheLoader = new TestCacheLoader(dimensions, cellDimensions, bytes);
		
		final BoundedSoftRefLoaderCache< Long, Cell< VolatileLabelMultisetArray > > cache = new BoundedSoftRefLoaderCache<>( 1 );
		final LoaderCacheAsCacheAdapter< Long, Cell< VolatileLabelMultisetArray > > wrappedCache = new LoaderCacheAsCacheAdapter<>( cache, cacheLoader );
		final CachedCellImg<VolatileLabelMultisetType,VolatileLabelMultisetArray> c = new CachedCellImg<VolatileLabelMultisetType,VolatileLabelMultisetArray>( new CellGrid(dimensions, cellDimensions), new VolatileLabelMultisetType(), wrappedCache, new VolatileLabelMultisetArray(0, true));
	
		final CellRandomAccess<VolatileLabelMultisetType, Cell<VolatileLabelMultisetArray>> randAccess = c.randomAccess();

		assertMultisetArrayEquals(testIds, testCounts, randAccess.getCell().getData());
	}
	
	private void assertMultisetArrayEquals(long[][] expectedIds, int[][] expectedCounts, VolatileLabelMultisetArray actual) {
		LabelMultisetEntryList ref = new LabelMultisetEntryList();
		for(int i = 0; i < expectedIds.length; ++i) {
			actual.getValue(i, ref);
			for(int j = 0; j < expectedIds[i].length; ++j) {
				Assert.assertEquals(ref.get(j).getId(), expectedIds[i][j]);
				Assert.assertEquals(ref.get(j).getCount(), expectedCounts[i][j]);
			}
		}
	}
	
	protected class TestCacheLoader extends AbstractLabelMultisetLoader {

		protected final byte[] data;
		
		public TestCacheLoader(CellGrid grid, byte[] data) {
			super(grid);
			this.data = data;
		}
		
		public TestCacheLoader(long[] dimensions, int[] cellDimensions, byte[] data) {
			super(new CellGrid(dimensions, cellDimensions));
			this.data = data;
		}

		@Override
		protected byte[] getData(long... gridPosition) {
			return data;
		}

	}

}
