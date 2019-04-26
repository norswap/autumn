package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

import static norswap.utils.Util.cast;

@AutoValue
public abstract class EnumConstant implements Declaration
{
    public abstract List<TAnnotation> annotations();
    public abstract Identifier name();
    public abstract @Nullable List<Expression> args();
    public abstract @Nullable List<Declaration> body();

    private List<EnumConstant> enum_constants;

    public List<EnumConstant> enum_constants()
    {
        if (enum_constants != null)
            return enum_constants;

        int constant_end_index = 0;

        for (Declaration dec: body())
            if (dec instanceof EnumConstant)
                ++ constant_end_index;
            else break;

        return enum_constants = cast(body().subList(0, constant_end_index));
    }

    public static EnumConstant mk (
        List<TAnnotation> annotations, Identifier name,
        @Nullable List<Expression> args, @Nullable List<Declaration> body)
    {
        return new AutoValue_EnumConstant(annotations, name, args, body);
    }
}
