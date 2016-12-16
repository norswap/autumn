# FAQ: Is Autumn scannerless?

Yes, but you can simulate tokenization using a [token grammar]. This actually gives you the benefits
of both worlds: the benefits of tokens (performance, disambiguation) when you want them, the
benefits of scannerless parsing (being able to shift the definition of your "tokens" depending on
the context).

If you have a tool that generates a stream of token, and you would like to match on that, it is
also possible, see the question [Can I feed tokens to Autumn instead of text?][feed-token]

[token grammar]: /doc/tokens.md
[feed-token]: /doc/faq/feed-tokens.md