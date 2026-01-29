package com.openclassrooms.mddapi;

import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.time.Instant;

public final class TestSupport {
	private TestSupport() {
	}

	public static <T> T setId(T entity, Long id) {
		setField(entity, "id", id);
		return entity;
	}

	public static <T> T setCreatedAt(T entity, Instant createdAt) {
		setField(entity, "createdAt", createdAt);
		return entity;
	}

	public static <T> T newInstance(Class<T> type) {
		try {
			Constructor<T> constructor = type.getDeclaredConstructor();
			constructor.setAccessible(true);
			return constructor.newInstance();
		} catch (ReflectiveOperationException ex) {
			throw new IllegalStateException("Unable to instantiate " + type.getName(), ex);
		}
	}

	private static void setField(Object target, String fieldName, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (ReflectiveOperationException ex) {
			throw new IllegalStateException("Unable to set field " + fieldName, ex);
		}
	}
}
