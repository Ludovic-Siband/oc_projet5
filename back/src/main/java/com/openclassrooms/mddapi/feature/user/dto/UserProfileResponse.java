package com.openclassrooms.mddapi.feature.user.dto;

import java.util.List;

public record UserProfileResponse(Long id, String email, String username, List<SubscriptionDto> subscriptions) {
}
