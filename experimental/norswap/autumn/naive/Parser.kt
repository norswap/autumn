package norswap.autumn.naive
import norswap.autumn.Grammar

abstract class Parser
{
    lateinit var grammar: Grammar
    abstract operator fun invoke(): Boolean
}