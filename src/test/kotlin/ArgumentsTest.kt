import commands.general.Argument
import commands.general.getCommands
import kotlin.test.Test

fun argumentIntegrity(arguments: List<Argument>) {
    for (argument in arguments) {
        assert(argument.invoke != null || (!argument.followingArguments.isNullOrEmpty()))

        assert((argument.isValid != null) == (argument.errorInvalid != null))

        if (!argument.followingArguments.isNullOrEmpty()) {
            assert(argument.followingArguments!!.any { it.errorMissing != null })
            argumentIntegrity(argument.followingArguments!!)
        }
    }
}

@Test
fun argumentIntegrityInit(): Unit {
    argumentIntegrity(getCommands())
}