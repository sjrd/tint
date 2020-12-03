package tint

import utest._
import scala.scalajs.js
import org.scalajs.ir.Trees._
import org.scalajs.ir.Names._
import org.scalajs.ir.Types._
import org.scalajs.ir.Position.NoPosition
import org.scalajs.ir.OriginalName.NoOriginalName

object ExecutorTests extends TestSuite{
  implicit val position = NoPosition
  implicit val env = Env.empty
  val x = LocalName("x")

  val tests = Tests {
    val executor = new Executor(Map())
    
    test("block execution returns last expression") {
      executor.eval(Block(List(StringLiteral("LOL")))) ==> "LOL"
    }

    test("block execution returns undefined when last expression is unit") {
      val envX = env.bind(x, 10)
      executor.eval(Block(List(
        Assign(VarRef(LocalIdent(x))(IntType), IntLiteral(0))
      )))(envX) ==> js.undefined
      envX.read(x) ==> 0
    }

    test("If then else basic operation") {
      val exp = If(
        BinaryOp(BinaryOp.===, VarRef(LocalIdent(x))(IntType), IntLiteral(0)),
        IntLiteral(1),
        IntLiteral(0)
      )(IntType)
      executor.eval(exp)(env.bind(x, 0)) ==> 1
      executor.eval(exp)(env.bind(x, 1)) ==> 0
    }

    test("Boxing of Chars") {
      val exp = UnaryOp(UnaryOp.CharToInt, CharLiteral('A'))
      executor.eval(exp) ==> 65
    }

    test("Array ops") {
      val ident = LocalIdent(LocalName("array"))
      val arrTypeRef = ArrayTypeRef(IntRef, 2)
      val exp = Block(List(
        VarDef(
          ident,
          NoOriginalName,
          ArrayType(arrTypeRef),
          false,
          NewArray(arrTypeRef, List(IntLiteral(1), IntLiteral(2)))
        ),
        Assign(
          ArraySelect(
            ArraySelect(
              VarRef(ident)(ArrayType(arrTypeRef)), IntLiteral(0)
            )(ArrayType(arrTypeRef)), // This TPE might be incompatible, but shouldn't spoil interpretation
            IntLiteral(1)
          )(IntType),
          IntLiteral(10)
        ),
        VarRef(ident)(ArrayType(arrTypeRef))
      ))
      val instance = executor.eval(exp).asInstanceOf[ArrayInstance]
      instance(0).asInstanceOf[ArrayInstance](1) ==> 10
    }
  }
}
