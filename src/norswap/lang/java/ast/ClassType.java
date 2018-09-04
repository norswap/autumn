package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class ClassType implements TType
{
    public abstract List<ClassTypePart> parts();

    public static ClassType make (List<ClassTypePart> parts) {
        return new AutoValue_ClassType(parts);
    }
}
