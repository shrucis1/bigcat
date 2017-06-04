package bdv.bigcat.viewer.data.spec;

import java.util.Optional;

import bdv.bigcat.ui.ARGBStream;

public abstract class AbstractLabelSpec extends AbstractNamedDatasetSpecification
{

	protected final ARGBStream colorStream;

	public AbstractLabelSpec(
			final String name,
			final Optional< double[] > resolution,
			final Optional< double[] > offset,
			final ARGBStream colorStream )
	{
		super( name, resolution, offset );
		this.colorStream = colorStream;
	}

}
