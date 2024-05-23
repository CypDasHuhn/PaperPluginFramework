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
import tSend

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
            argumentHandler = { (_, _, arg, _, _) -> Language.valueOf(arg.uppercase()) },
            tabCompletions = { (_, _, arg, _, _) ->
                Language.values().map { it.name }.returnWithStarting(arg, ignoreCase = true)
            },
            invoke = { sender, _, values ->
                val global = values[GLOBAL_KEY] as Boolean
                val lang = values[LANGUAGE_KEY] as Language

                fun langChangeMessage(
                    sender: CommandSender, language: Language?, targetLang: Language?, global: Boolean,
                ) {
                    val scaleKey = if (global) "scale_global" else "scale_your"
                    val langKey = "lang_${targetLang.toString().lowercase()}"
                    tSend(
                        sender,
                        "set_language",
                        Pair("scale", t(scaleKey, language)),
                        Pair("lang", t(langKey, language))
                    )
                }

                if (global) {
                    globalLanguage = lang
                    val senderLang = sender.language()
                    langChangeMessage(sender, senderLang, lang, true)
                } else {
                    val player = cachedPlayerData(sender as Player)
                    player.language = lang
                    player.updateDatabase()

                    langChangeMessage(sender, lang, lang, false)
                }
            },
            isValid = { (sender, _, arg, _, values) ->
                val global = values[GLOBAL_KEY] as Boolean? ?: false
                val consoleChangingGlobal = sender is ConsoleCommandSender && global

                // validness check for command blocks (which are invalid) and console which changes NOT global (also invalid)
                if (sender !is Player || consoleChangingGlobal) return@Argument Pair(false, null)

                val languageExists = Language.values().any { it.name.equals(arg, ignoreCase = true) }
                if (!languageExists) return@Argument Pair(false, LANGUAGE_NOT_EXISTING)

                return@Argument Pair(true, null)
            },
            errorInvalid = { (sender, _, arg, _, _), errorKey ->
                if (errorKey == LANGUAGE_NOT_EXISTING) {
                    tSend(sender, "not_valid_language", Pair("lang", arg))
                }
            },
            errorMissing = { (sender, _, _, _, _) ->
                tSend(sender, "specify_language")
            },
            key = LANGUAGE_KEY
        )
    ),
)