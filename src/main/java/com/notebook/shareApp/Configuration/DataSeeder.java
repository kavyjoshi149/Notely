package com.notebook.shareApp.Configuration;

import com.notebook.shareApp.entity.Branch;
import com.notebook.shareApp.entity.Course;
import com.notebook.shareApp.entity.University;
import com.notebook.shareApp.repositories.BranchRepository;
import com.notebook.shareApp.repositories.CourseRepository;
import com.notebook.shareApp.repositories.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/** Inserts starter academic data once, when the database is empty. Edit the lists to match your users. */
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final UniversityRepository universityRepository;
    private final CourseRepository courseRepository;
    private final BranchRepository branchRepository;

    @Bean
    CommandLineRunner seedAcademicData() {
        return args -> {
            if (universityRepository.count() == 0) {
                for (String[] u : new String[][]{
                        {"Dr. A.P.J. Abdul Kalam Technical University", "Lucknow"},
                        {"University of Delhi", "Delhi"},
                        {"University of Mumbai", "Mumbai"}}) {
                    University uni = new University();
                    uni.setName(u[0]);
                    uni.setCity(u[1]);
                    universityRepository.save(uni);
                }
            }

            if (courseRepository.count() == 0) {
                Map<String, Integer> courses = Map.of("B.Tech", 4, "BCA", 3, "MCA", 2, "B.Sc", 3, "MBA", 2);
                Map<String, List<String>> branches = Map.of(
                        "B.Tech", List.of("CSE", "IT", "ECE", "EEE", "Mechanical", "Civil"),
                        "BCA", List.of("Computer Applications"),
                        "MCA", List.of("Computer Applications"),
                        "B.Sc", List.of("Physics", "Chemistry", "Mathematics", "Computer Science"),
                        "MBA", List.of("Marketing", "Finance", "HR"));

                courses.forEach((name, years) -> {
                    Course course = new Course();
                    course.setName(name);
                    course.setDurationYears(years);
                    courseRepository.save(course);

                    for (String b : branches.get(name)) {
                        Branch branch = new Branch();
                        branch.setName(b);
                        branch.setCourse(course);
                        branchRepository.save(branch);
                    }
                });
            }
        };
    }
}
