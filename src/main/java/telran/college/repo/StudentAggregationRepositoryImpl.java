package telran.college.repo;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import org.springframework.stereotype.Repository;

import telran.college.documents.StudentDoc;
import telran.college.documents.SubjectMark;
import telran.college.dto.Student;
import telran.college.dto.Subject;
@Repository
public class StudentAggregationRepositoryImpl implements StudentAggregationRepository {
private static final String MIN_MARK = "min_mark";
private static final String COUNT = "count";
private static final String DOCUMENT = "doc";
private static final String MAX_COUNT = "max_count";
private static final String _ID = "_id";
private static final String AVG_MARK_FIELD = "avgMark";
private static final String STUDENT_ID_FIELD = "id";
private static final String STUDENT_NAME_FIELD = "name";
private static final String SUBJECT_NAME_FIELD = "marks.subject";
private static final String SUBJECT_MARK_FIELD = "marks.mark";
private static final String STUDENT_MARKS_FIELD = "marks";
private static final String DOCUMENT_ID = _ID;
@Autowired
	MongoTemplate mongoTemplate;
	@Override
	public List<Student> findTopBestStudents(int nStudents) {
		UnwindOperation unwindOperation = unwind("marks");
		GroupOperation groupOperation = group("id", "name").avg("marks.mark").as(AVG_MARK_FIELD);
		SortOperation sortOperation = sort(Direction.DESC, AVG_MARK_FIELD );
		LimitOperation limitOperation = limit(nStudents);
		ProjectionOperation projectionOperation = project().andExclude(AVG_MARK_FIELD);
		Aggregation aggregation = newAggregation(unwindOperation, groupOperation, sortOperation,
				limitOperation, projectionOperation);
		var documents = mongoTemplate.aggregate(aggregation, StudentDoc.class, Document.class);
		return getStudentsResult(documents);
	}
	private List<Student> getStudentsResult(AggregationResults<Document> documents) {
		return documents.getMappedResults().stream().map(this::getStudent).toList();
	}
	private Student getStudent(Document doc) {
		Document idDocument = doc.get(_ID, Document.class);
		return new Student(idDocument.getLong("id"), idDocument.getString("name"));
	}
	@Override
	public List<Student> findGoodStudents() {
		ArrayList<AggregationOperation> pipeline = new ArrayList<>();
		pipeline.add(unwind("marks"));
		pipeline.add(group("id", "name").avg("marks.mark").as(AVG_MARK_FIELD));
		double avgMark = getCollegeAvgMark();
		pipeline.add(match(Criteria.where(AVG_MARK_FIELD).gt(avgMark)));
		pipeline.add(sort(Direction.DESC, AVG_MARK_FIELD));
		pipeline.add(project().andExclude(AVG_MARK_FIELD));
		Aggregation aggregation = newAggregation(pipeline);
		var documents = mongoTemplate.aggregate(aggregation, StudentDoc.class, Document.class);
		return getStudentsResult(documents);
	}
	private double getCollegeAvgMark() {
		ArrayList<AggregationOperation> pipeline = new ArrayList<>();
		pipeline.add(unwind("marks"));
		pipeline.add(group().avg("marks.mark").as(AVG_MARK_FIELD));
		Aggregation aggregation = newAggregation(pipeline);
		var document = mongoTemplate.aggregate(aggregation, StudentDoc.class, Document.class)
				.getUniqueMappedResult();
		return document.getDouble(AVG_MARK_FIELD);
	}
	
	/************************** hw 67 ***************************/
	
	@Override
	public List<Integer> findStudentMarksSubject(String name, String subjectName) {
		Aggregation aggregation = newAggregation(unwind(STUDENT_MARKS_FIELD),
				match(Criteria.where(SUBJECT_NAME_FIELD).is(subjectName).and(STUDENT_NAME_FIELD).is(name)),
				group(SUBJECT_MARK_FIELD),
				sort(Direction.ASC, _ID));
		
		var documents = mongoTemplate.aggregate(aggregation, StudentDoc.class, Document.class).getMappedResults();
		
//		System.out.println("findStudent " + name + " MarksSubject " + subjectName + ": \n");
//		documents.forEach(d -> System.out.println(d.toString()));
		
		return documents.stream().map(i -> i.getInteger(_ID)).toList();
	}
	
	@Override
	public List<Student> findBestStudentsSubject(int nStudents, String subjectName) {
		Aggregation aggregation = newAggregation(unwind(STUDENT_MARKS_FIELD),
				match(Criteria.where(SUBJECT_NAME_FIELD).is(subjectName)),
				group(STUDENT_ID_FIELD, STUDENT_NAME_FIELD).avg(SUBJECT_MARK_FIELD).as(AVG_MARK_FIELD),
				sort(Direction.DESC, AVG_MARK_FIELD),
				project().andExclude(AVG_MARK_FIELD),
				limit(nStudents));
		
		var documents = mongoTemplate.aggregate(aggregation, StudentDoc.class, Document.class);
		
		return getStudentsResult(documents);
	}
	@Override
	public List<Student> findStudentsAllMarksSubjectGreaterThen(int mark, String subject) {
		Aggregation aggregation = newAggregation(unwind(STUDENT_MARKS_FIELD),
				match(Criteria.where(SUBJECT_NAME_FIELD).is(subject)),
				group(STUDENT_ID_FIELD, STUDENT_NAME_FIELD).min(SUBJECT_MARK_FIELD).as(MIN_MARK),
				match(Criteria.where(MIN_MARK).gte(mark)),
				project().andExclude(MIN_MARK));
		
		var documents = mongoTemplate.aggregate(aggregation, StudentDoc.class, Document.class);
		
//		System.out.println("findStudentsAllMarksSubjectGreaterThen then " + mark + ": \n");
//		documents.forEach(d -> System.out.println(d.toString()));
		
		return getStudentsResult(documents);
	}
	@Override
	public List<Student> findStudentsMaxMarksCount() {
		Aggregation aggregation = newAggregation(unwind(STUDENT_MARKS_FIELD),
				group(STUDENT_ID_FIELD, STUDENT_NAME_FIELD).count().as(COUNT),
				group(STUDENT_ID_FIELD, STUDENT_NAME_FIELD).max(COUNT).as(MAX_COUNT),
				group(MAX_COUNT).push(_ID).as(DOCUMENT),
				project(DOCUMENT).and(MAX_COUNT).previousOperation(),
				sort(Direction.DESC, MAX_COUNT),
				limit(1),
				group(DOCUMENT),
				unwind(_ID));
				
		var documents = mongoTemplate.aggregate(aggregation, StudentDoc.class, Document.class);
		
//		System.out.println("findStudentsMaxMarksCount: \n");
//		documents.forEach(d -> System.out.println(d.toString()));
		
		return getStudentsResult(documents);
	}
	@Override
	public void deleteStudentsAvgMarkLess(int avgMark) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public List<Student> deleteStudentsMarksCountLess(int count) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	

}
