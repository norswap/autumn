import norswap.autumn.positions.LineMapString;
import norswap.autumn.positions.Position;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public final class TestLineMapString
{
    // ---------------------------------------------------------------------------------------------

    private static void assert_throws (Class<? extends Exception> klass, Runnable f)
    {
        boolean caught = false;
        try {
            f.run();
        }
        catch (Exception e) {
            caught = true;
            assertTrue(klass.isAssignableFrom(e.getClass()));
        }
        finally {
            assertTrue(caught);
        }
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void test_line_positions()
    {
        LineMapString map1 = new LineMapString("\na\nbc\t\n\ndef\n");
        int[] expected_lines1 = {0, 1, 3, 7, 8, 12};
        assertEquals(map1.line_positions, expected_lines1);

        LineMapString map2 = new LineMapString("\na\nbc\t\n\ndef\n", 17, 3); // bogus params shouldn't matter
        int[] expected_lines2 = {0, 1, 3, 7, 8, 12};
        assertEquals(map2.line_positions, expected_lines2);

        LineMapString map3 = new LineMapString("");
        int[] expected_lines3 = {0};
        assertEquals(map3.line_positions, expected_lines3);

        LineMapString map4 = new LineMapString("\n");
        int[] expected_lines4 = {0, 1};
        assertEquals(map4.line_positions, expected_lines4);

        LineMapString map5 = new LineMapString("a");
        int[] expected_lines5 = {0};
        assertEquals(map5.line_positions, expected_lines5);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void test_line_from()
    {
        LineMapString map1 = new LineMapString("\na\nbc\t\n\ndef\n");
        assertEquals(map1.line_from(0),  1);
        assertEquals(map1.line_from(1),  2);
        assertEquals(map1.line_from(2),  2);
        assertEquals(map1.line_from(3),  3);
        assertEquals(map1.line_from(4),  3);
        assertEquals(map1.line_from(5),  3);
        assertEquals(map1.line_from(6),  3);
        assertEquals(map1.line_from(7),  4);
        assertEquals(map1.line_from(8),  5);
        assertEquals(map1.line_from(9),  5);
        assertEquals(map1.line_from(10), 5);
        assertEquals(map1.line_from(11), 5);
        assertEquals(map1.line_from(12), 6);

        assert_throws(IndexOutOfBoundsException.class,
            () -> map1.line_from(-1));
        assert_throws(IndexOutOfBoundsException.class,
            () -> map1.line_from(13));

        LineMapString map2 = new LineMapString("\na\nbc\t\n\ndef\n", 17, 3); // bogus params shouldn't matter
        assertEquals(map2.line_from(0),  1);
        assertEquals(map2.line_from(1),  2);
        assertEquals(map2.line_from(2),  2);
        assertEquals(map2.line_from(3),  3);
        assertEquals(map2.line_from(4),  3);
        assertEquals(map2.line_from(5),  3);
        assertEquals(map2.line_from(6),  3);
        assertEquals(map2.line_from(7),  4);
        assertEquals(map2.line_from(8),  5);
        assertEquals(map2.line_from(9),  5);
        assertEquals(map2.line_from(10), 5);
        assertEquals(map2.line_from(11), 5);
        assertEquals(map2.line_from(12), 6);

        LineMapString map3 = new LineMapString("");
        assertEquals(map3.line_from(0), 1);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void test_column_from()
    {
        LineMapString map1 = new LineMapString("\na\nbc\t\n\ndef\n");
        assertEquals(map1.column_from(0),  1);
        assertEquals(map1.column_from(1),  1);
        assertEquals(map1.column_from(2),  2);
        assertEquals(map1.column_from(3),  1);
        assertEquals(map1.column_from(4),  2);
        assertEquals(map1.column_from(5),  3);
        assertEquals(map1.column_from(6),  5); // tab jump
        assertEquals(map1.column_from(7),  1);
        assertEquals(map1.column_from(8),  1);
        assertEquals(map1.column_from(9),  2);
        assertEquals(map1.column_from(10), 3);
        assertEquals(map1.column_from(11), 4);
        assertEquals(map1.column_from(12), 1);

        assert_throws(IndexOutOfBoundsException.class,
            () -> map1.column_from(-1));
        assert_throws(IndexOutOfBoundsException.class,
            () -> map1.column_from(13));

        LineMapString map2 = new LineMapString("\na\nbc\t\n\ndef\n", 5, 0);
        assertEquals(map2.column_from(3),  0);
        assertEquals(map2.column_from(4),  1);
        assertEquals(map2.column_from(5),  2);
        assertEquals(map2.column_from(6),  5); // tab jump

        LineMapString map3 = new LineMapString("\t\nab\t\t\n\ta");
        assertEquals(map3.column_from(0),  1);
        assertEquals(map3.column_from(1),  5);
        assertEquals(map3.column_from(2),  1);
        assertEquals(map3.column_from(3),  2);
        assertEquals(map3.column_from(4),  3);
        assertEquals(map3.column_from(5),  5);
        assertEquals(map3.column_from(6),  9);
        assertEquals(map3.column_from(7),  1);
        assertEquals(map3.column_from(8),  5);
        assertEquals(map3.column_from(9),  6);

        LineMapString map4 = new LineMapString("");
        assertEquals(map4.column_from(0), 1);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void test_position_from()
    {
        LineMapString map1 = new LineMapString("\na\nbc\t\n\ndef\n");
        assertEquals(map1.position_from(0),  new Position(1, 1));
        assertEquals(map1.position_from(1),  new Position(2, 1));
        assertEquals(map1.position_from(2),  new Position(2, 2));
        assertEquals(map1.position_from(3),  new Position(3, 1));
        assertEquals(map1.position_from(4),  new Position(3, 2));
        assertEquals(map1.position_from(5),  new Position(3, 3));
        assertEquals(map1.position_from(6),  new Position(3, 5)); // tab jump
        assertEquals(map1.position_from(7),  new Position(4, 1));
        assertEquals(map1.position_from(8),  new Position(5, 1));
        assertEquals(map1.position_from(9),  new Position(5, 2));
        assertEquals(map1.position_from(10), new Position(5, 3));
        assertEquals(map1.position_from(11), new Position(5, 4));
        assertEquals(map1.position_from(12), new Position(6, 1));

        assert_throws(IndexOutOfBoundsException.class,
            () -> map1.position_from(-1));
        assert_throws(IndexOutOfBoundsException.class,
            () -> map1.position_from(13));

        LineMapString map2 = new LineMapString("\na\nbc\t\n\ndef\n", 5, 0);
        assertEquals(map2.position_from(3),  new Position(3, 0));
        assertEquals(map2.position_from(4),  new Position(3, 1));
        assertEquals(map2.position_from(5),  new Position(3, 2));
        assertEquals(map2.position_from(6),  new Position(3, 5)); // tab jump

        LineMapString map3 = new LineMapString("\t\nab\t\t\n\ta");
        assertEquals(map3.position_from(0),  new Position(1, 1));
        assertEquals(map3.position_from(1),  new Position(1, 5));
        assertEquals(map3.position_from(2),  new Position(2, 1));
        assertEquals(map3.position_from(3),  new Position(2, 2));
        assertEquals(map3.position_from(4),  new Position(2, 3));
        assertEquals(map3.position_from(5),  new Position(2, 5));
        assertEquals(map3.position_from(6),  new Position(2, 9));
        assertEquals(map3.position_from(7),  new Position(3, 1));
        assertEquals(map3.position_from(8),  new Position(3, 5));
        assertEquals(map3.position_from(9),  new Position(3, 6));

        LineMapString map4 = new LineMapString("");
        assertEquals(map4.position_from(0), new Position(1, 1));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void test_offset_from()
    {
        LineMapString map1 = new LineMapString("\na\nbc\t\n\ndef\n");
        assertEquals(map1.offset_from(new Position(1, 1)),  0);
        assertEquals(map1.offset_from(new Position(2, 1)),  1);
        assertEquals(map1.offset_from(new Position(2, 2)),  2);
        assertEquals(map1.offset_from(new Position(3, 1)),  3);
        assertEquals(map1.offset_from(new Position(3, 2)),  4);
        assertEquals(map1.offset_from(new Position(3, 3)),  5);
        assertEquals(map1.offset_from(new Position(3, 5)),  6); // tab jump
        assertEquals(map1.offset_from(new Position(4, 1)),  7);
        assertEquals(map1.offset_from(new Position(5, 1)),  8);
        assertEquals(map1.offset_from(new Position(5, 2)),  9);
        assertEquals(map1.offset_from(new Position(5, 3)), 10);
        assertEquals(map1.offset_from(new Position(5, 4)), 11);
        assertEquals(map1.offset_from(new Position(6, 1)), 12);

        assert_throws(IndexOutOfBoundsException.class,
            () -> map1.offset_from(new Position(-1, 1)));
        assert_throws(IndexOutOfBoundsException.class,
            () -> map1.offset_from(new Position(7, 1)));
        assert_throws(IndexOutOfBoundsException.class,
            () -> map1.offset_from(new Position(1, 0)));
        assert_throws(IndexOutOfBoundsException.class,
            () -> map1.offset_from(new Position(1, 2)));

        LineMapString map2 = new LineMapString("\na\nbc\t\n\ndef\n", 5, 0);
        assertEquals(map2.offset_from(new Position(3, 0)), 3);
        assertEquals(map2.offset_from(new Position(3, 1)), 4);
        assertEquals(map2.offset_from(new Position(3, 2)), 5);
        assertEquals(map2.offset_from(new Position(3, 5)), 6); // tab jump

        LineMapString map3 = new LineMapString("\t\nab\t\t\n\ta");
        assertEquals(map3.offset_from(new Position(1, 1)), 0);
        assertEquals(map3.offset_from(new Position(1, 5)), 1);
        assertEquals(map3.offset_from(new Position(2, 1)), 2);
        assertEquals(map3.offset_from(new Position(2, 2)), 3);
        assertEquals(map3.offset_from(new Position(2, 3)), 4);
        assertEquals(map3.offset_from(new Position(2, 5)), 5);
        assertEquals(map3.offset_from(new Position(2, 9)), 6);
        assertEquals(map3.offset_from(new Position(3, 1)), 7);
        assertEquals(map3.offset_from(new Position(3, 5)), 8);
        assertEquals(map3.offset_from(new Position(3, 6)), 9);

        assert_throws(IllegalArgumentException.class,
            () -> map3.offset_from(new Position(1, 2)));
        assert_throws(IllegalArgumentException.class,
            () -> map3.offset_from(new Position(2, 6)));

        LineMapString map4 = new LineMapString("");
        assertEquals(map4.offset_from(new Position(1, 1)), 0);
    }

    // ---------------------------------------------------------------------------------------------
}
