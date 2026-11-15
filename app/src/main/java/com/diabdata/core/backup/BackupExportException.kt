package com.diabdata.core.backup

/** Errors that can occur while exporting a backup via [BackupArchiveManager.writeBackup]. */
sealed class BackupExportException(message: String, cause: Throwable? = null) :
    Exception(message, cause) {
    /** The backup could not be encrypted: no key is stored, or the key could not be retrieved. */
    class EncryptionKeyUnavailable(cause: Throwable? = null) :
        BackupExportException("Unable to get encryption key", cause)
}
