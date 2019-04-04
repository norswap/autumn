package visitor_composition;

public class VisitorB implements _VisitorB
{
    private int storage;

    @Override public int some_storage () {
        return storage;
    }
}
