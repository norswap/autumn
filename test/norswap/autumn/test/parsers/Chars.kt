package norswap.autumn.test.parsers
import norswap.autumn.ExpectedIdentifier
import norswap.autumn.NoString
import norswap.autumn.UnexpectedChar
import norswap.autumn.conf.TAB_SIZE
import norswap.autumn.parsers.*
import norswap.autumn.test.*
import org.testng.annotations.Test

class Chars: EmptyGrammarFixture()
{
    // =============================================================================================

    fun char_failure(input: String) {
        failure_at(input, 0, UnexpectedChar)
    }

    // ---------------------------------------------------------------------------------------------

    fun str_failure(input: String) {
        failure_at(input, 0, NoString("hello"))
    }

    // ---------------------------------------------------------------------------------------------

    fun iden_failure(input: String) {
        failure_at(input, 0, ExpectedIdentifier)
    }

    // =============================================================================================

    @Test fun chars()
    {
        top_fun { g.alpha() }
        success("a")
        success("A")
        char_failure("1")

        top_fun { digit() }
        success("1")
        char_failure("a")

        top_fun { alphanum() }
        success("a")
        success("1")
        char_failure("_")

        top_fun { char_range('a', 'z') }
        success("a")
        char_failure("1")

        top_fun { char_set('a', 'b') }
        success("a")
        success("b")
        char_failure("c")

        top_fun { char_set("ab") }
        success("a")
        success("b")
        char_failure("c")

        top_fun { char_any() }
        success("a")
        success("_")
        char_failure("\u0000")

        top_fun { hex_digit() }
        success("a")
        success("f")
        success("F")
        char_failure("g")
        char_failure("G")
        success("1")

        top_fun { octal_digit() }
        success("0")
        success("7")
        char_failure("8")
        char_failure("a")

        top_fun { space_char() }
        success(" ")
        success("\n")
        char_failure("\u0000")
        char_failure("a")
        TAB_SIZE = 0
        success("\t")
        TAB_SIZE = 4
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun strings()
    {
        top_fun { string("hello") }
        success("hello")
        str_failure("world")
        str_failure(" hello")
        str_failure("")

        top_fun { word("hello") }
        success("hello")
        success("hello  ")
        str_failure("  hello")
        str_failure("world")
        str_failure("")
        str_failure(" ")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun test_ascii_java_iden()
    {
        top_fun { ascii_java_iden() }
        success("hello")
        success("heLLo")
        success("Hello")
        success("hello0")
        success("_hello")
        success("\$hello")
        success("hello_")
        success("hello$")
        success("_0")
        success("__")
        iden_failure("1hello")
        iden_failure("")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun test_java_iden()
    {
        top_fun { java_iden() }
        success("_hello")
        success("\$hello")
        success("hello_")
        success("éèàù")
        iden_failure("1hello")
        iden_failure("")
    }

    // =============================================================================================
}