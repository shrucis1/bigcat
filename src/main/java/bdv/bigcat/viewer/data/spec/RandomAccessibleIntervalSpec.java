package bdv.bigcat.viewer.data.spec;

import java.util.Optional;
import java.util.stream.DoubleStream;

import bdv.util.AxisOrder;
import bdv.util.BdvFunctions;
import bdv.viewer.SourceAndConverter;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

public class RandomAccessibleIntervalSpec< T extends RealType< T > > extends AbstractNamedDatasetSpecification
{

	private final DataType dataType;

	private final RandomAccessibleInterval< T > source;

	private final Converter< T, ARGBType > conv;

	private final SourceAndConverter< T > sac;

	public RandomAccessibleIntervalSpec(
			final DataType dataType,
			final RandomAccessibleInterval< T > source,
			final Converter< T, ARGBType > conv,
			final String name )
	{
		super( name, Optional.of( DoubleStream.generate( () -> 1.0 ).limit( source.numDimensions() ).toArray() ), Optional.of( DoubleStream.generate( () -> 0.0 ).limit( source.numDimensions() ).toArray() ) );
		this.dataType = dataType;
		this.source = source;
		this.conv = conv;
		this.sac = BdvFunctions.toSourceAndConverter( source, conv, AxisOrder.XYZ, new AffineTransform3D(), name() ).get( 0 );
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
		return o instanceof RandomAccessibleIntervalSpec && ( ( RandomAccessibleIntervalSpec ) o ).source == source;
	}

}
