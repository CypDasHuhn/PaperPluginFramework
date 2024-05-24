package commands

import frame.commands.CustomCommand
import frame.commands.RootArgument
import frame.database.getOrDefaultContext
import frame.`interface`.openTargetInterface
import interfaces.TestInterface
import interfaces.TestInterfaceContext
import org.bukkit.entity.Player

@CustomCommand
val interfaceCommand = RootArgument(
    invoke = { sender, _, _ ->
        openTargetInterface(
            sender as Player,
            TestInterface.interfaceName,
            getOrDefaultContext(sender, TestInterface.interfaceName) { TestInterfaceContext(0, "test") }
        )
    },
    labels = listOf("interface")
)