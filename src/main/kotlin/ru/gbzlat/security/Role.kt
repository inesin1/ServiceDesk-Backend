package ru.gbzlat.security

enum class Role(val id: Int) {
    EMPLOYEE(1),
    SPECIALIST(2),
    ADMIN(3);

    companion object {
        fun byId(id: Int): Role? = entries.find { it.id == id }
    }
}
