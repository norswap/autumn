# Worked Out Example

Below is a full example of what the syntax of a simple (but not trivial) language might look like.  
The source code is also located [here][SimpleGrammar].

[SimpleGrammar]: /src/norswap/lang/examples/simple/

    TODO INCLUDE SOURCE
    
And here is some input that the parser defined by this grammar is able to recognize:

    x = 2;
    y = 3;
    z = (x + y) * (x + y);
    print(z - 4);