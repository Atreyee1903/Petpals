package org.petpals.repository;

import org.petpals.model.Testimonial;
import org.petpals.model.Testimonial.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {

    List<Testimonial> findByStatusOrderBySubmittedAtDesc(Status status);

    List<Testimonial> findByStatusOrderBySubmittedAtAsc(Status status);

    List<Testimonial> findAllByOrderBySubmittedAtDesc();
}

