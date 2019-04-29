package pw.jonak.diceparser

import kotlinx.serialization.Serializable

@Serializable
open class Result(var roll: Int, var dropped: Boolean = false, var rerollable: Boolean = true) : Comparable<Result> {
    override operator fun compareTo(other: Result): Int {
        return roll.compareTo(other.roll)
    }

    override fun toString(): String {
        return "[${if(dropped) "/" else ""}$roll]"
    }
}

class BooleanResult(var success: Boolean, roll: Int) : Result(roll, false)