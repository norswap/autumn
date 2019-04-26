package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.List;

@AutoValue
public abstract class MarkerAnnotation implements TAnnotation
{
    public abstract List<Identifier> name();

    public static MarkerAnnotation mk (List<Identifier> name) {
        return new AutoValue_MarkerAnnotation(name);
    }

    public static MarkerAnnotation strings (String... names)
    {
        List<Identifier> idens = new ArrayList<>(names.length);
        for (String name: names) idens.add(Identifier.mk(name));
        return new AutoValue_MarkerAnnotation(idens);
    }
}