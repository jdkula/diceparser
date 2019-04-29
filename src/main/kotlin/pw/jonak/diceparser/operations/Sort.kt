package pw.jonak.diceparser.operations

import pw.jonak.diceparser.Operation
import pw.jonak.diceparser.Result

open class Sort : Operation {
    override fun apply(sides: Int, res: List<Result>): List<Result>? = null
    override val precedence: Int = 8

    companion object {
        fun parse(str: String): Pair<Sort, Int> {
            if(str.length == 1) {
                return Pair(AscendingSort, 1)
            }
            if(str[1] == 'd') {
                return Pair(DescendingSort, 2)
            }
            if(str[1] == 'a') {
                return Pair(AscendingSort, 2)
            }
            return Pair(AscendingSort, 1)
        }
    }
}

object AscendingSort : Sort() {
}

object DescendingSort : Sort() {
}