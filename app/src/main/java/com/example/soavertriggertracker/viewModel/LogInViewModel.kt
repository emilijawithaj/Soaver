package com.example.soavertriggertracker.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soavertriggertracker.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogInViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel()
{
    private val _emailText = MutableStateFlow("")
    private val _error = MutableStateFlow(false)

    val emailText = _emailText.asStateFlow()
    val error = _error.asStateFlow()
    fun onEmailTextChanged(newText: String) {
       _emailText.value = newText
    }
    fun onSignInClick(password: String) {
        viewModelScope.launch {
            authRepository.signInWithEmail(_emailText.value, password)
        }
    }
}