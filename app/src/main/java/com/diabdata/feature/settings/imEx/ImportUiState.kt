package com.diabdata.feature.settings.imEx

/** UI-facing state of an ongoing or completed backup import, exposed by [ImExViewModel.uiState]. */
sealed class ImportUiState {
    /** No import in progress. */
    object Idle : ImportUiState()

    /** An import is currently being read and/or decrypted. */
    object Loading : ImportUiState()

    /**
     * The backup is encrypted and could not be silently decrypted using the stored Keystore key.
     * The caller should prompt the user for a password and call [ImExViewModel.onPasswordSubmitted],
     * or [ImExViewModel.onPasswordDialogDismissed] to cancel.
     */
    object PasswordRequired : ImportUiState()

    /** The backup was successfully imported. */
    object Success : ImportUiState()

    /** The import failed; [message] describes the underlying error. */
    data class Error(val message: String) : ImportUiState()
}
