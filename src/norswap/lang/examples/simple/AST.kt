package norswap.lang.examples.simple
import norswap.whimsy.Node

abstract class Binary (val left: Any, val right: Any): Node()

class Product          (left: Any, right: Any): Binary(left, right)
class Division         (left: Any, right: Any): Binary(left, right)
class Remainder        (left: Any, right: Any): Binary(left, right)
class Sum              (left: Any, right: Any): Binary(left, right)
class Diff             (left: Any, right: Any): Binary(left, right)

class Assignment       (val variable: String, val expr: Any)
class Print            (val expr: Any)