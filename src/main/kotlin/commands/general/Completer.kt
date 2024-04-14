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
            when (arg.tabCompletions != null) {
                true -> arg.tabCompletions!!(argInfo)
                false -> ArrayList<String>().also {
                    val inferiorArguments = when (arg.modifier) {
                        true -> arguments.also { it.remove(arg) }
                        false -> arg.followingArguments
                    }

                    inferiorArguments?.invokeMissingArg(argInfo)
                }
            }
        } ?: return ArrayList()
    }
}