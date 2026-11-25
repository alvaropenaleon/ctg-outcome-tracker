package org.ctgtracker.model;

public record Target(
        String id,
        String category,
        String title,
        String definition,
        String sourceUrl
) {
}
