package org.petpals.service;

import org.petpals.model.Testimonial;
import org.petpals.model.Testimonial.Status;
import org.petpals.model.User;
import org.petpals.repository.TestimonialRepository;
import org.petpals.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TestimonialService {

    private final TestimonialRepository testimonialRepository;
    private final UserRepository userRepository;

    public TestimonialService(TestimonialRepository testimonialRepository,
                              UserRepository userRepository) {
        this.testimonialRepository = testimonialRepository;
        this.userRepository = userRepository;
    }

    public List<Testimonial> getApprovedTestimonials() {
        return testimonialRepository.findByStatusOrderBySubmittedAtDesc(Status.APPROVED);
    }

    public List<Testimonial> getPendingTestimonials() {
        return testimonialRepository.findByStatusOrderBySubmittedAtAsc(Status.PENDING);
    }

    public List<Testimonial> getAllTestimonials() {
        return testimonialRepository.findAllByOrderBySubmittedAtDesc();
    }

    @Transactional
    public Testimonial submitTestimonial(Long userId, String name, String location,
                                         String petName, String text, Integer rating, String image) {
        User user = userRepository.findById(userId).orElseThrow();
        Testimonial t = new Testimonial();
        t.setUser(user);
        t.setName(name);
        t.setLocation(location);
        t.setPetName(petName);
        t.setText(text);
        t.setRating(rating);
        t.setImage(image);
        t.setStatus(Status.PENDING);
        return testimonialRepository.save(t);
    }

    @Transactional
    public boolean updateStatus(Long testimonialId, Status newStatus) {
        Optional<Testimonial> opt = testimonialRepository.findById(testimonialId);
        if (opt.isPresent()) {
            opt.get().setStatus(newStatus);
            testimonialRepository.save(opt.get());
            return true;
        }
        return false;
    }

    @Transactional
    public void deleteTestimonial(Long id) {
        testimonialRepository.deleteById(id);
    }
}

