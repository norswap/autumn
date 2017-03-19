package norswap.uranium
import norswap.utils.thread_local

/**
 * Implicit context that allows passing the reactor implicitly
 */
object ReactorContext
{
    var reactor: Reactor by thread_local.late_init<Reactor>()
}