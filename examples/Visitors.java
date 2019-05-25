public final class Visitors
{
    // ---------------------------------------------------------------------------------------------
    // 1. Initial setup.

    interface Visitor {
        void visit (A object);
        void visit (B object);
    }

    interface Base {
        void accept (Visitor visitor);
    }

    static class A implements Base {
        @Override public void accept (Visitor visitor) {
            visitor.visit(this); // calls visit(A)
        }
    }

    static class B implements Base {
        @Override public void accept (Visitor visitor) {
            visitor.visit(this); // calls visit(B)
        }
    }

    // ---------------------------------------------------------------------------------------------
    // 2. Adding a visitor.

    interface _PrintVisitor extends Visitor
    {
        @Override default void visit (A object) {
            System.out.println("printing an A");
        }
        @Override default void visit (B object) {
            System.out.println("printing a B");
        }
    }

    static class PrintVisitor implements  _PrintVisitor {}

    // ---------------------------------------------------------------------------------------------
    // 3. Adding two *independent* data types C and D.

    interface VisitorC extends Visitor {
        void visit (C object);
    }

    static class C implements Base {
        @Override public void accept (Visitor visitor) {
            ((VisitorC) visitor).visit(this);
        }
    }

    interface _PrintVisitorC extends _PrintVisitor, VisitorC {
        @Override default void visit (C object) {
            System.out.println("printing a C");
        }
    }

    static class PrintVisitorC implements _PrintVisitorC {}

    interface VisitorD extends Visitor {
        void visit (D object);
    }

    static class D implements Base {
        @Override public void accept (Visitor visitor) {
            ((VisitorD) visitor).visit(this);
        }
    }

    interface _PrintVisitorD extends _PrintVisitor, VisitorD {
        @Override default void visit (D object) {
            System.out.println("printing a D");
        }
    }

    static class PrintVisitorD implements _PrintVisitorD {}

    // ---------------------------------------------------------------------------------------------
    // 4. Composing independent data types C & D.

    interface _PrintVisitorCD extends _PrintVisitorC, _PrintVisitorD {}

    static class PrintVisitorCD implements _PrintVisitorCD {}

    // ---------------------------------------------------------------------------------------------
    // 5. Adding a new visitor that manipulate states.
    //    Still assumes C & D where developed independently.

    // 5.1. Initial

    interface _AddRankVisitor extends Visitor
    {
        int base();
        int result();
        void set_result (int result);

        @Override default void visit (A object) { set_result(base() + 1); }
        @Override default void visit (B object) { set_result(base() + 2); }
    }

    static class AddRankVisitor implements _AddRankVisitor
    {
        private final int base;
        private int result;

        AddRankVisitor (int base) { this.base = base; }
        @Override public int base () { return base; }
        @Override public int result () { return result; }
        @Override public void set_result (int result) { this.result = result; }
    }

    // 5.2. C & D extensions

    interface _Add_RankVisitorC extends _AddRankVisitor, VisitorC {
        @Override default void visit (C object) { set_result(base() + 3); }
    }

    static class AddRankVisitorC extends AddRankVisitor implements _Add_RankVisitorC {
        AddRankVisitorC (int base) { super(base); }
    }

    interface _Add_RankVisitorD extends _AddRankVisitor, VisitorD {
        @Override default void visit (D object) { set_result(base() + 4); }
    }

    static class AddRankVisitorD extends AddRankVisitor implements _Add_RankVisitorD {
        AddRankVisitorD (int base) { super(base); }
    }

    // 5.3 Composing C & D

    interface _Add_RankVisitorCD extends _Add_RankVisitorC, _Add_RankVisitorD {}

    static class AddRankVisitorCD extends AddRankVisitor implements _Add_RankVisitorCD {
        AddRankVisitorCD (int base) { super(base); }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Example: using the rank visitor that handles A, B, C and D.
     */
    public static int add_rank (int base, Base v)
    {
        AddRankVisitor visitor = new AddRankVisitorCD(base);
        v.accept(visitor);
        return visitor.result();
    }

    // ---------------------------------------------------------------------------------------------
}
