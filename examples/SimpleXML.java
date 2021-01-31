import norswap.autumn.Grammar;
import norswap.autumn.Parse;
import norswap.autumn.ParseState;
import norswap.autumn.Parser;
import norswap.autumn.parsers.AbstractWrapper;
import norswap.autumn.positions.Span;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines a simple XML language with nested tags and text, but no attributes.
 *
 * <p>This grammar showcases the definition of context-sensitive features in Autumn: the parser
 * check that each closing tag is match with a corresponding open tag, and emits an error when
 * that is not the case.
 */
public final class SimpleXML extends Grammar
{
    public static final class Tag {
        public final List<?> contents;
        public Tag (List<?> contents) {
            this.contents = contents;
        }
    }

    private final ParseState<ArrayDeque<String>> tag_stack
        = new ParseState<>(SimpleXML.class, ArrayDeque::new);

    public final class CloseTag extends AbstractWrapper
    {
        public CloseTag (Parser identifier) {
            super("close_tag", identifier);
        }

        @Override protected boolean doparse (Parse parse)
        {
            int pos0 = parse.pos;
            if (!child.parse(parse))
                return false;

            String close_tag = new Span(pos0, parse.pos).get(parse.string);
            ArrayDeque<String> tstack = tag_stack.data(parse);
            String open_tag = tstack.peek();

            if (open_tag == null) {
                parse.setErrorMessage(
                    "Closing tag without corresponding opening tag: </" + close_tag + ">");
                return false;
            }

            if (!close_tag.equals(open_tag)) {
                parse.setErrorMessage(
                    "Mismatched opening and closing tag: <" + open_tag + "> and </" + close_tag + ">");
                return false;
            }

            tstack.pop();
            return true;
        }
    }

    public rule identifier =
        seq(alpha, alphanum.at_least(0));

    public rule open_identifier =
        identifier
        .collect($ -> $.apply(() -> {
            $.data(tag_stack).push($.str());
            return () -> $.data(tag_stack).pop();
        }));

    public rule close_identifier =
        rule(new CloseTag(identifier.getParser()));

//    // Alternative: inlining CloseTag
//    public rule close_identifier2 =
//        seq(identifier.collect().push_string_match(), context(p ->
//        {
//            String close_tag = $(p.stack.peek());
//            String open_tag = tag_stack.data(p).peek();
//
//            if (open_tag == null) {
//                p.set_error_message(
//                    "Closing tag without corresponding opening tag: </" + close_tag + ">");
//                return false;
//            }
//
//            if (!close_tag.equals(open_tag)) {
//                p.set_error_message(
//                    "Mismatched opening and closing tag: <" + open_tag + "> and </" + close_tag + ">");
//                return false;
//            }
//
//            tag_stack.data(p).pop();
//            return true;
//        }));

    public rule open_tag = seq("<", open_identifier, ">");
    public rule close_tag = seq("</", close_identifier, ">");

    public rule text =
        cpred(c -> c != '<').at_least(1)
        .push($ ->
            // remove leading and trailing whitespace from every line
            Arrays.stream($.str().split("\n"))
                .map(String::trim)
                .collect(Collectors.joining("\n")));

    public rule contents =
        choice(lazy(() -> this.tag), text).at_least(0);

    public rule tag =
        seq(open_tag, contents, close_tag)
        .push($ -> new Tag($.$list()));

    @Override public rule root() {
        return tag;
    }
}
