package pw.jonak.diceparser.operations

import pw.jonak.diceparser.BooleanResult
import pw.jonak.diceparser.ComparePoint
import pw.jonak.diceparser.Operation
import pw.jonak.diceparser.Result

class Success(private val cp: ComparePoint) : Operation {
    override fun apply(sides: Int, res: List<Result>): List<Result>? {
        val results = ArrayList<Result>()
        res.forEach {
            if (cp(it.roll)) {
                results += BooleanResult(true, it.roll)
            }
        }
        return null
    }

    override val precedence: Int = 6

    companion object {
        fun parse(str: String): Pair<Success?, Int> {
            val cpr = ComparePoint.parse(str)
            return Pair(cpr.first?.let { Success(it) }, cpr.second)
        }
    }
}