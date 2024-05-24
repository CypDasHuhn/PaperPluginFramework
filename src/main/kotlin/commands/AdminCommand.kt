package commands

import frame.commands.Argument
import frame.commands.RootArgument
import frame.database.*

const val PLAYER_KEY = "player"
const val USER_NOT_ADMIN = "user_not_admin"
const val PLAYER_NOT_EXISTS = "player_not_admin"

val adminCommand = RootArgument(
    labels = listOf("admin"),
    followingArguments = listOf(
        Argument(
            invoke = { _, _, values ->
                val playerName = values[PLAYER_KEY] as String
                val player = getPlayerByName(playerName) ?: return@Argument

                player.isAdmin = !player.isAdmin
                player.updateDatabase()
            },
            tabCompletions = { (_, _, _, _, _) ->
                getPlayers().map { it.username }
            },
            errorMissing = { (sender, _, _, _, _) ->
                sender.sendMessage("You need to name a player!")
            },
            isValid = { (sender, _, arg, _, _) ->
                if (!sender.isAdmin()) Pair(false, USER_NOT_ADMIN)
                if (!playerExists(arg)) Pair(false, PLAYER_NOT_EXISTS)

                Pair(true, null)
            },
            errorInvalid = { (sender, _, _, _, _), key ->
                if (key == USER_NOT_ADMIN) {
                    sender.sendMessage("${sender.name} is not an admin!")
                }
                if (key == PLAYER_NOT_EXISTS) {
                    sender.sendMessage("${sender.name} does not exist!")
                }
            },
            key = PLAYER_KEY
        )
    )
)