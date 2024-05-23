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
        return goThroughArguments(sender, commands, label, args, true)
            ?: return ArrayList()
    }

    fun List<String>.returnWithStarting(str: String, ignoreCase: Boolean = false): List<String> {
        return this.filter { it.startsWith(str, ignoreCase = ignoreCase) }
    }
}