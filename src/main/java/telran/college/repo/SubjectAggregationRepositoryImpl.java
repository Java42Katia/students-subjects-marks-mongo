package telran.college.repo;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;

import telran.college.documents.SubjectDoc;
import telran.college.dto.Subject;

@Repository
public class SubjectAggregationRepositoryImpl implements SubjectAggregationRepository {

	private static final String MARKS_COLLECTION = "marks";
	private static final String NEW_SUBJECT_MARK = "newSubject.mark";
	private static final String _ID = "_id";
	private static final String ID = "id";
	private static final String NEW_SUBJECT = "newSubject";
	private static final String SUBJECT = "subject";
	private static final String SUBJECT_NAME = "subjectName";
	private static final String AVG_MARK_FIELD = "avgMark";
	@Autowired
	MongoTemplate mongoTemplate;

	@Override
	public Subject findSubjectGreatestAvgMark() {
		Aggregation aggregation = newAggregation(lookup(MARKS_COLLECTION, SUBJECT_NAME, SUBJECT, NEW_SUBJECT),
				unwind(NEW_SUBJECT, true),
				group(ID, SUBJECT_NAME).avg(ConditionalOperators.ifNull(NEW_SUBJECT_MARK).then(0)).as(AVG_MARK_FIELD),
				sort(Direction.DESC, AVG_MARK_FIELD),
				limit(1),
				project().andExclude(AVG_MARK_FIELD));
		
		var doc = mongoTemplate.aggregate(aggregation, SubjectDoc.class, Document.class).getMappedResults().get(0).get(_ID, Document.class);
		
		return getSubjectFrom(doc);
	}
	
	@Override
	public List<Subject> findSubjectsAvgMarkGreater(int avgMark) {
		Aggregation aggregation = newAggregation(lookup(MARKS_COLLECTION, SUBJECT_NAME, SUBJECT, NEW_SUBJECT),
				unwind(NEW_SUBJECT, true),
				group(ID, SUBJECT_NAME).avg(ConditionalOperators.ifNull(NEW_SUBJECT_MARK).then(0)).as(AVG_MARK_FIELD),
				match(Criteria.where(AVG_MARK_FIELD).gte(avgMark)),
				sort(Direction.DESC, AVG_MARK_FIELD),
				project().andExclude(AVG_MARK_FIELD));
		
		var documents = mongoTemplate.aggregate(aggregation, SubjectDoc.class, Document.class).getMappedResults();
		
//		System.out.println("findSubjectsAvgMarkGreater then " + avgMark + ": \n");
//		documents.forEach(d -> System.out.println(d.toString()));
		
		return getMappedSubjects(documents);
	}

	@Override
	public List<Subject> findSubjectsAvgMarkLess(int avgMark) {
		Aggregation aggregation = newAggregation(lookup(MARKS_COLLECTION, SUBJECT_NAME, SUBJECT, NEW_SUBJECT),
				unwind(NEW_SUBJECT, true),
				group(ID, SUBJECT_NAME).avg(ConditionalOperators.ifNull(NEW_SUBJECT_MARK).then(0)).as(AVG_MARK_FIELD),
				match(Criteria.where(AVG_MARK_FIELD).lt(avgMark)),
				sort(Direction.ASC, ID),
				project().andExclude(AVG_MARK_FIELD));
		
		var documents = mongoTemplate.aggregate(aggregation, SubjectDoc.class, Document.class).getMappedResults();
		
//		System.out.println("findSubjectsAvgMarkLess then " + avgMark + ": \n");
//		documents.forEach(d -> System.out.println(d.toString()));
		
		return getMappedSubjects(documents);
	}

	private List<Subject> getMappedSubjects(List<Document> documents) {
		return documents.stream().map(d -> getSubjectFrom(d.get(_ID, Document.class))).toList();
	}

	private Subject getSubjectFrom(Document doc) {
		return new Subject(doc.getLong(ID), doc.getString(SUBJECT_NAME));
	}
}
