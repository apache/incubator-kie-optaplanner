package com.example.persistence;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.example.domain.Timeslot;

public interface TimeslotRepository extends PagingAndSortingRepository<Timeslot, Long> {

    @Override
    List<Timeslot> findAll();

}
