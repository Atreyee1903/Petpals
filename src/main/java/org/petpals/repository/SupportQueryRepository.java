package org.petpals.repository;

import org.petpals.model.SupportQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportQueryRepository extends JpaRepository<SupportQuery, Long> {

    List<SupportQuery> findByUserIdOrderByQueryTimestampDesc(Long userId);

    List<SupportQuery> findByStatusOrderByQueryTimestampAsc(String status);

    List<SupportQuery> findAllByOrderByQueryTimestampDesc();
}

