package norswap.lang.java.ast;

import java.util.List;

public class ClassType implements TType
{
    public final List<ClassTypePart> parts;

    public ClassType (List<ClassTypePart> parts) {
        this.parts = parts;
    }
}
