package com.notebook.shareApp.services;

import com.notebook.shareApp.entity.*;
import com.notebook.shareApp.payload.*;
import com.notebook.shareApp.repositories.*;
import com.notebook.shareApp.util.RegistrationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final CourseRepository courseRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest req) {
        String username = req.getUsername().trim().toLowerCase();
        String email = req.getEmail().trim().toLowerCase();
        String rollNumber = req.getRollNumber().trim();

        if (userRepository.existsByUsername(username)) {
            throw new RegistrationException("username", "This username is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RegistrationException("email", "An account with this email already exists");
        }

        University university = universityRepository.findById(req.getUniversityId())
                .orElseThrow(() -> new RegistrationException("universityId", "Select a valid university"));
        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new RegistrationException("courseId", "Select a valid course"));
        Branch branch = branchRepository.findById(req.getBranchId())
                .orElseThrow(() -> new RegistrationException("branchId", "Select a valid branch"));

        if (!branch.getCourse().getId().equals(course.getId())) {
            throw new RegistrationException("branchId", "This branch does not belong to the selected course");
        }
        if (userRepository.existsByUniversityIdAndRollNumber(university.getId(), rollNumber)) {
            throw new RegistrationException("rollNumber", "This roll number is already registered at this university");
        }

        User user = new User();
        user.setName(req.getName().trim());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setBatch(req.getBatch().trim());
        user.setRollNumber(rollNumber);
        user.setUniversity(university);
        user.setCourse(course);
        user.setBranch(branch);
        user.setRole(Role.STUDENT);
        user.setEnabled(true);

        return userRepository.save(user);
    }
}

