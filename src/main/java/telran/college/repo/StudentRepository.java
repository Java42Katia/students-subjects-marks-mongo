package telran.college.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import telran.college.documents.StudentDoc;
import telran.college.projection.*;

public interface StudentRepository extends MongoRepository<StudentDoc, Long>, StudentAggregationRepository {
//@Query(value="{'marks.subject': ?0, 'marks.mark': {$gte: ?1}}", fields = "{name: 1}")
	List<StudentNameProj> findByMarksSubjectAndMarksMarkGreaterThanEqual(String subjectName, int mark);
//@Query(value = "{'name': ?0, 'marks.subject': ?1}", fields = "{'marks': 1}")
StudentMarksProj findByNameAndMarksSubject(String name, String subjectName);

	@Query(value = "{'name': ?0}", fields = "{'marks': 1}")
	StudentMarksProj findByName(String name);

}
