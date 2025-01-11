package com.busanit501.bootproject.repository;

import com.busanit501.bootproject.domain.MatchingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchingRoomRepository extends JpaRepository<MatchingRoom, Long> {

    @Query("SELECT r.title FROM MatchingRoom r")
    List<String> findAllTitles();

    @Query("SELECT r.place FROM MatchingRoom r")
    List<String> findAllPlaces();

    @Query("SELECT p.type FROM MatchingRoom r JOIN r.participants rp JOIN rp.pet p")
    List<String> findAllPetTypes();

    @Query("SELECT DISTINCT r FROM MatchingRoom r " +
            "LEFT JOIN r.participants rp " +
            "LEFT JOIN rp.pet p " +
            "WHERE LOWER(r.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(r.place) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.type) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<MatchingRoom> searchRoomsByQuery(@Param("query") String query);
}
