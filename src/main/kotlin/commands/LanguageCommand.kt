package commands

import commands.general.Argument
import commands.general.Completer.returnWithStarting
import commands.general.CustomCommand
import commands.general.RootArgument
import commands.general.simpleModifierArgument
import database.*
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import t

const val LANGUAGE_KEY = "language"
const val GLOBAL_KEY = "global"
const val GLOBAL_COMMAND = "-global"
const val LANGUAGE_NOT_EXISTING = "lang_not_existing"

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
            isValidCompleter = { (sender, _, _, _, _) ->
                sender.isAdmin()
            },
            key = GLOBAL_KEY,
        ),
        Argument(
            tabCompletions = { (_, _, arg, _, _) ->
                Language.values().map { it.name }.returnWithStarting(arg)
            },
            invoke = { sender, _, values ->
                val global = values[GLOBAL_KEY] as Boolean
                val langStr = values[LANGUAGE_KEY] as String
                val lang = Language.valueOf(langStr)

                fun langChangeMessage(sender: CommandSender, language: Language?, global: Boolean) {
                    val scale = if (global) "scale_global" else "scale_your"
                    sender.sendMessage(t("set_language", language, Pair("scale", t(scale, language))))
                }

                if (global) {
                    globalLanguage = lang
                    val senderLang = sender.language()
                    langChangeMessage(sender, senderLang, true)
                } else {
                    val player = cachedPlayerData(sender as Player)
                    player.language = lang
                    player.updateDatabase()

                    langChangeMessage(sender, lang, false)
                }
            },
            isValid = { (sender, _, arg, _, values) ->
                val global = values[GLOBAL_KEY] as Boolean? ?: false
                val consoleChangingGlobal = sender is ConsoleCommandSender && global

                // validness check for command blocks (which are invalid) and console which changes NOT global (also invalid)
                if (sender !is Player || consoleChangingGlobal) return@Argument Pair(false, null)

                val languageExists = Language.values().contentToString().contains(arg)
                if (!languageExists) return@Argument Pair(false, LANGUAGE_NOT_EXISTING)

                return@Argument Pair(true, null)
            },
            errorInvalid = { (sender, _, arg, _, _), errorKey ->
                if (errorKey == LANGUAGE_NOT_EXISTING) {
                    sender.sendMessage(t("not_valid_language", sender.language(), Pair("lang", arg)))
                }
            },
            errorMissing = { (sender, _, _, _, _) ->
                sender.sendMessage(t("specify_language", sender.language()))

            },
            key = LANGUAGE_KEY
        )
    ),
)