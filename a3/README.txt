
- Data extraction is implemented via cs455.hadoop.census.data
-- Each relevant field is implemented as an enumeration that knows where it starts, and what length the field is.
-- The group fields like age distribution that are homogeneous, and repeat continuously for a particular number of instances, are also implemented as enumerations ending with _GRP
-- The actual extraction is carried out by FieldExtractor class, which has suitalble methods for single field extraction (.extract()) and group field extractions in a loop (.extractAt())

- Segments are modeled as enumerations Segment.ONE, Segment.TWO, Segment.BOTH

- Field type is also an enumeration: Field.NUMERIC and Field.TEXTUAL. Based on the field type, the extract() and extractAt() return an int and a String respectively

- Jobs are organized under cs455.hadoop.census.jobs
-- cs455.hadoop.census.jobs.tenure.TenureJob for q1
-- cs455.hadoop.census.jobs.marital.MaritalJob for q2
-- cs455.hadoop.census.jobs.age.AgeJob for q3
-- cs455.hadoop.census.jobs.urbanrural.UrbanRuralJob for q4
-- cs455.hadoop.census.jobs.housevalue.HouseValueJob for q5
-- cs455.hadoop.census.jobs.rent.HouseRentJob for q6
-- cs455.hadoop.census.jobs.rooms.RoomsJob for q7
-- cs455.hadoop.census.jobs.elderly.ElderlyJob for q8
-- cs455.hadoop.census.jobs.analysis.AnalysisJob for q9

All jobs have two arguments for input and output paths expect q7 which has a third path argument to store temporary files
