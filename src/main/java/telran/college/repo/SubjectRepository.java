package telran.college.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import telran.college.documents.SubjectDoc;
import telran.college.dto.Subject;

public interface SubjectRepository extends MongoRepository<SubjectDoc, Long>, SubjectAggregationRepository {

	Subject findBySubjectName(String subjectName);
	List<Subject> findBySubjectName(List<String> subjectName);

}
