package org.ctgtracker.controller;

import java.util.List;

import org.ctgtracker.model.dto.TargetResponse;
import org.ctgtracker.service.TargetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TargetController {

    private final TargetService targetService;

    public TargetController(TargetService targetService) {
        this.targetService = targetService;
    }

    @GetMapping("/api/targets")
    public List<TargetResponse> listTargets() {
        return targetService.listTargets().stream()
                .map(TargetResponse::from)
                .toList();
    }
}
