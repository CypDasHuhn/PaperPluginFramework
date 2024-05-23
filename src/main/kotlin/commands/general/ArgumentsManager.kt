package commands.general

import commands.general.Completer.returnWithStarting
import io.github.classgraph.ClassGraph
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import java.util.*

val defaultTrue: ArgumentPredicate = { _ -> true }
fun returnString(): ArgumentHandler = { (_, _, str, _, _) -> str }

lateinit var rootArguments: MutableList<RootArgument>
var defaultErrorArgumentsOverflow: (ArgumentInfo) -> Unit =
    { (sender, _, _, _, _) -> sender.sendMessage("Too many Arguments!") }

/** The Argument Class is what's used to model a segment inside a command.
 * Its content can very dynamically, depending on the argument Information.
 * The Argument Class is intended for nesting. When you want to chain arguments, you need to fill the following arguments. */
open class Argument(
    /** Controls whether the current string will be identified as this argument. The default is that it's always set to true.*/
    open var isArgument: ArgumentPredicate = defaultTrue,
    /** Is there too set the Tab Completions for this argument. (Not the ones which follow, this field represents this argument!)*/
    open var tabCompletions: CompletionLambda? = null,
    /** Is there for the Action to follow when this argument is the last and being invoked by the user.
     * You can use the HashMap to access the values registered by the previous arguments which were set by the [argumentHandler].*/
    open var isValidCompleter: ArgumentPredicate? = null,
    open var invoke: InvokeLambda? = null,
    /** This field is there to set a value which can be later corresponded with this argument. It uses the [key]
     * to set a value under that key, which can later be used by [invoke].
     * The default is [returnString] if you don't care about that and want to transform it later. */
    open var argumentHandler: ArgumentHandler = returnString(),
    /** Checks whether the current string is also actually valid. (Logic to determine whether it should be followed/invoked or not)
     * When you have multiple returns, add a Key to later access it in [errorInvalid].*/
    open var isValid: ArgumentPredicateString? = null,
    /** If the [isValid] check fails, this method will be invoked (you can send a message or do other logic here)
     * When you had multiple returns inside the [isValid], you can access the key to customize an action.*/
    open var errorInvalid: ErrorLambdaString? = null,
    /** Determines whether this argument is a modified. Modifier Arguments don't get followed,
     * but rather can be squeezed in between arguments. For example, you can have "/setLanguage EN" or "/setLanguage -global EN".
     * "-global" is a modifier argument here, and EN is a normal argument.
     * Both of them are inside [followingArguments] of "/setLanguage", and modifiers can be only chained before the actual argument.
     * When the modifier wasn't used, it is still saved under the key inside the values, with the value being false (meaning it wasn't accessed)*/
    open var isModifier: Boolean = false,
    /** This function will be invoked when it's a children of following Arguments, but the user didn't invoke any.
     * The program finds the first child registered which has this field filled, and invokes it.
     * (Important, this field only represents itself, not its children!)*/
    open var errorMissing: ErrorLambda? = null,
    /** This is the field that is used to chain commands.*/
    open var followingArguments: List<Argument>? = null,
    open var errorArgumentOverflow: ((ArgumentInfo) -> Unit)? = null,
    /** This is the key that you can later access the values stored with the [argumentHandler] inside [invoke] by.
     * You probably want to save this under a variable, else you can easily do stumblers by key mismatches. */
    open var key: String,
)

/** An Argument class which is at the start of an argument tree. It's [key] is always "label"! */
class RootArgument(
    /** Is there for the Action to follow when this argument is the last and being invoked by the user.
     * You can use the HashMap to access the values registered by the previous arguments which were set by the [argumentHandler].*/
    override var invoke: InvokeLambda? = null,
    /** This field is there to set a value which can be later corresponded with this argument. It uses the [key]
     * to set a value under that key, which can later be used by [invoke].
     * The default is [returnString] if you don't care about that and want to transform it later. */
    override var argumentHandler: ArgumentHandler = returnString(),
    /** Checks whether the current string is also actually valid. (Logic to determine whether it should be followed/invoked or not)*/
    override var isValid: ArgumentPredicateString? = null,
    /** If the [isValid] check fails, this method will be invoked (you can send a message or do other logic here)*/
    override var errorInvalid: ErrorLambdaString? = null,
    /** This is the field that is used to chain commands.*/
    override var followingArguments: List<Argument>? = null,
    /** The labels are strings that the user can use to start a command. ("/<label>"*/
    var labels: List<String>,
    /** This is a function that will be invoked once the system processes the command. use this to create
     * cached objects, or whatever else calculations you would do repeatedly else. Return false if you want
     * to early return the entire command processing, like if the sender type is wrong, or there are no permissions for this command.*/
    var startingUnit: ((sender: CommandSender) -> Boolean)? = null,
    override var errorArgumentOverflow: ((ArgumentInfo) -> Unit)? = null
) : Argument(
    defaultTrue,
    null,
    null,
    invoke,
    argumentHandler,
    isValid,
    errorInvalid,
    false,
    null,
    followingArguments,
    errorArgumentOverflow,
    "label",
)

/** A sort of constructor which takes down the boilerplate of creating a simple Modifier Argument. */
fun simpleModifierArgument(
    commandName: String,
    isArgument: ArgumentPredicate = { (_, _, arg, _, _) -> arg == commandName },
    isValidCompleter: ArgumentPredicate? = null,
    isValid: ArgumentPredicateString? = null,
    errorInvalid: ErrorLambdaString? = null,
    key: String,
): Argument {
    return Argument(
        isArgument = isArgument,
        isModifier = true,
        tabCompletions = { (_, _, arg, _, _) -> listOf(commandName).returnWithStarting(arg) },
        isValidCompleter = isValidCompleter,
        argumentHandler = { (_, _, arg, _, _) -> arg == commandName },
        isValid = isValid,
        errorInvalid = errorInvalid,
        key = key,
    )
}

typealias InvokeLambda = (CommandSender, Array<String>, HashMap<String, Any>) -> Unit

typealias ArgumentPredicate = (ArgumentInfo) -> Boolean
typealias ArgumentPredicateString = (ArgumentInfo) -> Pair<Boolean, String?>
typealias CompletionLambda = (ArgumentInfo) -> List<String>
typealias ErrorLambda = (ArgumentInfo) -> Unit
typealias ErrorLambdaString = (ArgumentInfo, String) -> Unit
typealias ArgumentHandler = (ArgumentInfo) -> Any

/** The Argument Info Class is a model as to what the argument currently can interact with. */
data class ArgumentInfo(
    val sender: CommandSender,
    val args: Array<String>,
    val label: String,
    val index: Int,
    val values: HashMap<String, Any>
)


/** Finds the first argument that contains an errorMissing lambda, and invokes it. */
fun List<Argument>.invokeMissingArg(argInfo: ArgumentInfo) {
    this.first { arg: Argument -> arg.errorMissing != null }.errorMissing!!(argInfo)
}

/** Returns the first argument which matches the isArgument predicate.
 * If none could be found, it invokes the missingError lambda and returns null. */
fun List<Argument>.getArgument(argInfo: ArgumentInfo): Argument? {
    return this.firstOrNull { arg -> arg.isArgument(argInfo) }.also {
        if (it == null) this.invokeMissingArg(argInfo)
    }
}

/** Takes in the general arguments for a Command or TabCompleter, and also the parameter [function] that will be called to execute at the end, also returning a value.
 * This method is the wrapper for going through the argument tree, which is used by Command and TabCompleter. */
fun goThroughArguments(
    sender: CommandSender, _command: Command, label: String, args: Array<String>,
    isTabCompleter: Boolean,
): List<String>? {
    val commandArgs =
        args.toMutableList().also { it.add(0, label) }.toTypedArray() // prepend the label before the arguments

    var arguments: MutableList<Argument> =
        rootArguments.filter { it.labels.contains(label) } as MutableList<Argument>

    var errorArgumentOverflow: ((ArgumentInfo) -> Unit)? = null
    arguments.first().errorArgumentOverflow?.let {
        errorArgumentOverflow = it
    }

    LinkedList<Argument>()

    if (arguments.first() is RootArgument && (arguments.first() as RootArgument).startingUnit != null) { // invoke the starting unit (if existing)
        (arguments.first() as RootArgument).startingUnit!!(sender)
    }

    val values: HashMap<String, Any> = HashMap()

    for ((i, commandArg) in commandArgs.withIndex()) {
        val argInfo = ArgumentInfo(sender, commandArgs, commandArg, i, values)

        val currentArgument = arguments.getArgument(argInfo) // found argument
            ?: return null

        currentArgument.errorArgumentOverflow?.let {
            errorArgumentOverflow = it
        }

        if (!isTabCompleter) currentArgument.isValid?.let {  // error handling
            val (isValid, key) = it(argInfo)
            if (!isValid) {
                currentArgument.errorInvalid!!(argInfo, key ?: "")
                return null
            } else {
                values[currentArgument.key] = currentArgument.argumentHandler(argInfo) // fill in values
            }
        }


        arguments.filter { arg -> arg.isModifier }
            .forEach { arg -> values.putIfAbsent(arg.key, false) } // set every modifier to false, if absent

        val lastElement = i + 1 == commandArgs.size
        if (!lastElement) {
            if (currentArgument.isModifier) {
                arguments = arguments.filter { it != currentArgument }.toMutableList()
            } else if (currentArgument.followingArguments != null) {
                arguments = currentArgument.followingArguments as MutableList<Argument>
            } else {
                if (!isTabCompleter) {
                    if (errorArgumentOverflow == null)
                        defaultErrorArgumentsOverflow(argInfo)
                    else
                        errorArgumentOverflow!!(argInfo)
                }
                return ArrayList()
            }
            continue
        }

        // only invoked if last element

        fun invokeMissingArg() {
            val inferiorArguments = when (currentArgument.isModifier) {
                true -> arguments.also { arguments = arguments.filter { a -> a != currentArgument }.toMutableList() }
                false -> currentArgument.followingArguments
            }

            inferiorArguments!!.invokeMissingArg(argInfo)
        }

        if (isTabCompleter) {
            return when (arguments.any { it.tabCompletions != null }) {
                true -> arguments
                    .filter { it.isValidCompleter == null || it.isValidCompleter!!(argInfo) }
                    .flatMap { it.tabCompletions!!(argInfo) }

                false -> ArrayList<String>().also { invokeMissingArg() }
            }
        } else {
            currentArgument.invoke?.let { it(sender, commandArgs, values) }
                ?: invokeMissingArg()
        }
    }
    return null
}

/**  ChatGPT generated Code.
 * Returns every instance of Argument which is annotated with @Argument. */
fun getCommands(): List<RootArgument> {
    val commands = mutableListOf<RootArgument>()
    ClassGraph()
        .enableAllInfo()
        .scan().use { scanResult ->
            scanResult
                .getClassesWithFieldAnnotation(CustomCommand::class.qualifiedName)
                .forEach { classInfo ->
                    classInfo.fieldInfo.forEach { fieldInfo ->
                        val field = Class.forName(classInfo.name).getDeclaredField(fieldInfo.name)
                        field.isAccessible = true
                        val fieldValue = field.get(null)
                        if (fieldValue is RootArgument) {
                            commands.add(fieldValue)
                        }
                    }
                }
        }
    return commands
}

/** Using this annotation on a field of the class [RootArgument],
 * makes that field being registered as a Command.  */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class CustomCommand

/** Returns all labels from the commands which were registered. */
fun getLabels(): List<String> = getCommands().flatMap { it.labels }
