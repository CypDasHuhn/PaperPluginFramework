package commands

import commands.general.Argument
import commands.general.CustomCommand
import commands.general.RootArgument
import org.bukkit.entity.Player

const val LANGUAGE_KEY = "language"

@CustomCommand
var languageArgument = RootArgument(
    invoke = null,
    isValid = null,
    errorInvalid = null,
    labels = listOf("!language", "!l"),
    followingArguments = listOf(
        Argument(
            invoke = { sender, _, values ->
                if (sender !is Player) return@Argument;


            },
            isValid = null,
            errorInvalid = null,
            key = LANGUAGE_KEY
        )
    ),
)