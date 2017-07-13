package bdv.labels.labelset;

import java.util.Set;

import bdv.labels.labelset.Multiset.Entry;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.IntegerType;

public class IntegerTypeLabelMultisetTypeConverter<T extends IntegerType<T>> implements Converter< T, LabelMultisetType >
{
	// is converter applicable here? LabelMultisetType doesn't behave like other Type classes
	@Override
	public void convert(T input, LabelMultisetType output) {
		Set<Entry<Label>> entries = output.entrySet();
		entries.clear();
		entries.add(new LabelMultisetEntry(input.getIntegerLong(), 1));
	}
	
	/*
	// also possibly valid?
	public void convert(T input, LabelMultisetType output) {
		Set<Entry<Label>> entries = output.entrySet();
		Iterator<Entry<Label>> it = entries.iterator();
		if(!it.hasNext())
			return; // ?
		LabelMultisetEntry firstEntry = ((LabelMultisetEntry)it.next());
		firstEntry.setId(input.getIntegerLong()); // uses method that shouldn't be public
		firstEntry.setCount(1);
		while(it.hasNext()) {
			it.next();
			it.remove();
		}
	}
    */
} 
