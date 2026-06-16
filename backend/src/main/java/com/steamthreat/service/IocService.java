package com.steamthreat.service;

import com.steamthreat.entity.Ioc;
import com.steamthreat.repository.IocRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class IocService {

    private final IocRepository iocRepository;

    public List<Ioc> findAll() {
        return iocRepository.findAll();
    }

    public Optional<Ioc> findById(Long id) {
        return iocRepository.findById(id);
    }

    public Ioc save(Ioc ioc) {
        return iocRepository.save(ioc);
    }

    public void deleteById(Long id) {
        iocRepository.deleteById(id);
    }

    public List<Ioc> findByType(String iocType) {
        return iocRepository.findByIocType(iocType);
    }

    public List<Ioc> search(String keyword) {
        return iocRepository.findByIocValueContaining(keyword);
    }

    public Map<String, Long> statsByType() {
        Map<String, Long> stats = new LinkedHashMap<>();
        for (Object[] row : iocRepository.countByType()) {
            stats.put((String) row[0], (Long) row[1]);
        }
        return stats;
    }

    public Map<String, Long> statsByRiskLevel() {
        Map<String, Long> stats = new LinkedHashMap<>();
        for (Object[] row : iocRepository.countByRiskLevel()) {
            stats.put((String) row[0], (Long) row[1]);
        }
        return stats;
    }
}
