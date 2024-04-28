package commands.general

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

object Completer : TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        commands: Command,
        label: String,
        args: Array<String>
    ): List<String> {
        return goThroughArguments(sender, commands, label, args) { arg, argInfo, arguments ->
            when (arguments.any { it.tabCompletions != null }) {
                true -> arguments.flatMap { it.tabCompletions!!(argInfo) }
                false -> ArrayList<String>().also {
                    val inferiorArguments = when (arg.isModifier) {
                        true -> arguments.also { it.remove(arg) }
                        false -> arg.followingArguments
                    }

                    inferiorArguments?.invokeMissingArg(argInfo)
                }
            }
        } ?: return ArrayList()
    }

    fun List<String>.returnWithStarting(str: String): List<String> {
        return this.filter { it.startsWith(str) }
    }
}