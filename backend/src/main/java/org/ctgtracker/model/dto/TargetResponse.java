package org.ctgtracker.model.dto;

import org.ctgtracker.model.Target;

public record TargetResponse(
        String id,
        String category,
        String title,
        String definition,
        String sourceUrl
) {
    public static TargetResponse from(Target target) {
        return new TargetResponse(target.id(), target.category(), target.title(), target.definition(), target.sourceUrl());
    }
}
