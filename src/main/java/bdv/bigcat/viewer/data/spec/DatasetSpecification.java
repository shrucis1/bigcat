package bdv.bigcat.viewer.data.spec;

import java.util.Optional;

import bdv.viewer.SourceAndConverter;

public interface DatasetSpecification
{

	public static enum DataType
	{
		RAW, LABEL
	};

	public DataType dataType();

	public default Optional< double[] > resolution()
	{
		return Optional.empty();
	}

	public default Optional< double[] > offset()
	{
		return Optional.empty();
	}

	public String name();

	public void name( String name );

	public SourceAndConverter< ? > sourceAndConverter();

}
