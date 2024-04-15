import commands.general.Argument
import commands.general.getCommands
import kotlin.test.Test

class ArgumentTest {
    private fun argumentIntegrity(arguments: List<Argument>) {
        for (argument in arguments) {
            assert(
                (!argument.isModifier && (argument.invoke != null || !argument.followingArguments.isNullOrEmpty())) ||
                        (argument.isModifier && argument.invoke == null && argument.followingArguments.isNullOrEmpty())
            ) {
                "$argument has no invoke and no follow arguments"
            }

            assert((argument.isValid != null) == (argument.errorInvalid != null)) {
                "$argument has to either hav isValid & errorInvalid or neither"
            }

            if (!argument.followingArguments.isNullOrEmpty()) {
                assert(argument.followingArguments!!.any { it.errorMissing != null }) {
                    "$argument with follow arguments needs to have a child with errorMissing"
                }
                argumentIntegrity(argument.followingArguments!!)
            }
        }
    }

    @Test
    fun argumentIntegrityInit() {
        argumentIntegrity(getCommands())
    }
}