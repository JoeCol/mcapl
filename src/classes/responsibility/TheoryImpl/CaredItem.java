package responsibility.TheoryImpl;

public class CaredItem implements Comparable<CaredItem> {

	public int size;
	public int highestValue;
	public int index;
	@Override
	public int compareTo(CaredItem compared) {
		if (highestValue == compared.highestValue)
		{
			if (size == compared.size)
			{
				return 0 - Integer.compare(index, compared.index); //reverse order so first arrived is first done when equal
			}
			return Integer.compare(size, compared.size);
		}
		else
		{
			return Integer.compare(highestValue, compared.highestValue);
		}
	}

}
