package com.openclassrooms.mddapi.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subscription")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription {

	@EmbeddedId
	@Setter(AccessLevel.NONE)
	private SubscriptionId id = new SubscriptionId();

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("userId")
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("subjectId")
	@JoinColumn(name = "subject_id", nullable = false)
	private Subject subject;

	public Subscription(User user, Subject subject) {
		this.user = user;
		this.subject = subject;
	}
}
