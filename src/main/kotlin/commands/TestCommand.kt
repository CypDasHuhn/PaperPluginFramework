package commands

import commands.general.Argument
import commands.general.Completer.returnWithStarting
import commands.general.CustomCommand
import commands.general.RootArgument
import commands.general.simpleModifierArgument
import database.Language
import database.playerIsAdmin
import database.updatePlayerLanguage
import org.bukkit.entity.Player

const val LANGUAGE_KEY = "language"
const val GLOBAL_KEY = "global"
const val NOT_ADMIN_KEY = "not_admin"
const val LANGUAGE_MISSING_KEY = "lang_missing"

/*
/!language <language>
/!language -global <language>
/!l <language>
/!l -global <language>
 */

@CustomCommand
var languageArgument = RootArgument(
    invoke = null,
    isValid = null,
    errorInvalid = null,
    labels = listOf("!language", "!l"),
    followingArguments = listOf(
        simpleModifierArgument(
            commandName = "-global",
            isValid = { (sender, _, _, _, _) ->
                if (sender !is Player) Pair(false, null)

                val isAdmin = playerIsAdmin((sender as Player).uniqueId.toString())
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
                Language.entries.map { it.name }.returnWithStarting(arg)
            },
            invoke = { sender, _, values ->
                val global = values[GLOBAL_KEY] as Boolean

                if (global) {
                    // TODO: Implement storing a global State!
                } else {
                    val player: Player = sender as Player
                    val language = values[LANGUAGE_KEY] as String

                    updatePlayerLanguage(database.Player(player.uniqueId.toString(), player.name, language))
                }
            },
            isValid = { (sender, _, arg, _, _) ->
                if (sender !is Player) Pair(false, null)

                val languageExists = Language.entries.toTypedArray().contentToString().contains(arg)
                if (languageExists) Pair(false, LANGUAGE_MISSING_KEY)

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