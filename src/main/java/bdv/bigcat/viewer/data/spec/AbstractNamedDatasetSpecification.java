package bdv.bigcat.viewer.data.spec;

import java.util.Optional;

public abstract class AbstractNamedDatasetSpecification implements DatasetSpecification
{

	protected String name;

	protected final Optional< double[] > resolution;

	protected final Optional< double[] > offset;

	public AbstractNamedDatasetSpecification( final String name, final Optional< double[] > resolution, final Optional< double[] > offset )
	{
		super();
		this.name = name;
		this.offset = offset;
		this.resolution = resolution;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public void name( final String name )
	{
		this.name = name;
	}

	@Override
	public Optional< double[] > resolution()
	{
		return resolution;
	}

	@Override
	public Optional< double[] > offset()
	{
		return offset;
	}

}
