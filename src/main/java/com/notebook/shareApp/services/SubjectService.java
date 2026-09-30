package com.notebook.shareApp.services;
import com.notebook.shareApp.dto.*;
import com.notebook.shareApp.entity.*;
import com.notebook.shareApp.repositories.BranchRepository;
import com.notebook.shareApp.repositories.SubjectRepository;
import com.notebook.shareApp.util.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final BranchRepository branchRepository;

    /** Reuses an existing subject when the (university, code) pair already exists —
     *  this is what keeps every note on "CS301" pointing at one shared subject
     *  instead of a fresh row per upload. Otherwise creates one. */
    @Transactional
    public Subject findOrCreateSubject(User user, NoteUploadRequest req) {
        String code = req.getSubjectCode().trim().toUpperCase();
        return subjectRepository.findByUniversityIdAndCodeIgnoreCase(user.getUniversity().getId(), code)
                .orElseGet(() -> createSubject(user, req, code));
    }

    private Subject createSubject(User user, NoteUploadRequest req, String code) {
        Branch branch = branchRepository.findById(req.getBranchId())
                .orElseThrow(() -> new NotFoundException("Selected branch does not exist"));

        Subject subject = new Subject();
        subject.setName(req.getSubjectName().trim());
        subject.setCode(code);
        subject.setSemester(req.getSemester());
        subject.setUniversity(user.getUniversity());
        subject.setBranch(branch);
        return subjectRepository.save(subject);
    }
}
