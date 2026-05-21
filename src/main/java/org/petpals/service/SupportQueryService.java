package org.petpals.service;

import org.petpals.model.SupportQuery;
import org.petpals.model.User;
import org.petpals.repository.SupportQueryRepository;
import org.petpals.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SupportQueryService {

    private final SupportQueryRepository supportQueryRepository;
    private final UserRepository userRepository;

    public SupportQueryService(SupportQueryRepository supportQueryRepository,
                               UserRepository userRepository) {
        this.supportQueryRepository = supportQueryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SupportQuery submitQuery(Long userId, String queryText) {
        User user = userRepository.findById(userId).orElseThrow();
        SupportQuery q = new SupportQuery();
        q.setUser(user);
        q.setQueryText(queryText);
        q.setStatus("Open");
        return supportQueryRepository.save(q);
    }

    public List<SupportQuery> getQueriesByUser(Long userId) {
        return supportQueryRepository.findByUserIdOrderByQueryTimestampDesc(userId);
    }

    public List<SupportQuery> getOpenQueries() {
        return supportQueryRepository.findByStatusOrderByQueryTimestampAsc("Open");
    }

    public List<SupportQuery> getAllQueries() {
        return supportQueryRepository.findAllByOrderByQueryTimestampDesc();
    }

    @Transactional
    public boolean replyToQuery(Long queryId, String replyText) {
        Optional<SupportQuery> opt = supportQueryRepository.findById(queryId);
        if (opt.isPresent()) {
            SupportQuery q = opt.get();
            q.setAdminReply(replyText);
            q.setReplyTimestamp(LocalDateTime.now());
            q.setStatus("Answered");
            supportQueryRepository.save(q);
            return true;
        }
        return false;
    }

    @Transactional
    public void deleteQuery(Long queryId) {
        supportQueryRepository.deleteById(queryId);
    }
}

