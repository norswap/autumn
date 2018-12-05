package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class InitBlock implements Declaration
{
    public abstract boolean is_static();
    public abstract Block block();

    public static InitBlock mk (boolean is_static, Block block) {
        return new AutoValue_InitBlock(is_static, block);
    }
}
