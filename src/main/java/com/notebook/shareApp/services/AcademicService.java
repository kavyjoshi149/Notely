package com.notebook.shareApp.services;

import com.notebook.shareApp.payload.OptionResponse;
import com.notebook.shareApp.repositories.BranchRepository;
import com.notebook.shareApp.repositories.CourseRepository;
import com.notebook.shareApp.repositories.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicService {

    private final UniversityRepository universityRepository;
    private final CourseRepository courseRepository;
    private final BranchRepository branchRepository;

    public List<OptionResponse> getUniversities() {
        return universityRepository.findAll(Sort.by("name")).stream()
                .map(u -> new OptionResponse(u.getId(), u.getName()))
                .toList();
    }

    public List<OptionResponse> getCourses() {
        return courseRepository.findAll(Sort.by("name")).stream()
                .map(c -> new OptionResponse(c.getId(), c.getName()))
                .toList();
    }

    public List<OptionResponse> getBranchesByCourse(Long courseId) {
        if (courseId == null) {
            return List.of();
        }
        return branchRepository.findByCourseId(courseId).stream()
                .map(b -> new OptionResponse(b.getId(), b.getName()))
                .toList();
    }
}

