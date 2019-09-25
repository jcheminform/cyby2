/**                                                                    **\
**  Copyright (c) 2018-2019 Center for Organic and Medicinal Chemistry  **
**                Zurich University of Applied Sciences                 **
**                Wädenswil, Switzerland                                **
\**                                                                    **/

package cyby
package server
package example

import cyby.dat.example.{SubTreeEd, Project}

/**
  * Some utility type aliases
  */
trait types {
  type Err = cyby.dat.example.DataErr

  type ProAuthEnv = (List[Project.Id], AuthEnv)

  type SubTree = SubTreeEd[BiodataEntryS.Ed,ContainerS.Ed,SubFilS.Ed,SubS.Ed]

  type SubTreeL = SubTreeEd[BiodataEntryS.LoadEd,ContainerS.LoadEd,SubFilS.LoadEd,SubS.LoadEd]

  type StEnv = HQ[St,shapeless.HNil,shapeless.HNil]

  type Asmbl[A,B]   = Assemble[St,Err,A,B]

  type AsmblLink[A] = Asmbl[A,cyby.dat.Link[A]]

  type DataE[A] = ErrNel[Err,A]

  type DataV[A] = ValNel[Err,A]
}

// vim: set ts=2 sw=2 et:

