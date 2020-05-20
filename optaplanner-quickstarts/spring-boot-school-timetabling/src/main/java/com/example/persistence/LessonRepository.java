package com.example.persistence;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.example.domain.Lesson;

public interface LessonRepository extends PagingAndSortingRepository<Lesson, Long> {

    @Override
    List<Lesson> findAll();

}
