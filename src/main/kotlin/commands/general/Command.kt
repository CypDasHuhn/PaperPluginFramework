package commands.general

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


object Command : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        goThroughArguments(sender, command, label, args) { arg, (sender, argList, strArg, index, values), arguments ->
            arg.invoke?.let { it(sender, argList, values) } ?: {
                val inferiorArguments = when (arg.modifier) {
                    true -> arguments.also { it.remove(arg) }
                    false -> arg.followingArguments
                }

                inferiorArguments?.invokeMissingArg(ArgumentInfo(sender, argList, strArg, index, values))
            }
        }
        return false
    }
}