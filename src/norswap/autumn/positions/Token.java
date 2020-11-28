package norswap.autumn.positions;

public interface Token
{
    /**
     * Start offset of the token in the character stream that underlies the token stream.
     */
    int start();

    /**
     * End offset of the token in the character stream that underlies the token stream.
     * This should be the offset of the last character spanned by the token + 1.
     */
    int end();

    /**
     * Length of the tokens in characters, always equal to {@code end() - start()}.
     */
    default int length() {
        return end() - start();
    }
}
