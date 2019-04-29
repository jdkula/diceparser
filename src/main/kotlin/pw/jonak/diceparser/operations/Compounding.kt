package pw.jonak.diceparser.operations

import pw.jonak.diceparser.ComparePoint
import pw.jonak.diceparser.Operation
import pw.jonak.diceparser.Result
import java.util.*

class Compounding(private val cp: ComparePoint, private val random: Random = Random()) : Operation {
    override fun apply(sides: Int, res: List<Result>): List<Result>? {
        res.forEach {
            if(cp(it.roll)) {
                do {
                    val lastRoll: Int = random.nextInt(sides) + 1
                    it.roll += lastRoll
                } while(cp(lastRoll))
            }
        }
        return null
    }

    override val precedence: Int = 1
}