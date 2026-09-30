package com.notebook.shareApp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "branches",uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g. CSE, ECE, Mechanical
    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;
}