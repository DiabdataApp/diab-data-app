package com.diabdata.feature.userProfile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.diabdata.core.database.DataRepository
import com.diabdata.core.model.UserDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: DataRepository,
    application: Application
) : AndroidViewModel(application) {
    val userDetails: StateFlow<UserDetails?> = repository.getUserDetails()
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), null)

    fun updateUserDetails(userDetails: UserDetails) {
        viewModelScope.launch {
            repository.updateUserDetails(userDetails)
        }
    }

    fun deleteUserDetails() = viewModelScope.launch {
        repository.deleteUserDetails()
    }

    fun saveProfilePhoto(uri: Uri, onSaved: (String) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val bytes = application.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@launch // ou gérer l'échec explicitement

            val path = repository.saveProfilePhotoBytes(bytes, application.filesDir)

            withContext(Dispatchers.Main) {
                onSaved(path)
            }
        }
    }

    suspend fun updateProfilePhotoPath(path: String) {
        repository.addProfilePhotoPath(path)
    }

    suspend fun getProfilePhotoPath(): String? =
        repository.getUserDetails().first()?.profilePhotoPath
}
