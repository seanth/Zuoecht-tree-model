
import java.util.Comparator;

//comparator class that is used to sort the segments by height
public class CompareSegmentZ implements Comparator<Segment> {
	public int compare (Segment one, Segment two) {
		if (one.end.z < two.end.z)
			return 1;
		if (one.end.z > two.end.z) 
			return -1;
		return 0;
	}

}
