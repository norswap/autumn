package norswap.autumn.parsers;

import norswap.autumn.Grammar;
import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.utils.data.wrappers.Slot;
import norswap.utils.multimap.MultiMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

import static norswap.utils.Strings.joinArray;

/**
 * An optimized parser for a choice between a series of literal string (i.e. something like
 * {@code choice("a", "ab", "bc")}). Used notably as part of the reserved words / keyword system
 * (see /doc/A7-reserved-words-and-identifiers.md).
 *
 * <p>There are no builders for this parser in {@link Grammar} as it is rarely useful outside of the
 * reserved word system: we usually want to do something (like build an AST node) depending
 * on which string we match.
 *
 * <p>The implementation currently uses a trie.
 */
public class StringChoice extends Parser
{
    private static class TrieNode
    {
        /**  The code point followed to arrive at this node (0 for the root). */
        private final int inbound;

        /** If all the children share a prefix, the prefix, otherwise null. */
        private final int[] lead;

        /** Children of the node, one character ahead. */
        private final TrieNode[] children;

        /** Whether there is a value entirely matched by arriving at this node. */
        private final boolean value;

        private TrieNode(int i, Collection<int[]> strings)
        {
            int[] first = strings.iterator().next();
            this.inbound = i == 0 ? (char) 0 : first[i - 1];

            // character -> { strings starting with this character }
            // 0 = there is a value entirely matched by this node
            MultiMap<Integer, int[]> map = multiMap(i, strings);

            // build up the common prefix
            int[] lead = new int[0];
            while (map.size() == 1) {
                int k = map.keySet().iterator().next();
                if (k == 0) break;
                lead = Arrays.copyOf(lead, lead.length + 1);
                lead[lead.length - 1] = k;
                map = multiMap(++i, map.get(k));
            }
            this.lead = lead.length == 0 ? null : lead;

            // build up the children array
            ArrayList<TrieNode> children = new ArrayList<>();
            final int j = i;
            final Slot<Boolean> value = new Slot<>(false);
            map.forEach((k, v) -> {
                if (k == 0)
                    value.x = true;
                else
                    children.add(new TrieNode(j + 1, v));
            });

            this.children = children.toArray(new TrieNode[0]);
            this.value = value.x;
        }

        private MultiMap<Integer, int[]> multiMap (int i, Collection<int[]> strings)
        {
            return strings.stream().collect(MultiMap.collector(
                str -> i == str.length ? 0 : str[i],
                Function.identity()));
        }

        /** Returns the child matching the given codepoint, or null if there isn't any. */
        private TrieNode get (int c)
        {
            for (TrieNode child: children)
                if (child.inbound == c)
                    return child;
            return null;
        }
    }

    private final TrieNode trie;
    public final String[] strings;

    public StringChoice (String... strings)
    {
        this.strings = strings;
        Collection<int[]> codepoints =
            Arrays.stream(strings).map(s -> s.codePoints().toArray()).collect(Collectors.toList());
        this.trie = new TrieNode(0, codepoints);
    }

    @Override protected boolean doparse (Parse parse)
    {
        int pos0 = parse.pos;
        int furthestMatch = -1;
        TrieNode node = this.trie;
        int i = 0;
        while (node != null) {
            if (node.lead != null) {
                if (!parse.match(pos0 + i, node.lead)) break;
                i += node.lead.length;
            }
            if (node.value)
                furthestMatch = i;
            node = node.get(parse.charAt(pos0 + i++));
        }
        if (furthestMatch >= 0) {
            parse.pos = pos0 + furthestMatch;
            return true;
        }
        return false;
    }

    @Override public Iterable<Parser> children () {
        return Collections.emptyList();
    }

    @Override public String toStringFull ()
    {
        StringBuilder b = new StringBuilder("stringChoice(");
        joinArray(b, ", ", strings);
        b.append(")");
        return b.toString();
    }

    /*
    // Alternate implementation.
    // In my tests, this is slower, but only marginally so (Java benchmark: 8.8s vs 9.3s)
    // The complexity & memory footprint is however much less.
    // Keeping this around, as depending on the technology, this might wound up faster at some point.

    int[][] strings;

    public StringChoice(String... strings) {
        this.strings = Arrays.stream(strings).map(s -> s.codePoints().toArray()).toArray(int[][]::new);
    }

    @Override protected boolean doparse (Parse parse)
    {
        for (int[] string: strings) {
            if (parse.match(parse.pos, string)) {
                parse.pos += string.length;
                return true;
            }
        }
        return false;
    }
    */
}
