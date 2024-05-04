package commands

import commands.general.Argument
import commands.general.Completer.returnWithStarting
import commands.general.CustomCommand
import commands.general.RootArgument
import commands.general.simpleModifierArgument
import database.*
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

const val LANGUAGE_KEY = "language"
const val GLOBAL_KEY = "global"
const val GLOBAL_COMMAND = "-global"
const val NOT_ADMIN_KEY = "not_admin"
const val LANGUAGE_MISSING_KEY = "lang_missing"

@OptIn(ExperimentalStdlibApi::class)
@CustomCommand
var languageCommand = RootArgument(
    labels = listOf("language", "l"),
    startingUnit = { sender ->
        if (sender is Player) cachedPlayerData(sender)

        true
    },
    followingArguments = listOf(
        simpleModifierArgument(
            commandName = GLOBAL_COMMAND,
            isArgument = { (sender, _, arg, _, _) ->
                arg == GLOBAL_COMMAND && sender.isAdmin()
            },
            isValid = { (sender, _, _, _, _) ->
                val isAdmin = sender.isAdmin()
                Pair(isAdmin, NOT_ADMIN_KEY)
            },
            errorInvalid = { (sender, _, _, _, _), errorKey ->
                if (errorKey == NOT_ADMIN_KEY) {
                    sender.sendMessage("${sender.name} is not an admin!")
                }
            },
            key = GLOBAL_KEY,
        ),
        Argument(
            tabCompletions = { (_, _, arg, _, _) ->
                Language.values().map { it.name }.returnWithStarting(arg)
            },
            invoke = { sender, _, values ->
                val global = values[GLOBAL_KEY] as Boolean

                if (global) {
                    globalLanguage = Language.valueOf(values[LANGUAGE_KEY] as String)
                } else {
                    val language = values[LANGUAGE_KEY] as String
                    val player = cachedPlayerData(sender as Player)

                    player.language = language
                    player.updateDatabase()
                }
            },
            isValid = { (sender, _, arg, _, values) ->
                val global = values[GLOBAL_KEY] as Boolean? ?: false
                val consoleChangingGlobal = sender is ConsoleCommandSender && global

                if (sender !is Player || consoleChangingGlobal) Pair(false, null)

                val languageExists = Language.values().contentToString().contains(arg)
                if (!languageExists) Pair(false, LANGUAGE_MISSING_KEY)

                Pair(true, null)
            },
            errorInvalid = { (sender, _, arg, _, _), errorKey ->
                if (errorKey == LANGUAGE_MISSING_KEY) {
                    sender.sendMessage("$arg is not a valid language")
                }
            },
            errorMissing = { (sender, _, _, _, _) ->
                sender.sendMessage("You need to specify a language!")
            },
            key = LANGUAGE_KEY
        )
    ),
)