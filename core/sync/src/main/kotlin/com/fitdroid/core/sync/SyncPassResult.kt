package com.fitdroid.core.sync

data class SyncPassResult(
    val status: Status,
    val error: String? = null,
    val retryable: Boolean = false,
) {
    enum class Status { Success, Skipped, Failed }

    val isSuccess: Boolean get() = status == Status.Success
    val isFailed: Boolean get() = status == Status.Failed
    val isSkipped: Boolean get() = status == Status.Skipped

    companion object {
        fun success(): SyncPassResult = SyncPassResult(Status.Success)

        fun skipped(reason: String): SyncPassResult =
            SyncPassResult(status = Status.Skipped, error = reason)

        fun failed(reason: String, retryable: Boolean = true): SyncPassResult =
            SyncPassResult(status = Status.Failed, error = reason, retryable = retryable)
    }
}

data class SyncOutcome(
    val healthConnect: SyncPassResult,
    val googleHealth: SyncPassResult,
) {
    val retry: Boolean
        get() = healthConnect.retryable || googleHealth.retryable
}
