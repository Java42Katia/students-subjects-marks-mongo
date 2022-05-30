package telran.college.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import telran.college.documents.*;
import telran.college.projection.*;

public interface StudentRepository extends MongoRepository<StudentDoc, Long> {
	@Query(value="{'marks.subject': ?0, 'marks.mark': {$gte: ?1}}", fields = "{name: 1}")
	List<StudentNameProj> findByMarksSubjectAndMarksMarkGreaterThanEqual(String subjectName, int mark);

	@Query(value = "{'name': ?0, 'marks.subject': ?1}", fields = "{'marks.mark': 1}")
	StudentMarksProj findByNameAndMarksSubject(String name, String subjectName);


}
