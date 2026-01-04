package com.openclassrooms.mddapi.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class SubscriptionId implements Serializable {

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "subject_id", nullable = false)
	private Long subjectId;

	public SubscriptionId(Long userId, Long subjectId) {
		this.userId = userId;
		this.subjectId = subjectId;
	}
}
