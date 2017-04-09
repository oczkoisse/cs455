package cs455.hadoop.census.data;

public enum Field {
	
	STATE(8, 2, Segment.BOTH, 1, FieldType.TEXTUAL),
	SUMMARY_LEVEL(10, 3, Segment.BOTH),
	RECORD_NUMBER(18, 6, Segment.BOTH),
	SEGMENT_NUMBER(24, 4, Segment.BOTH),
	SEGMENT_COUNT(28, 4, Segment.BOTH),
	
	POPULATION(300, 9, Segment.ONE),
	MALE(363, 9, Segment.ONE),
	FEMALE(372, 9, Segment.ONE),
	MALE_NEVER_MARRIED(4422, 9, Segment.ONE),
	FEMALE_NEVER_MARRIED(4493, 9, Segment.ONE),
	HOUSES_OWNED(1803, 9, Segment.TWO),
	HOUSES_RENTED(1812, 9, Segment.TWO),
	
	
	AGE_GRP(795, 9, Segment.ONE, 31, FieldType.NUMERIC),
	MALE_HISP_AGE_GRP(3864, 9, Segment.ONE, 31, FieldType.NUMERIC),
	FEMALE_HISP_AGE_GRP(4143, 9, Segment.ONE, 31, FieldType.NUMERIC),
	HOUSE_ROOMS_GRP(2388, 9, Segment.TWO, 9, FieldType.NUMERIC),
	HOUSE_VALUE_GRP(2928, 9, Segment.TWO, 20, FieldType.NUMERIC),
	HOUSE_RENT_GRP(3450, 9, Segment.TWO, 17, FieldType.NUMERIC);
	
	private int start;
	private int size;
	private Segment s;
	private int instances;
	private FieldType f;
	
	private Field(int start, int size, Segment s, int instances, FieldType f)
	{
		this.start = start;
		this.size = size;
		this.s = s;
		this.instances = instances;
		this.f = f;
	}
	
	private Field(int start, int size, Segment s)
	{
		this.start = start;
		this.size = size;
		this.s = s;
		this.instances = 1;
		this.f = FieldType.NUMERIC;
	}
	
	public int getStart()
	{
		return start;
	}
	
	public int getSize()
	{
		return size;
	}
	
	public FieldType getType()
	{
		return f;
	}
	
	public Segment getSegment()
	{
		return s;
	}
	
	public int getInstances()
	{
		return instances;
	}

}
