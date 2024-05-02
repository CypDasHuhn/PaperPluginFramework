package commands

import commands.general.Argument
import commands.general.RootArgument
import database.*

const val PLAYER_KEY = "player"
const val USER_NOT_ADMIN = "user_not_admin"
const val PLAYER_NOT_EXISTS = "player_not_admin"

val adminCommand = RootArgument(
    labels = listOf("admin"),
    followingArguments = listOf(
        Argument(
            invoke = { _, _, values ->
                val playerName = values[PLAYER_KEY] as String
                val player = getPlayerByName(playerName)
                if (player == null) return@Argument
                
                player.isAdmin = !player.isAdmin
                updatePlayerAdminState(player)
            },
            tabCompletions = { (_, _, _, _, _) ->
                getAllPlayers().map { it.username }
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