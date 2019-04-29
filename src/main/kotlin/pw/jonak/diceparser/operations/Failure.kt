package pw.jonak.diceparser.operations

import pw.jonak.diceparser.BooleanResult
import pw.jonak.diceparser.ComparePoint
import pw.jonak.diceparser.Operation
import pw.jonak.diceparser.Result

class Failure(private val cp: ComparePoint) : Operation {
    override fun apply(sides: Int, res: List<Result>): List<Result>? {
        res.forEach {
            if (it is BooleanResult && cp(it.roll)) {
                it.success = false
            }
        }
        return null
    }

    override val precedence: Int = 7

    companion object {
        fun parse(str: String): Pair<Failure?, Int> {
            val p = ComparePoint.parse(str)
            return Pair(p.first?.let { Failure(it) }, p.second)
        }
    }
}