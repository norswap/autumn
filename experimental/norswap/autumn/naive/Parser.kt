package norswap.autumn.naive
import norswap.autumn.Grammar

abstract class Parser: () -> Boolean
{
    lateinit var grammar: Grammar
}