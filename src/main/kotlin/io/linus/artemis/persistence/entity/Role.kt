package io.linus.artemis.persistence.entity

enum class Role(val role: String) {
    VISITOR("ROLE_VISITOR"),
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN"),
    UNKNOWN("ROLE_UNKNOWN")
}