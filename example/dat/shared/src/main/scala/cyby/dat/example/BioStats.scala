/**                                                                    **\
**  Copyright (c) 2018-2019 Center for Organic and Medicinal Chemistry  **
**                Zurich University of Applied Sciences                 **
**                Wädenswil, Switzerland                                **
\**                                                                    **/

package cyby
package dat
package example

import shapeless.HNil

@io.circe.generic.JsonCodec case class BioStats(
  sub:   Compound.Cli,
  con:   Container.Cli,
){
  def path: Container.Path = con.id :: sub.id :: HNil

  lazy val stats: Map[Method.Id,Stats] =
     con.bio.groupBy(_.method._1).flatMap(stats)

  private def stats(p: (Method.Id,List[BiodataEntry.Cli])): Map[Method.Id,Stats] = p match {
    case (pth,Nil)    ⇒ Map.empty
    case (pth,h::t)   ⇒ Map(pth -> Stats(Nel(h,t).map(_.value.v)))
  }
}

object BioStats {
  implicit lazy val toMolI: ToMol[BioStats] = ToMol(_.sub.id.v, _.sub.structure.o)
}

