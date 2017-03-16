package norswap.whimsy

/**
 * Lightweight exception (no stack trace) meant to be thrown inside reactions and caught by
 * the reactor, which extracts [error].
 */
class ReactorException (val error: ReactorError): Throwable(error.msg, null, false, false)