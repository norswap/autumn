package norswap.autumn;

public abstract class Parser
{
    // ---------------------------------------------------------------------------------------------

    private String rule;

    // ---------------------------------------------------------------------------------------------

    /**
     * The name of the rule this parser is assigned to, if any, or null.
     */
    public final String rule() {
        return rule;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Sets the name of the rule this parser is assigned to.
     * This may be called at most once, or an error will occur.
     */
    public final void set_rule (String rule)
    {
        if (this.rule != null)
            throw new Error("rule name already set");
        this.rule = rule;
    }

    // ---------------------------------------------------------------------------------------------

    protected abstract boolean doparse (Parse parse);

    // ---------------------------------------------------------------------------------------------

    public final boolean parse (Parse parse)
    {
        int pos0 = parse.pos;
        int log0 = parse.log.size();
        boolean success = doparse(parse);
        if (success) return true;
        parse.pos = pos0;
        if (parse.error < pos0)
            parse.error = pos0;
        parse.rollback(log0);
        return false;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the rule name, if any, or the full string representation of this parser, as per
     * {@link #toStringFull()}.
     */
    @Override public final String toString()
    {
        return rule != null
            ? rule
            : toStringFull();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the full string representation of this parser (i.e. not only its rule name).
     * The ouput may however reference sub-parsers by rule name.
     */
    public abstract String toStringFull();

    // ---------------------------------------------------------------------------------------------
}
