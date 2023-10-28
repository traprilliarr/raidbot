package com.example.raid_bot_2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BotRepository extends JpaRepository<Request, String> {

    @Query(value = "SELECT * FROM request ORDER BY date_time DESC LIMIT 1", nativeQuery = true)
    Request findByDateTime();
}
