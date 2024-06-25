import java.util.*;

//comparator class that is used to sort the segments by age
public class CompareSegmentAge implements Comparator<Segment> {
	public int compare (Segment one, Segment two) {
		if (one.getAge() < two.getAge())
			return 1;
		if (one.getAge() > two.getAge()) 
			return -1;
		return 0;
	}

}
