// "Wrap with '?.let { ... }' call" "false"
// WITH_STDLIB
// ACTION: Add non-null asserted (!!) call
// ACTION: Do not show return expression hints
// ACTION: Introduce import alias
// ACTION: Introduce local variable
// ACTION: Replace with safe (this?.) call
// ERROR: Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type String?

fun String?.foo() {
    toLowerCase<caret>()
}