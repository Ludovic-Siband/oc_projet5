package com.openclassrooms.mddapi.feature.subject.dto;

public record SubjectResponse(Long id, String name, String description, boolean subscribed) {
}
