package bdv.labels.labelset;

import bdv.labels.labelset.Multiset.Entry;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.integer.LongType;

public class VolatileLabelMultisetTypeLongTypeConverter implements Converter< VolatileLabelMultisetType, LongType >
{
	@Override
	public void convert(VolatileLabelMultisetType input, LongType output) {
		int maxCount = -1;
		long maxCountLabel = -1;
		for(Entry<Label> entry : input.get().entrySet()) {
			if(entry.getCount() > maxCount) {
				maxCount = entry.getCount();
				maxCountLabel = entry.getElement().id();
			}
		}
		output.set(maxCountLabel);
	}
}
