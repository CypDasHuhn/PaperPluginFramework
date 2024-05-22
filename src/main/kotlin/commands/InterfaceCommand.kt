package commands

import commands.general.CustomCommand
import commands.general.RootArgument
import interfaces.TestInterface
import interfaces.TestInterfaceContext
import interfaces.general.openTargetInterface
import org.bukkit.entity.Player

@CustomCommand
val interfaceCommand = RootArgument(
    invoke = { sender, _, _ ->
        openTargetInterface(
            sender as Player,
            TestInterface.interfaceName,
            TestInterfaceContext(0, "test")
        )
    },
    labels = listOf("interface")
)