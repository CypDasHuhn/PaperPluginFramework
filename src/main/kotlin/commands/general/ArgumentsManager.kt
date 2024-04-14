package commands.general

import commands.general.Completer.returnWithStarting
import io.github.classgraph.ClassGraph
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import java.util.*

val defaultTrue: ArgumentPredicate = { _ -> true }
fun returnString(): ArgumentHandler = { (_, _, str, _, _) -> str }

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
    /** This is the key that you can later access the values stored with the [argumentHandler] inside [invoke] by.
     * You probably want to save this under a variable, else you can easily do stumblers by key mismatches. */
    open var key: String,
) {
    fun isValidTest(argInfo: ArgumentInfo): Boolean {
        if (isValid != null) {
            val (isValid, errorKey) = isValid!!(argInfo)

            if (!isValid) {
                errorInvalid!!(argInfo, errorKey ?: "")
                return false
            }
        }
        return true
    }
}

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
    var labels: List<String>
) : Argument(
    defaultTrue,
    null,
    invoke,
    argumentHandler,
    isValid,
    errorInvalid,
    false,
    null,
    followingArguments,
    "label"
)

/** A sort of constructor which takes down the boilerplate of creating a simple Modifier Argument. */
fun simpleModifierArgument(
    commandName: String,
    isValid: ArgumentPredicateString,
    errorInvalid: ErrorLambdaString,
    key: String,
): Argument {
    return Argument(
        isArgument = { (_, _, arg, _, _) -> arg == commandName },
        isModifier = true,
        tabCompletions = { (_, _, arg, _, _) -> listOf(commandName).returnWithStarting(arg) },
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
    val firstArgument: Optional<Argument> = this.stream()
        .filter { arg: Argument -> arg.errorMissing != null }
        .findFirst()

    firstArgument.get().errorMissing!!(argInfo)
}

/** Returns the first argument which matches the isArgument predicate.
 * If none could be found, it invokes the missingError lambda and returns null. */
fun List<Argument>.getArgument(argInfo: ArgumentInfo): Argument? {
    val optionalArg = this.stream()
        .filter { arg -> arg.isArgument(argInfo) }
        .findFirst()

    if (!optionalArg.isPresent) {
        this.invokeMissingArg(argInfo)
        return null
    }

    return optionalArg.get()
}

/** Takes in the general arguments for a Command or TabCompleter, and also the parameter [function] that will be called to execute at the end, also returning a value.
 * This method is the wrapper for going through the argument tree, which is used by Command and TabCompleter. */
fun <T> goThroughArguments(
    sender: CommandSender, command: Command, label: String, args: Array<String>,
    function: (Argument, ArgumentInfo, MutableList<Argument>) -> T
): T? {
    val _argList = args.toMutableList()
    _argList.add(0, label)
    val argList = _argList.toTypedArray()

    var arguments: MutableList<Argument> = getCommands().toMutableList()

    val values: HashMap<String, Any> = HashMap()
    for (i in argList.indices) {
        val currentArg = argList[i]
        val argInfo = ArgumentInfo(sender, argList, currentArg, i, values)

        val currentArgument = arguments.getArgument(argInfo) ?: return null

        val isValid = currentArgument.isValidTest(argInfo)
        if (!isValid) return null

        values[currentArgument.key] = currentArgument.argumentHandler(argInfo)

        arguments.stream().filter { a -> a.isModifier }.forEach { a -> values.putIfAbsent(a.key, false) }

        if (i + 1 != argList.size) {
            if (currentArgument.isModifier) {
                arguments.remove(currentArgument)
            } else {
                arguments = currentArgument.followingArguments as MutableList<Argument>
            }
            continue
        }

        return function(currentArgument, argInfo, arguments)
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

