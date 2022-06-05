package telran.college.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import telran.college.documents.SubjectMark;

public interface SubjectMarkRepository extends MongoRepository<SubjectMark, Long> {

}
