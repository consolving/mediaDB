package helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Util {
	
	private Util() {
		
	}
	// thx to http://stackoverflow.com/a/740351
	public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
		List<T> list = new ArrayList<T>(c);
		java.util.Collections.sort(list);
		return list;
	}
}
