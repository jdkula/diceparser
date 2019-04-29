package pw.jonak.diceparser

import kotlinx.serialization.Serializable
import pw.jonak.diceparser.operations.*
import sun.awt.SunToolkit
import java.lang.Integer.max
import java.security.InvalidParameterException
import java.security.SecureRandom
import java.util.*

class Roll(val n: Int, val s: Int, val modifier: Int, rollingOperations: List<Operation>, finishOperations: List<Operation>, random: Random = Random()) {
    private val _results = ArrayList<Result>()
    val results: List<Result> get() = _results
    private var successes: Int? = null

    init {
        for (i in 1..n) {
            val thisRoll = Result(random.nextInt(s) + 1)
            _results += thisRoll
            rollingOperations.forEach { op -> op.apply(s, listOf(thisRoll))?.let { _results.addAll(it) } }
        }
        finishOperations.forEach { op -> op.apply(s, results)?.let { _results.addAll(it) } }

        var success_count = 0
        var failure_count = 0

        _results.forEachIndexed { i, it ->
            if (it is BooleanResult) {
                if (it.success) success_count += 1
                else failure_count += 1
                _results.removeAt(i)
            }
        }

        if (success_count != 0) {
            successes = max(0, success_count - failure_count)
        }

        if (AscendingSort in finishOperations) {
            _results.sort()
        }
        if (DescendingSort in finishOperations) {
            _results.apply {
                sort()
                reverse()
            }
        }
    }

    fun toInt(): Int {
        return results.fold(0, { acc, res -> acc + if (res.dropped || res is BooleanResult) 0 else res.roll }) + modifier
    }

    override fun toString(): String {
        return results.joinToString(", ") + " + $modifier = ${toInt()}"
    }

    fun toMessage(): RollMessage =
            RollMessage(results, n, s, modifier)

    companion object {

        enum class State {
            N,
            S,
            OPTIONS
        }

        fun parse(str: String): List<Roll> {
            var currentIter = 0
            var i = 0
            val rolls = ArrayList<Roll>()

            val random: Random = SecureRandom()

            while (i < str.length) {
                var n: Int
                var s: Int
                val rollingOperations = ArrayList<Operation>()
                val finishOperations = ArrayList<Operation>()
                var rerollOp: Reroll? = null
                var m: Int = 0

                var start = i
                var end = i
                while (end < str.length) {
                    if (str[end] == 'd') {
                        break
                    }
                    end += 1
                }
                n = str.slice(start until end).toIntOrNull() ?: 1
                if(n > ROLL_MAX_N) throw InvalidParameterException()
                i = end
                start = i + 1
                end = start
                while (end < str.length) {
                    if (!str[end].isDigit()) {
                        break
                    }
                    end += 1
                }
                s = str.slice(start until end).toIntOrNull() ?: 20
                i = end
                // Do Option processing
                option@ while (i < str.length) {
                    if (currentIter > MAX_ITERATION) throw SunToolkit.InfiniteLoop()
                    currentIter += 1
                    when (str[i]) {
                        '=', '>', '<' -> {
                            val (op, adv) = Success.parse(str.substring(i))
                            op?.let { finishOperations.add(it) }
                            i += adv
                        }
                        'f' -> {
                            val (op, adv) = Failure.parse(str.substring(i))
                            op?.let { finishOperations.add(it) }
                            i += adv
                        }
                        '!' -> {
                            val (op, adv) = Exploding.parse(str.substring(i), s, random)
                            op?.let { rollingOperations.add(it) }
                            i += adv
                        }
                        'k' -> {
                            val (op, adv) = Keep.parse(str.substring(i))
                            op?.let { finishOperations.add(it) }
                            i += adv
                        }
                        'd' -> {
                            val (op, adv) = Drop.parse(str.substring(i))
                            op?.let { finishOperations.add(op) }
                            i += adv
                        }
                        'r' -> {
                            val (op, adv) = Reroll.parse(str.substring(i), rerollOp, random)
                            op?.let {
                                rollingOperations.add(it)
                                rerollOp = it
                            }
                            i += adv
                        }
                        's' -> {
                            val (op, adv) = Sort.parse(str.substring(i))
                            finishOperations.add(op)
                            i += adv
                        }
                        '+', '-' -> {
                            if (str[i] == '+' && (str.indexOf('+', i + 1) > str.indexOf('d', i + 1) || (str.indexOf('+', i + 1) == -1) && str.indexOf('d', i + 1) != -1)) {
                                break@option
                            }
                            start = i
                            end = start + 1
                            while (end < str.length) {
                                if (currentIter > MAX_ITERATION) throw SunToolkit.InfiniteLoop()
                                currentIter += 1

                                if (!str[end].isDigit()) {
                                    break
                                }
                                end += 1
                            }
                            m = str.slice(start until end).toInt()
                            i = end
                        }
                        else -> break@option
                    }
                }
                rolls += Roll(n, s, m, rollingOperations.sorted(), finishOperations.sorted())
                i += 1
            }
            return rolls
        }
    }
}

@Serializable
data class RollMessage(val results: List<Result>, val numDice: Int, val numSides: Int, val modifier: Int)
