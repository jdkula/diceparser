package pw.jonak.diceparser.operations

import pw.jonak.diceparser.ComparePoint
import pw.jonak.diceparser.Operation
import pw.jonak.diceparser.Result
import java.util.*
import kotlin.collections.ArrayList

class Penetrating(private val cp: ComparePoint, private val random: Random = Random()) : Operation {
    override fun apply(sides: Int, res: List<Result>): List<Result>? {
        tailrec fun helper(res: List<Result>, acc: ArrayList<Result> = ArrayList()): List<Result> {
            if(res.isEmpty()) return acc
            val result = ArrayList<Result>()
            res.forEach {
                if(cp(it.roll)) {
                    result += Result(random.nextInt(sides))
                }
            }
            acc.addAll(result)
            return helper(result, acc)
        }

        return helper(res)
    }

    override val precedence: Int = 2
}