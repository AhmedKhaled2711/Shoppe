package com.lee.shoppe.ui.viewmodel

data class SignupUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val firstNameError: Boolean = false,
    val lastNameError: Boolean = false,
    val emailError: Boolean = false,
    val passwordError: Boolean = false,
    val confirmPasswordError: Boolean = false,
    val isLoading: Boolean = false,
    val signupSuccess: Boolean = false,
    val errorMessage: String? = null
)
