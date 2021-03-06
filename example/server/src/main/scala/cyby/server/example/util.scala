/**                                                                    **\
**  Copyright (c) 2018-2019 Center for Organic and Medicinal Chemistry  **
**                Zurich University of Applied Sciences                 **
**                Wädenswil, Switzerland                                **
\**                                                                    **/

package cyby
package server
package example

import cats.data.Validated.fromEither, cats.implicits._
import cyby.dat.{Mol ⇒ _, _}, example.{NotFound ⇒ _, _}
import org.http4s.dsl.io.{Unauthorized ⇒ _, _}

/**
  * Mostly trivial utility functions used at different
  * locations in the server code.
  */
trait util {
  def isAdmin(u: UserS.Srv): Boolean            = cyby.dat.example.isAdmin(u.level.v)

  def isUser(u: UserS.Srv): Boolean             = cyby.dat.example.isUser(u.level.v)

  def isSuperUser(u: UserS.Srv): Boolean        = cyby.dat.example.isSuperUser(u.level.v)

  def isGuest(u: UserS.Srv): Boolean            = cyby.dat.example.isGuest(u.level.v)

  def actual(u: UserS.Srv)(id: User.Id): Boolean = u.id === id

  def isOwner(u: UserS.Srv, p: ProjectS.Srv): Boolean = p.owner.v === u.id

  def hasAccess(u: UserS.Srv, p: ProjectS.Srv): Boolean =
    isOwner(u,p) || p.users.v.exists(_ === u.id)

  def asAdmin(u: UserS.Srv): List[Err] = must(isAdmin(u))(Unauthorized)

  def asUser(u: UserS.Srv): List[Err] = must(isUser(u))(Unauthorized)

  def asSuperUser(u: UserS.Srv): List[Err] = must(isSuperUser(u))(Unauthorized)

  def asAdmin(u: UserLevel): List[Err] = must(cyby.dat.example.isAdmin(u))(Unauthorized)

  def asUser(u: UserLevel): List[Err] = must(cyby.dat.example.isUser(u))(Unauthorized)

  def asSuperUser(u: UserLevel): List[Err] = must(cyby.dat.example.isSuperUser(u))(Unauthorized)

  def asOwner(u: UserS.Srv, p: ProjectS.Srv): List[Err] =
    must(isOwner(u,p))(Unauthorized)

  def accessiblePros(u: UserS.Srv, ps: ProjectS.DB): Set[Project.Id] =
    if (isAdmin(u)) ps.keySet
    else ps.filter{ case (_,p) ⇒ hasAccess(u,p) }.keySet

  def asmblD[A,B](implicit D: DerivedAssemble[St,Err,A,B]): DerivedAssemble[St,Err,A,B] = D
 
  def dbAsmbl[I,A,B](srt: List[B] ⇒ List[B])
    (implicit A: DerivedAssemble[St,Err,A,B]): Asmbl[Map[I,A],List[B]] =
    Assemble.inst((s,db) ⇒ db.toList.traverse{ case (_,a) ⇒ A.run(s,a) }.map(srt))
 
  def asmblLink[A,B](find: (St,A) ⇒ DataE[B]): Asmbl[A,B] =
    Assemble.inst((st:St,a:A) ⇒ fromEither(find(st,a)))
 
  def subPth(p: Compound.Path): String = s"${p.head}"
 
  def subFilPth(p: Compound.FilPath): String = s"${subPth(p.tail)}-${p.head}"

  def conPth(p: Container.Path): String = s"${subPth(p.tail)}-${p.head}"
 
  def conFilPth(p: Container.FilPath): String = s"${conPth(p.tail)}-${p.head}"

  def bioPth(p: BiodataEntry.Path): String = s"${conPth(p.tail)}-${p.head}"
 
  def bioFilPth(p: BiodataEntry.FilPath): String = s"${bioPth(p.tail)}-${p.head}"

  lazy val notFound: IO[Response] = NotFound()

  implicit lazy val supLA: AsmblLink[Supplier.Id] = asmblLink(SupplierS.link)

  implicit lazy val useLA: AsmblLink[User.Id] = asmblLink(UserS.link)

  implicit lazy val proLA: AsmblLink[Project.AccId] = asmblLink(ProjectS.link)

  implicit lazy val metLA: AsmblLink[Method.Id] = asmblLink(MethodS.link)

  implicit lazy val stoLA: AsmblLink[Location.Id] = asmblLink(LocationS.link)

  def adjEditInfo(s: St, ei: EditInfo): EditInfo = ei match {
    case EditInfo(t,i,_) ⇒ EditInfo(t, i, UserS.link(s,Id(i)).map(_._2).toOption)
  }

  implicit lazy val editAsmbl: Asmbl[EditInfo,EditInfo] = Assemble.inst{
    (s,ei) ⇒ valid(adjEditInfo(s,ei))
  }

  def authEnv(u: UserS.Srv, st: St): AuthEnv = HasAccess.AuthEnv(
    u.id, u.level, accessiblePros(u, st.pros)
  )
}

// vim: set ts=2 sw=2 et:

