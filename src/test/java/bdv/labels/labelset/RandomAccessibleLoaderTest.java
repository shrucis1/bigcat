package bdv.labels.labelset;

import org.junit.Assert;
import org.junit.Test;

import net.imglib2.cache.CacheLoader;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.ref.BoundedSoftRefLoaderCache;
import net.imglib2.cache.util.LoaderCacheAsCacheAdapter;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.CellRandomAccess;
import net.imglib2.type.numeric.integer.LongType;

public class RandomAccessibleLoaderTest {

	//@Test
	public void test1x1Cells() {
		long[] testData = new long[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		ArrayImg<LongType, LongArray> source = ArrayImgs.longs( testData, 3, 3 );
		
		final long[] dimensions = new long[] {3,  3};
		final int[] cellDimensions = new int[] {1, 1};
		
		final CacheLoader< Long, Cell< VolatileLabelMultisetArray > > cacheLoader = new RandomAccessibleLoader( dimensions, cellDimensions, source);
		
		final BoundedSoftRefLoaderCache< Long, Cell< VolatileLabelMultisetArray > > cache = new BoundedSoftRefLoaderCache<>( 100 );
		final LoaderCacheAsCacheAdapter< Long, Cell< VolatileLabelMultisetArray > > wrappedCache = new LoaderCacheAsCacheAdapter<>( cache, cacheLoader );
		final CachedCellImg<VolatileLabelMultisetType,VolatileLabelMultisetArray> c = new CachedCellImg<VolatileLabelMultisetType,VolatileLabelMultisetArray>( new CellGrid(dimensions, cellDimensions), new VolatileLabelMultisetType(), wrappedCache, new VolatileLabelMultisetArray(0, true));
	
		final CellRandomAccess<VolatileLabelMultisetType, Cell<VolatileLabelMultisetArray>> randAccess = c.randomAccess();
		
		LabelMultisetEntryList ref = new LabelMultisetEntryList();
		
		for(int i = 0; i < source.size(); ++i) {
			randAccess.setPosition(new int[] {i%3, (i/3)%3, (i/9)%3});
			VolatileLabelMultisetArray var = randAccess.getCell().getData();
			var.getValue(0, ref);
			Assert.assertEquals(ref.get(0).getId(), testData[i]);
			Assert.assertEquals(ref.get(0).getCount(), 1);
		}
	}
	
	@Test
	public void test2x2Cells() throws Exception {
		long[] testData = new long[] { 1, 1, 2, 1, 2, 3, 2, 2, 3 };
		ArrayImg<LongType, LongArray> source = ArrayImgs.longs( testData, 3, 3 );
		
		final long[] dimensions = new long[] {3, 3};
		final int[] cellDimensions = new int[] {2, 2};
		
		final CacheLoader< Long, Cell< VolatileLabelMultisetArray > > cacheLoader = new RandomAccessibleLoader( dimensions, cellDimensions, source);
		
		final BoundedSoftRefLoaderCache< Long, Cell< VolatileLabelMultisetArray > > cache = new BoundedSoftRefLoaderCache<>( 100 );
		final LoaderCacheAsCacheAdapter< Long, Cell< VolatileLabelMultisetArray > > wrappedCache = new LoaderCacheAsCacheAdapter<>( cache, cacheLoader );
		final CachedCellImg<VolatileLabelMultisetType,VolatileLabelMultisetArray> c = new CachedCellImg<VolatileLabelMultisetType,VolatileLabelMultisetArray>( new CellGrid(dimensions, cellDimensions), new VolatileLabelMultisetType(), wrappedCache, new VolatileLabelMultisetArray(0, true));
	
		final CellRandomAccess<VolatileLabelMultisetType, Cell<VolatileLabelMultisetArray>> randAccess = c.randomAccess();
		

		// test all positions in cell0
		randAccess.setPosition(new int[] {0, 0, 0});
		assert2x2Cell(0, randAccess.getCell().getData());
		
		randAccess.setPosition(new int[] {1, 0, 0});
		assert2x2Cell(0, randAccess.getCell().getData());
		
		randAccess.setPosition(new int[] {0, 1, 0});
		assert2x2Cell(0, randAccess.getCell().getData());
		
		randAccess.setPosition(new int[] {1, 1, 0});
		assert2x2Cell(0, randAccess.getCell().getData());
		

		// test all positions in cell1
		randAccess.setPosition(new int[] {2, 0, 0});
		assert2x2Cell(1, randAccess.getCell().getData());
		
		randAccess.setPosition(new int[] {2, 1, 0});
		assert2x2Cell(1, randAccess.getCell().getData());
		

		// test all positions in cell2
		randAccess.setPosition(new int[] {0, 2, 0});
		assert2x2Cell(2, randAccess.getCell().getData());
		
		randAccess.setPosition(new int[] {1, 2, 0});
		assert2x2Cell(2, randAccess.getCell().getData());


		// test all positions in cell3
		randAccess.setPosition(new int[] {2, 2, 0});
		assert2x2Cell(3, randAccess.getCell().getData());
	}
	
	// helper routine to check the 2x2 cells in test2x2Cells()
	private void assert2x2Cell(int cellNum, VolatileLabelMultisetArray actual) {
		LabelMultisetEntryList ref = new LabelMultisetEntryList();
		switch(cellNum) {
		case 0: // [[1, 1], [1, 2]]
			actual.getValue(0, ref);
			Assert.assertEquals(1, ref.size());
			Assert.assertEquals(1, ref.get(0).getId());
			Assert.assertEquals(1, ref.get(0).getCount());
			actual.getValue(1, ref);
			Assert.assertEquals(1, ref.size());
			Assert.assertEquals(1, ref.get(0).getId());
			Assert.assertEquals(1, ref.get(0).getCount());
			actual.getValue(2, ref);
			Assert.assertEquals(1, ref.size());
			Assert.assertEquals(1, ref.get(0).getId());
			Assert.assertEquals(1, ref.get(0).getCount());
			actual.getValue(3, ref);
			Assert.assertEquals(1, ref.size());
			Assert.assertEquals(2, ref.get(0).getId());
			Assert.assertEquals(1, ref.get(0).getCount());
			break;
		case 1: // [[2], [3]]
			actual.getValue(0, ref);
			Assert.assertEquals(1, ref.size());
			Assert.assertEquals(2, ref.get(0).getId());
			Assert.assertEquals(1, ref.get(0).getCount());
			actual.getValue(1, ref);
			Assert.assertEquals(1, ref.size());
			Assert.assertEquals(3, ref.get(0).getId());
			Assert.assertEquals(1, ref.get(0).getCount());
			break;
		case 2: // [[2, 2]]
			actual.getValue(0, ref);
			Assert.assertEquals(1, ref.size());
			Assert.assertEquals(2, ref.get(0).getId());
			Assert.assertEquals(1, ref.get(0).getCount());
			actual.getValue(1, ref);
			Assert.assertEquals(1, ref.size());
			Assert.assertEquals(2, ref.get(0).getId());
			Assert.assertEquals(1, ref.get(0).getCount());
			break;
		case 3: // [[3]]
			actual.getValue(0, ref);
			Assert.assertEquals(1, ref.size());
			Assert.assertEquals(3, ref.get(0).getId());
			Assert.assertEquals(1, ref.get(0).getCount());
			break;
		default:
			break;
		}
	}
}
