// "Create local variable 'foo'" "true"
// ACTION: Convert property getter to initializer
// ACTION: Convert to block body
// ACTION: Create local variable 'foo'
// ACTION: Create property 'foo'
// ACTION: Create property 'foo' as constructor parameter
// ACTION: Do not show return expression hints
// ACTION: Rename reference

class A {
    val t: Int get() = <caret>foo
}