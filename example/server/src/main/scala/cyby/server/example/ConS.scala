/**                                                                    **\
**  Copyright (c) 2018-2019 Center for Organic and Medicinal Chemistry  **
**                Zurich University of Applied Sciences                 **
**                Wädenswil, Switzerland                                **
\**                                                                    **/

package cyby
package server
package example

import cats.implicits.{none ⇒ _, _}
import cyby.dat._, cyby.dat.example._

/**
  * CRUD of containers
  */
object ConS extends ChildEditor {
  //----------------------------------------------------------------------
  //                         Types
  //----------------------------------------------------------------------
  
  type Id            = Con.Id
  type ZCli          = Con.Cli
  type Add           = Con[Pure,Undef,Sto.Id,Sup.Id,Pro.Id,Undef,Undef,Undef,Undef]
  type Mod           = Con[Option,Undef,Sto.Id,Sup.Id,Pro.Id,Undef,Undef,Undef,Undef]
  type Srv           = Con[Pure,Id,Sto.Id,Sup.Id,Pro.Id,BioS.DB,ConFilS.DB,TimeStamp,EditInfo]
  type SrvAdd        = Con[Pure,Id,Sto.Id,Sup.Id,Pro.Id,Undef,Undef,TimeStamp,EditInfo]
  type SrvMod        = Con[Option,Undef,Sto.Id,Sup.Id,Pro.Id,Undef,Undef,Undef,EditInfo]
  type Acc           = Con[Pure,Id,Sto.Id,Sup.Id,Pro.AccId,BioS.AccDB,ConFilS.AccDB,TimeStamp,EditInfo]

  //----------------------------------------------------------------------
  //                         Util
  //----------------------------------------------------------------------
  
  val parent     = SubS
  val notFound   = p ⇒ PathNotFound(ConP(p))

  val dbL        = lens[SubS.Srv].containers
  val getId      = _.id

  def envs(ee: Env, edSt: EdSt) ={
    val e  = ee loggedInEnv getSt(edSt)
    val pp = edSt.path
    val cs = edSt.node.containers
    val ae = List(edSt.node.project.v) -> e.ae
    Envs(ae,ae,e.lvl,pp -> cs,pp -> cs,(),e.ei -> cs.keySet,e.ei,e.u)
  }

  implicit lazy val filA: Asmbl[ConFilS.AccDB,List[Fil.Cli]] = ConFilS.asmbl
  implicit lazy val bioA: Asmbl[BioS.AccDB,List[Bio.Cli]] = BioS.asmbl
  lazy val asmbl = dbAsmbl[Id,Acc,Con.Cli](_.sortBy(_.location.v._2))

  //----------------------------------------------------------------------
  //                         Valdation
  //----------------------------------------------------------------------

  val exists = (p: Sub.Path) ⇒ (i: Con.Id,n: Plain) ⇒ BatchExists(n, i::p)
  val bo   = (b: Plain) ⇒ if (b.v.nonEmpty) some(b) else none[Plain]
  val bsrv = (s: Srv) ⇒ s.batch map bo

  val valid = ValidatorImpl[(Sub.Path,DB),(Sub.Path,DB),Unit](
    (p,u)   ⇒ uniqAddO(p._2, u.batch map bo)(bsrv)(exists(p._1)),
    (p,o,u) ⇒ uniqModO(p._2, o.id, u.batch map bo)(bsrv)(exists(p._1)),
    (_,_,_) ⇒ Nil,
  )

  //----------------------------------------------------------------------
  //                         Authorization
  //----------------------------------------------------------------------
  
  val au = ProAuth(lens[Add].project, lens[Srv].project, lens[Mod].project)
  lazy val auth = au.auth

  def acc(ae: AuthEnv, s: Srv) = ae.accPro(s.project) map (
    ps ⇒ s.copy(project = ps, files = ConFilS.accDB(ae, s.files), bio = BioS.accDB(ae, s.bio))
  )

  //----------------------------------------------------------------------
  //                         Editing
  //----------------------------------------------------------------------
  
  val mo = DerivedModifier[Srv,SrvMod]
  
  val cud = CUDImpl[(EditInfo,Set[Id]),EditInfo](
    a ⇒ {case (e,i) ⇒ a.copy(id = nextId(i), created  = e.timestamp, modified = e)},
    a ⇒ a.copy(bio = Map(), files = Map()),
    (ei,m) ⇒ m.copy(modified = ei),
    mo.apply
  )
}
