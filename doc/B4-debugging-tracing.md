# B4. Debugging & Tracing a Parse

Here are a few easy ways to debug and/or improve the performance of your parser:

- Write unit tests using [`TestFixture`]. See [an example] for the [Java grammar].
  
- Make sure assertions are enabled by passing the `-ea` argument to the Java virtual machine (`java`
command).
  
- If you are using IntelliJ IDEA, make sure to define the environment variable
 `AUTUMN_USE_CHAR_COLUMN` for more accurate hyperlinked file locations. The same applies if your
 editor supports hyperlinked file locations with columns expressed as a character offset (tabs count
 for 1) instead of width (tabs go to next multiple of the tab size).
  
- **!!!** Don't forget to disable call stack recording in production with
  `ParseOptions.recordCallStack(false).get()`

- Specify that the parse should be traced via the options:
```
ParseOptions options = ParseOptions.trace(true).get();
ParseResult result = Autumn.parse(grammar, input, options);
if (!result.fullMatch)
    //
else
    System.out.println(options.metrics);
```

- Use a `PEEK_ONLY` collect parser to print or set a breakpoint during the parse:
```
public rule myParser = seq(a, b, whatever)
    // add this line:
    .collect($ -> System.out.println("N was here!"), PEEK_ONLY); 
```

[`TestFixture`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/TestFixture.html
[an example]: /test/lang/java/TestGrammar.java
[Java grammar]: /examples/norswap/lang/java/JavaGrammar.java

<!-- TODO: discuss a list of common performance pitfalls -->