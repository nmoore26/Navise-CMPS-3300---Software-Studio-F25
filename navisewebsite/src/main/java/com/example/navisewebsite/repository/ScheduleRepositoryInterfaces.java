package com.example.navisewebsite.repository;

import com.example.navisewebsite.domain.ScheduleDomain.ScheduleCourse;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepositoryInterfaces {
    
    interface ScheduleCourseRepository {
        List<ScheduleCourse> coursesForPathway(String pathwayId);
        Optional<ScheduleCourse> courseById(int id);
        Optional<ScheduleCourse> courseByCode(String code);
    }
    
    interface ScheduleUserRepository {
        List<Integer> completedCourseIdsForUser(String userId);
    }
}