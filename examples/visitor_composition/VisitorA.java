package visitor_composition;

public class VisitorA implements _VisitorA
{
    private int storage;

    @Override public int some_storage () {
        return storage;
    }
}
