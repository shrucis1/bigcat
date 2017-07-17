package bdv.labels.labelset;

import java.util.Arrays;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.CacheLoader;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.view.Views;

public class DownscalingVolatileLabelMultisetArrayLoader implements CacheLoader< Long, Cell< VolatileLabelMultisetArray > >
{
	protected final CellGrid grid;
	private RandomAccessible<LabelMultisetType> source;
	private int[] factor;
	
	public DownscalingVolatileLabelMultisetArrayLoader(RandomAccessibleInterval<LabelMultisetType> source, long[] dimensions, int[] cellDimensions, int[] factor) {
		this(source, new CellGrid(dimensions, cellDimensions), factor);
	}
	
	public DownscalingVolatileLabelMultisetArrayLoader(RandomAccessibleInterval<LabelMultisetType> source, CellGrid grid, int[] factor) {
		this.grid = grid;
		this.source = source;
		this.factor = factor;
	}
	
	@Override
	public Cell<VolatileLabelMultisetArray> get(Long key) throws Exception {
		
		int numDimensions = grid.numDimensions();
		
		long[] cellMin = new long[ numDimensions ];
		int[] cellSize = new int[ numDimensions ];
		
		grid.getCellDimensions( key, cellMin, cellSize );
		
		long[] sourceMin = new long [numDimensions];
		long[] sourceSize = new long [numDimensions];
		Arrays.setAll(sourceMin, i -> cellMin[i] * factor[i]);
		Arrays.setAll(sourceSize, i -> cellSize[i] * factor[i]);
		
		RandomAccessibleInterval<LabelMultisetType> cellInterval = Views.offsetInterval(source, sourceMin, sourceSize);
		
		VolatileLabelMultisetArray downscaledCell = LabelMultisetTypeDownscaler.createDownscaledCell(cellInterval, factor);
		
		return new Cell<VolatileLabelMultisetArray>(cellSize, cellMin, downscaledCell);
	}

}