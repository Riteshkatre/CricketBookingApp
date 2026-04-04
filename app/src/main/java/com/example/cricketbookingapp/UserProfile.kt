package com.example.cricketbookingapp

data class UserProfile(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val mobileNumber: String = "",
    val email: String = ""
) {
    val fullName: String
        get() = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ").trim()

    val initials: String
        get() = buildString {
            firstName.firstOrNull()?.let { append(it.uppercaseChar()) }
            lastName.firstOrNull()?.let { append(it.uppercaseChar()) }
        }.ifBlank { "U" }
}
