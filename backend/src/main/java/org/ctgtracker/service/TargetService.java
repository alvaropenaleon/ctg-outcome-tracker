package org.ctgtracker.service;

import java.util.List;

import org.ctgtracker.model.Target;
import org.ctgtracker.repository.TargetRepository;
import org.springframework.stereotype.Service;

@Service
public class TargetService {

    private final TargetRepository targetRepository;

    public TargetService(TargetRepository targetRepository) {
        this.targetRepository = targetRepository;
    }

    public List<Target> listTargets() {
        return targetRepository.findAll();
    }
}
