package bdv.bigcat.viewer.data.spec;

import java.io.IOException;
import java.util.Optional;

import bdv.AbstractViewerSetupImgLoader;
import bdv.bigcat.ui.ARGBConvertedLabelsSource;
import bdv.bigcat.ui.ARGBStream;
import bdv.img.cache.VolatileGlobalCellCache;
import bdv.img.h5.H5LabelMultisetSetupImageLoader;
import bdv.labels.labelset.LabelMultisetType;
import bdv.labels.labelset.VolatileLabelMultisetType;
import bdv.viewer.SourceAndConverter;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import net.imglib2.display.ScaledARGBConverter;
import net.imglib2.display.ScaledARGBConverter.ARGB;
import net.imglib2.display.ScaledARGBConverter.VolatileARGB;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.volatiles.VolatileARGBType;

public class H5LabelSpec extends AbstractLabelSpec
{

	private final String path;

	private final String dataset;

	private final DataType dataType;

	private final SourceAndConverter< ? > sac;

	public H5LabelSpec(
			final String path,
			final String dataset,
			final DataType dataType,
			final ARGBStream colorStream ) throws IOException
	{
		this( path, dataset, dataType, colorStream, Optional.empty(), Optional.empty() );
	}

	public H5LabelSpec(
			final String path,
			final String dataset,
			final DataType dataType,
			final ARGBStream colorStream,
			final Optional< double[] > resolution,
			final Optional< double[] > offset ) throws IOException
	{
		super( path + "/" + dataset, resolution, offset, colorStream );
		this.path = path;
		this.dataset = dataset;
		this.dataType = dataType;
		this.sac = createSourceAndConverter( path, dataset, resolution, offset, colorStream );
	}

	@Override
	public DataType dataType()
	{
		return dataType;
	}

	@Override
	public SourceAndConverter< ? > sourceAndConverter()
	{
		return sac;
	}

	@Override
	public boolean equals( final Object o )
	{
		return o instanceof H5LabelSpec && ( ( H5LabelSpec ) o ).path.equals( path ) && ( ( H5LabelSpec ) o ).dataset.equals( dataset );
	}

	private static SourceAndConverter< ? > createSourceAndConverter(
			final String path, final String dataset, final Optional< double[] > resolution, final Optional< double[] > offset, final ARGBStream colorStream ) throws IOException
	{
		final IHDF5Reader reader = HDF5Factory.open( path );
		final double[] res = resolution.orElse( readResolution( reader, dataset ) );
		final double[] off = offset.orElse( readOffset( reader, dataset ) );
		final int[] chunkSize = null;

		final H5LabelMultisetSetupImageLoader loader = new H5LabelMultisetSetupImageLoader( reader, null, dataset, 0, chunkSize, res, off, new VolatileGlobalCellCache( 1, 10 ) );

		final ARGB converter = new ScaledARGBConverter.ARGB( 0, 255 );
		final VolatileARGB vconverter = new ScaledARGBConverter.VolatileARGB( 0, 255 );
		final AbstractViewerSetupImgLoader< LabelMultisetType, VolatileLabelMultisetType > l = loader;
		final ARGBConvertedLabelsSource source = new ARGBConvertedLabelsSource( 0, l, colorStream );
		final SourceAndConverter< VolatileARGBType > vsac = new SourceAndConverter<>( source, vconverter );
		final SourceAndConverter< ARGBType > sac = new SourceAndConverter<>( source.nonVolatile(), converter, vsac );

		return sac;
	}

	public static double[] readResolution( final IHDF5Reader reader, final String dataset )
	{
		final double[] resolution;
		if ( reader.object().hasAttribute( dataset, "resolution" ) )
		{
			final double[] h5res = reader.float64().getArrayAttr( dataset, "resolution" );
			resolution = new double[] { h5res[ 2 ], h5res[ 1 ], h5res[ 0 ], };
		}
		else
			resolution = new double[] { 1, 1, 1 };

		return resolution;
	}

	public static double[] readOffset( final IHDF5Reader reader, final String dataset )
	{
		final double[] offset;
		if ( reader.object().hasAttribute( dataset, "offset" ) )
		{
			final double[] h5offset = reader.float64().getArrayAttr( dataset, "offset" );
			offset = new double[] { h5offset[ 2 ], h5offset[ 1 ], h5offset[ 0 ], };
		}
		else
			offset = new double[] { 0, 0, 0 };

		return offset;
	}

}
