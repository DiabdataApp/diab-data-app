package com.diabdata.core.backup

/** Errors that can occur while importing a backup via [BackupArchiveManager.readBackup]. */
sealed class BackupImportException(message: String) : Exception(message) {
    /** The backup content could not be recognized as any supported [BackupArchiveManager.BackupFormat]. */
    class InvalidFormat : BackupImportException("The backup format is invalid")

    /** The backup is encrypted, and no stored key could silently decrypt it; a password must be requested from the user. */
    class PasswordRequired : BackupImportException("A password is required to decrypt this backup")

    /** The password provided (either stored or entered by the user) failed to decrypt the backup. */
    class WrongPassword : BackupImportException("The provided password is incorrect")

    /** The backup content is empty. */
    class EmptyBackup : BackupImportException("The backup content is empty")
}
