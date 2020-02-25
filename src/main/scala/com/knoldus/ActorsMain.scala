package com.knoldus

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import akka.routing.RoundRobinPool

object ActorsMain extends App {

  val path = "/home/knoldus/Documents/Assignment-Akka/akka-actors-log-assignment/src/main/resources/SampleFolderLogs"
  val rd = new ReadDirectory
  val listOfFiles = rd.getListOfFile(path).map(_.toString)

  val actorSystem = ActorSystem("First-Actor-System")
  val listOfActorRef = getReferenceOfActors(listOfFiles, List())
  val x = futureListCountItems(listOfActorRef, List())
  val futureFinalValue = Future.sequence(x).map(an => an.foldLeft(CountItems(0, 0, 0)) { (acc, y) => caseClassMembersAddition(acc, y) })
  val finalResult = Await.result(futureFinalValue, 1 second)
  println(finalResult)

  /**
   * getReferenceOfActors function basically calls the actors for each individual files.
   * @param listOfFiles - list of file names in a directory
   * @param listOfActorRef - list of ActorRef (references to an actor)
   * @return - a list of ActorRef
   */
  @scala.annotation.tailrec
  def getReferenceOfActors(listOfFiles: List[String], listOfActorRef: List[ActorRef]): List[ActorRef] = {
    listOfFiles match {
      case Nil => listOfActorRef
      case head :: rest =>
        val myActor = actorSystem.actorOf((RoundRobinPool(5)).props(Props[LogActor]).withDispatcher("fixed-thread-pool"))
        myActor ! fileName(head)
        getReferenceOfActors(rest, myActor :: listOfActorRef)

    }
  }

  /**
   * futureList function returns a list that contains all case class objects with future wrapper.
   * @param value - a list of actor references.
   * @param futureLst - a list containing futures of case class objects (initially empty).
   * @return - list of future of case class objects
   */
  @scala.annotation.tailrec
  def futureListCountItems(value: List[ActorRef], futureLst: List[Future[CountItems]]): List[Future[CountItems]] = {
    implicit val timeout: Timeout = Timeout(5 second)
    value match {
      case Nil => futureLst
      case head :: rest =>
        val temp = (head ? "shivani").mapTo[CountItems]
        futureListCountItems(rest, temp :: futureLst)
    }
  }

  /**
   * caseClassMembersAddition function performs addition of member's values on two case class objects
   * @param acc - first case class object
   * @param y - second case class object
   * @return - case class object after addition
   */
  def caseClassMembersAddition(acc: CountItems, y: CountItems): CountItems = {
    CountItems(acc.countError + y.countError, acc.countWarnings + y.countWarnings, acc.countInfo + y.countInfo)
  }


}
