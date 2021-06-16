@file:Suppress("unused")

package biser.stub

enum class StubE { A, B }

abstract class StubA(val v: Int)

class StubA1(v: Int, val s: Long) : StubA(v)

abstract class StubA2(v: Int, val s: String) : StubA(v)
class StubA2s(v: Int, s: String) : StubA2(v, s)

open class StubB(val v: String)
class StubB1(v: String, val s: Boolean) : StubB(v)
open class StubB2(v: String, val s: Byte) : StubB(v)
class StubB2s(v: String, s: Byte) : StubB2(v, s)

class StubC {
	sealed class Sealed(val v: String) {
		class A(v: String) : Sealed(v)
		
		object B : Sealed("b")
	}
}

object StubAo1: StubA(1)
object StubAo2: StubA(2)

class StubM(
	val vSealed: StubC.Sealed,
	
	val vBoolean: Boolean,
	val vByte: Byte,
	val vInt: Int,
	val vLong: Long,
	val vDouble: Double,
	
	val vBooleanArray: BooleanArray,
	val vByteArray: ByteArray,
	val vIntArray: IntArray,
	val vLongArray: LongArray,
	val vDoubleArray: DoubleArray,
	
	val vString: String,
	
	val vStubA: StubA,
	val vStubA1: StubA1,
	val vStubA2: StubA2,
	val vStubANullable: StubA?,
	val vStubA1Nullable: StubA1?,
	val vStubA2Nullable: StubA2?,
	
	val vStubB: StubB,
	val vStubB1: StubB1,
	val vStubB2: StubB2,
	val vStubBNullable: StubB?,
	val vStubB1Nullable: StubB1?,
	val vStubB2Nullable: StubB2?,
	
	val vStubE: StubE,
	
	val lBoolean: List<Boolean>,
	val lByte: List<Byte>,
	val lInt: List<Int>,
	val lLong: List<Long>,
	val lDouble: List<Double>,
	
	val lBooleanArray: List<BooleanArray>,
	val lByteArray: List<ByteArray>,
	val lIntArray: List<IntArray>,
	val lLongArray: List<LongArray>,
	val lDoubleArray: List<DoubleArray>,
	
	val lString: List<String>,
	
	val lStubA: List<StubA>,
	val lStubB: List<StubB>,
	val lStubE: List<StubE>,
	
	val llBoolean: List<List<Boolean>>,
	val llByte: List<List<Byte>>,
	val llInt: List<List<Int>>,
	val llLong: List<List<Long>>,
	val llDouble: List<List<Double>>,
	
	val llBooleanArray: List<List<BooleanArray>>,
	val llByteArray: List<List<ByteArray>>,
	val llIntArray: List<List<IntArray>>,
	val llLongArray: List<List<LongArray>>,
	val llDoubleArray: List<List<DoubleArray>>,
	
	val llString: List<List<String>>,
	
	val llStubA: List<List<StubA>>,
	val llStubB: List<List<StubB>>,
	val llStubE: List<List<StubE>>
)
