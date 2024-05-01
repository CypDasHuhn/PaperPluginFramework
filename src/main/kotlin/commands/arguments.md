# Arguments

---

The Argument class is there to register your command tree.

To create a command using the provided Argument API,
follow these steps:

```kotlin
@CustommCommad
val yourCommand = RootArgument()
```
You dont need to actually register this field anywhere,
it will be automatically caught by the Argument API.
To actually have a functioning command, you need to insert
the **constructor parameters**.
<br>
But before that, we need to 