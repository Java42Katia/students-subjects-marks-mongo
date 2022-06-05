package telran.college.repo;

import java.util.List;

import telran.college.dto.Subject;

public interface SubjectAggregationRepository {

	Subject findSubjectGreatestAvgMark();
	List<Subject> findSubjectsAvgMarkGreater(int avgMark);
	List<Subject> findSubjectsAvgMarkLess(int avgMark);

}
