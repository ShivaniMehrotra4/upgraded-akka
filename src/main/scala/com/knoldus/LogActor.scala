package com.knoldus

import akka.actor.Actor
import akka.pattern.pipe
import scala.concurrent.Future
import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * The class LogActor extends Actor functionality.
 */
class LogActor extends Actor {
  var numOfErrors = 0
  var numOfWarnings = 0
  var numOfInfo = 0

  override def receive: Receive = {
    case fileName(file) =>
      val fSource = Source.fromFile(s"$file")
      val listOfLines = fSource.getLines().toList
      finder(listOfLines)

    case "shivani" => val temp = CountItems(numOfErrors, numOfWarnings, numOfInfo)
      futureWrapper(temp).pipeTo(context.sender())
  }

  /**
   * finder function finds Errors,Warnings and Information from each line of each file
   * @param listOfLines - a list of Lines from input file
   * @return - an object of CountItems case class containing counts of Errors,Warnings and Information
   */
  def finder(listOfLines: List[String]): CountItems = {
    listOfLines match {
      case Nil => CountItems(numOfErrors, numOfWarnings, numOfInfo)
      case head :: rest if head.contains("[ERROR]") => numOfErrors += 1; finder(rest)
      case head :: rest if head.contains("[WARN]") => numOfWarnings += 1; finder(rest)
      case head :: rest if head.contains("[INFO]") => numOfInfo += 1; finder(rest)
      case _ :: rest => finder(rest)
    }
  }

  /**
   * futureWrapper function wraps the parameter object of case class in future
   * @param temp - object of case class CountItems
   * @return - Future[case-class-object]
   */
  def futureWrapper(temp: CountItems): Future[CountItems] = Future {
    temp
  }

}
