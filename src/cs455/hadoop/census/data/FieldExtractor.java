package cs455.hadoop.census.data;

import cs455.hadoop.census.except.SegmentMismatchException;

public class FieldExtractor {
	
	public static Object extract(String s, Field fld) throws SegmentMismatchException
	{
		return extractAt(s, fld, 1);
	}
	
	public static Object extractAt(String s, Field fld, int instance) throws SegmentMismatchException
	{
		if (instance < 1 || instance > fld.getInstances())
			throw new IllegalArgumentException("Instance number " + instance + " too large for field " + fld.toString());
		
		// If field is present in both segments, then no need to check further
		if (fld.getSegment() != Segment.BOTH)
		{
			Segment sgmt = extractSegment(s);
			if (sgmt != fld.getSegment())
				throw new SegmentMismatchException("Field " + fld.toString() + " is not present in this segment");
		}
		
		int beginIndex = fld.getStart() + (instance - 1) * fld.getSize();
		
		String result = s.substring(beginIndex, beginIndex + fld.getSize());
		
		switch(fld.getType())
		{
		case TEXTUAL:
			return result;
		case NUMERIC:
			return Integer.parseInt(result);
		default:
			throw new IllegalStateException("Invalid field type found: " + fld.getType());
		}
	}
	
	private static Segment extractSegment(String s)
	{
		String seg = s.substring(Field.SEGMENT_NUMBER.getStart(),
								 Field.SEGMENT_NUMBER.getStart() + Field.SEGMENT_NUMBER.getSize());
		
		int result = Integer.parseInt(seg);
		
		switch(result)
		{
		case 1: return Segment.ONE;
		case 2: return Segment.TWO;
		default: throw new IllegalStateException("Invalid segment number found: " + result);
		}
	}

}
