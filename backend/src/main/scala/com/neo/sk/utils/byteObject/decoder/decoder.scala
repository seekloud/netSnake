package com.neo.sk.utils.byteObject

import com.neo.sk.hiStream.utils.MiddleBuffer
import shapeless.labelled.{FieldType, field}
import shapeless.{:+:, ::, CNil, Coproduct, HList, HNil, Inl, Inr, LabelledGeneric, Lazy, Witness}

import scala.reflect.ClassTag

/**
  * User: Taoz
  * Date: 7/15/2018
  * Time: 8:44 AM
  */
package object decoder {


  trait BytesDecoder[A] {
    def decode(buffer: MiddleBuffer): A
  }

  object BytesDecoder {
    //summoner
    def apply[A](implicit dec: BytesDecoder[A]): BytesDecoder[A] = dec

    //constructor
    def instance[A](func: MiddleBuffer => A): BytesDecoder[A] = {
      new BytesDecoder[A] {
        override def decode(buffer: MiddleBuffer): A = {
          func(buffer)
        }
      }
    }


    implicit def genericDecoder[A, R](
      implicit
      gen: LabelledGeneric.Aux[A, R],
      dec: Lazy[BytesDecoder[R]]
    ): BytesDecoder[A] = {
      instance[A] { buffer =>
        val r = dec.value.decode(buffer)
        gen.from(r)
      }
    }

    implicit def hListDecoder[K <: Symbol, H, T <: HList](
      implicit
      witness: Witness.Aux[K],
      hDecoder: Lazy[BytesDecoder[H]],
      tDecoder: BytesDecoder[T]
    ): BytesDecoder[FieldType[K, H] :: T] = {
      instance { buffer =>
//        val name = witness.value.name
//        val value = witness.value
//        println(s"hListDecoder, process. name=$name value=$value")
        val h = hDecoder.value.decode(buffer)
        val t = tDecoder.decode(buffer)
//        println(s"h:$h, h.getClass=${h.getClass}")
//        println(s"t:$t, t.getClass=${t.getClass}")
        field[K](h) :: t
      }
    }


    trait CoproductTypeBytesDecoder[A] extends BytesDecoder[A] {
      def decodeCoproduct(buffer: MiddleBuffer, nameOption: Option[String]): A

      override def decode(buffer: MiddleBuffer): A = decodeCoproduct(buffer, None)
    }

    implicit def coproductDecoder[K <: Symbol, H, T <: Coproduct](
      implicit
      witness: Witness.Aux[K],
      hDecoder: Lazy[BytesDecoder[H]],
      tDecoder: CoproductTypeBytesDecoder[T]
    ): CoproductTypeBytesDecoder[FieldType[K, H] :+: T] = {
      new CoproductTypeBytesDecoder[FieldType[K, H] :+: T] {
        override def decodeCoproduct(
          buffer: MiddleBuffer,
          nameOption: Option[String]
        ): FieldType[K, H] :+: T = {
          val cName = nameOption match {
            case Some(name) => name
            case None => buffer.getString()
          }
          val nameInWitness = witness.value.name
          val value = witness.value
//          println(s"coproductDecoder, process. nameInWitness=$nameInWitness value=$value, c=$cName")
          if (cName == nameInWitness) {
//            println("in left")
            val h = hDecoder.value.decode(buffer)
//            println(s"coproductDecoder left.h=$h, h.getClass=${h.getClass}")
            Inl(field[K](h))
          } else {
//            println("in right")
            val t = tDecoder.decodeCoproduct(buffer, Some(cName))
//            println(s"coproductDecoder right. t=$t, t.getClass=${t.getClass}")
            Inr(t)
          }
        }
      }
    }


    implicit val hNilInstance = instance[HNil] { buffer =>
        //do nothing.
        HNil
    }

    implicit val cNilInstance = {
      new CoproductTypeBytesDecoder[CNil] {
        override def decodeCoproduct(buffer: MiddleBuffer, nameOption: Option[String]): CNil = {
          throw new Exception("it should never get to cNilInstance.")
        }
      }
    }

    //instance[CNil] { buffer => throw new Exception("it should never get to cNilInstance.") }


    implicit val intInstance = instance[Int](buffer => buffer.getInt())
    implicit val floatInstance = instance[Float](buffer => buffer.getFloat())
    implicit val stringInstance = instance[String](buffer => buffer.getString())
    implicit val booleanInstance = instance[Boolean](buffer => buffer.getByte() == 1.toByte)


    private def readToArray[A](
      buffer: MiddleBuffer, len: Int, dec: BytesDecoder[A]
    )(implicit m: ClassTag[A]): Array[A] = {
/*
      var ls = List.empty[A]
      var c = 0
      while (c < len) {
        ls = dec.decode(buffer) :: ls
        c += 1
      }
      ls.reverse
*/
      val arr = new Array[A](len)
      var c = 0
      while (c < len) {
        arr(c) = dec.decode(buffer)
        c += 1
      }
      arr
    }

    implicit def seqDecoder[A](implicit dec: BytesDecoder[A], m: ClassTag[A]): BytesDecoder[Seq[A]] = {
      instance[Seq[A]] { buffer =>
        val len = buffer.getInt()
        readToArray(buffer, len, dec).toSeq
      }
    }


    implicit def listDecoder[A](implicit dec: BytesDecoder[A], m: ClassTag[A]): BytesDecoder[List[A]] = {
      instance[List[A]] { buffer =>
        val len = buffer.getInt()
        readToArray(buffer, len, dec).toList
      }
    }


    implicit def arrayDecoder[A](implicit dec: BytesDecoder[A], m: ClassTag[A]): BytesDecoder[Array[A]] = {
      instance[Array[A]] { buffer =>
        val len = buffer.getInt()
        readToArray(buffer, len, dec)
      }
    }

    implicit def optionDecoder[A](implicit dec: BytesDecoder[A]): BytesDecoder[Option[A]] = {
      instance[Option[A]] { buffer =>
        val len = buffer.getInt()
        if (len == 1) {
          Some(dec.decode(buffer))
        } else {
          None
        }
      }
    }

    implicit def mapDecoder[K, V](
      implicit
      kDec: BytesDecoder[K],
      vDec: BytesDecoder[V],
      c1: ClassTag[K],
      c2: ClassTag[V]
    ): BytesDecoder[Map[K, V]] = {
      instance{ buffer =>
        val len = buffer.getInt()
        val kArr = new Array[K](len)
        val vArr = new Array[V](len)
        var c = 0
        while (c < len) {
          kArr(c) = kDec.decode(buffer)
          vArr(c) = vDec.decode(buffer)
          c += 1
        }
        kArr.zip(vArr).toMap
      }
    }
  }


}
