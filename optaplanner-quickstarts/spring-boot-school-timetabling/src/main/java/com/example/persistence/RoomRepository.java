package com.example.persistence;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.example.domain.Room;

public interface RoomRepository extends PagingAndSortingRepository<Room, Long> {

    @Override
    List<Room> findAll();

}
