import kotlinx.coroutines.*

object Action {
    data class RepeatData(
        val repeatCount: Int,
        val offset: Int
    )

    data class TimeOutData(
        val timeOutLength: Int,
        val timeOutError: () -> Unit
    )

    fun start(delay: Long = 0,
              repeatData: RepeatData? = null,
              async: Boolean = false,
              timeOutData: TimeOutData? = null,
              action: () -> Any?
    ): Any? {
        return CoroutineScope(Dispatchers.Default).launch {
            delay(delay)

            val job = launch {
                repeatData?.let { repeatInfo ->
                    repeat(repeatInfo.repeatCount) {
                        if (isActive) {
                            action()
                        } else {
                            return@launch
                        }
                    }
                } ?: action.invoke()
            }

            job.invokeOnCompletion {
                if (it is CancellationException) {
                    println("Action cancelled")
                }
            }

            job.join() // Wait for the action to complete
        }
    }
}