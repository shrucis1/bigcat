package bdv.labels.labelset;

import java.util.Set;

import bdv.labels.labelset.Multiset.Entry;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.IntegerType;

public class IntegerTypeLabelMultisetTypeConverter<T extends IntegerType<T>> implements Converter< T, LabelMultisetType >
{
	// TODO do this a better way
	@Override
	public void convert(T input, LabelMultisetType output) {
		Set<Entry<Label>> entries = output.entrySet();
		if(entries.isEmpty()) {
			entries.add(new LabelMultisetEntry(input.getIntegerLong(), 1));
			return;
		}
		LabelMultisetEntry firstEntry = ((LabelMultisetEntry)entries.iterator().next());
		firstEntry.setId(input.getIntegerLong()); // uses method that shouldn't be public
		firstEntry.setCount(1);
	}
} 
