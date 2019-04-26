package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class LabelledStatement implements Statement
{
    public abstract Identifier label();
    public abstract Statement statement();

    public static LabelledStatement mk (Identifier label, Statement statement) {
        return new AutoValue_LabelledStatement(label, statement);
    }

}
