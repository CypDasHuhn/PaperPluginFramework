# Arguments

---

The Argument class is there to register your command tree.

To create a command using the provided Argument API,
follow these steps:

```kotlin
@CustommCommad
val yourCommand = RootArgument( ... )
```
You dont need to actually register this field anywhere,
it will be automatically caught by the Argument API.
To actually have a functioning command, you need to insert
the **constructor parameters**. <br>
But before that, we need to to clarify the difference between
**Argument** and **RootArgument**.<br>

---

**Argument** is the general class, which **RootArgument** extends. <br>
To create a command, you need to have a **RootArgument** at top, followed by regular **Argument**'s. <br>
But how do we create an argument tree? <br>
The **Argument** class has a constructor parameter called **followingArguments**. <br>
Populate this field with a list of **Argument** instances, and you created a Tree. <br>
```kotlin
@CustommCommad
val yourCommand = RootArgument(
    follwingArguments = listOf(
        Argument(
            ...
        ),
        Argument(
            ...
        ),
        Argument(
            followingArguments = listOf(
                ...
            )
        )
    ),
    ...
)
```
Back to the difference between **RootArgument** and **Argument**. <br>
We will talk about all the fields that an **Argument** class can contain, 
but first we will note which Parameters only **RootArgument** has. <br>
The **RootArgument** class contains the **labels** paramater, which is a List of Strings. These are the initial strings which this argument is registered for, <br>
meaning this part: ``/<label> ...``. <be>
This is the only parameter that you absolutely need to define, no matter what. <br>
Another parameter which is unique to **RootArgument** is **startingUnit**.
This is a lambda-parameter you can optionally fill to create a starting Behavior.
This can be used for caching, or whatever else you'd like to do. 
It's type is ``(CommandSender) -> Unit``.
---
Now I will cover all the fields which the **Argument** class contains. <br>


| Parameter | Description                                                                                                                                                                                                  |
|-----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Invoke    | The invoke parameter is a lambda there to define the effect when sender ends the command with this current argument. <br/> The Type is ``(CommandSender, Array<String>, HashMap<String, Any>) -> Unit``<br/> |
