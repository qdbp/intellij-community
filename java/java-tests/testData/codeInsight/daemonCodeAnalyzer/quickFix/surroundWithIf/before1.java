// "Surround with 'if (i != null)'" "true"
class A {
    void foo(int x){
        String i = x > 0 ? "" : null;
        i.has<caret>hCode();
    }
}