package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class Block implements Statement
{
    public abstract List<Statement> statements();

    public static Block mk (List<Statement> statements) {
        return new AutoValue_Block(statements);
    }
}
