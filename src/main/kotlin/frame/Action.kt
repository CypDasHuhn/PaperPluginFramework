package frame

import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

data class RepeatData(
    val repeatCount: Int = 1,
    val offset: Long = 0,
    val infinite: Boolean = false
)

data class TimeOutData(
    val timeOutLength: Long = 0,
    val timeOutError: () -> Unit
)


data class ProgressData<T>(
    val offset: Long,
    val value: MutableValue<T>,
    val progressHandler: suspend (MutableValue<T>) -> Unit,
)

data class MutableValue<T>(var value: T)

suspend fun <T> startWithProgress(
    delay: Long = 0,
    repeatData: RepeatData? = null,
    async: Boolean = false,
    timeOutData: TimeOutData? = null,
    progressData: ProgressData<T>,
    action: suspend (data: MutableValue<T>) -> Unit
): Job {
    return start(delay, repeatData, async, timeOutData) {
        val job = coroutineContext.job
        val currentState = progressData.value

        val progressJob = CoroutineScope(coroutineContext).launch {
            while (job.isActive) {
                val progress = progressData.progressHandler(currentState)
                println("Progress: $progress")
                delay(progressData.offset)
            }
        }

        try {
            action(currentState)
        } finally {
            progressJob.cancel()
        }
    }
}

suspend fun start(
    delay: Long = 0,
    repeatData: RepeatData? = null,
    async: Boolean = false,
    timeOutData: TimeOutData? = null,
    action: suspend () -> Unit
): Job {
    val scope = if (async) CoroutineScope(Dispatchers.Default) else CoroutineScope(Dispatchers.IO)

    delay(delay)
    return scope.launch {
        repeatData?.let { repeatInfo ->
            if (repeatInfo.infinite) {
                while (isActive) {
                    performActionWithTimeout(timeOutData, action)
                    delay(repeatInfo.offset)
                }
            } else {
                repeat(repeatInfo.repeatCount) {
                    performActionWithTimeout(timeOutData, action)
                    delay(repeatInfo.offset)
                }
            }
        } ?: performActionWithTimeout(timeOutData, action)
    }
}

suspend fun performActionWithTimeout(
    timeOutData: TimeOutData? = null,
    action: suspend () -> Unit
) {
    val actionJob = coroutineScope {
        async {
            try {
                withTimeout(timeOutData?.timeOutLength ?: Long.MAX_VALUE) {
                    action()
                }
            } catch (e: TimeoutCancellationException) {
                timeOutData?.timeOutError?.invoke()
                null
            }
        }
    }
    actionJob.await()
}