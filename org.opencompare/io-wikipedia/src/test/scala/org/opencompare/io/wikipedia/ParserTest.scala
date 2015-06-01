package org.opencompare.io.wikipedia

import java.io.{File, FileWriter}
import java.util.concurrent.Executors

import org.opencompare.api.java.impl.io.{KMFJSONExporter, KMFJSONLoader}
import org.opencompare.io.wikipedia.export.{PCMModelExporter, WikiTextExporter}
import org.opencompare.io.wikipedia.pcm.Page
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.xml.PrettyPrinter

class ParserTest extends FlatSpec with Matchers with BeforeAndAfterAll {
  
  val executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(20))
  val miner = new WikipediaPageMiner

  override def beforeAll() {

    new File("input/").mkdirs()
    new File("output/csv/").mkdirs()
    new File("output/html/").mkdirs()
    new File("output/dump/").mkdirs()
    new File("output/model/").mkdirs()
    new File("output/wikitext/").mkdirs()
  }

  def parsePCMFromFile(file : String) : Page= {
    val reader= Source.fromFile(file)
    val code = reader.mkString
    reader.close
    val preprocessedCode = miner.preprocess(code)
    miner.parse(preprocessedCode, file)
  }
  
  def parseFromTitle(title : String) : Page = {
    val code = miner.getPageCodeFromWikipedia(title)
    val preprocessedCode = miner.preprocess(code)
    miner.parse(preprocessedCode, title)
  }
  
  def parseFromOfflineCode(title : String) : Page = {
    val code = Source.fromFile("input/" + title.replaceAll(" ", "_") + ".txt").getLines.mkString("\n")
    miner.parse(code, title)
  }
  
  def testArticle(title : String) : Page = {
    val pcm = parseFromOfflineCode(title)
    writeToHTML(title, pcm)
    dumpCellsInFile(title, pcm)
    writeToCSV(title, pcm)
    writeToPCM(title, pcm)
    writeToWikiText(title, pcm)
    pcm
  }
  
  def writeToHTML(title : String, pcm : Page) {
    val writer = new FileWriter("output/html/" + title.replaceAll(" ", "_") + ".html")
    writer.write((new PrettyPrinter(80,2)).format(pcm.toHTML))
    writer.close()
  }
  
  def dumpCellsInFile(title : String, pcm : Page) {
    val writer = new FileWriter("output/dump/" + title.replaceAll(" ", "_") + ".txt")
    for(matrix <- pcm.getMatrices; 
    row <- 0 until matrix.getNumberOfRows; 
    column <- 0 until matrix.getNumberOfColumns) {
      val cell = matrix.getCell(row, column)
      if (cell.isDefined) {
        val content = cell.get.content
        val words = for (word <- content.split("\\s") if !word.isEmpty()) yield word
        val formattedContent = words.mkString("", " ", "").toLowerCase()
        writer.write(formattedContent + "\n")
      }
    }
    writer.close()
  }
  
  def writeToCSV(title : String, pcm : Page) {
    val writer = new FileWriter("output/csv/" + title.replaceAll(" ", "_") + ".csv")
    writer.write(pcm.toCSV)
    writer.close()
  }

  def writeToPCM(title : String, page : Page) {
    val exporter = new PCMModelExporter
    val pcms = exporter.export(page)
//    val serializer = new PCMtoHTML
//    writer.write(serializer.toHTML(pcm))
    val serializer = new KMFJSONExporter
    val loader = new KMFJSONLoader
    for ((pcm, index) <- pcms.zipWithIndex) {
      val path = "output/model/" + title.replaceAll(" ", "_") + "_" + index + ".pcm"
      val writer = new FileWriter(path)
      writer.write(serializer.toJson(pcm))
      writer.close()

      loader.load(new File(path))

    }

  }

  def writeToWikiText(title : String, page : Page) {
    val exporter = new PCMModelExporter
    val pcms = exporter.export(page)

    val serializer = new WikiTextExporter

    for ((pcm, index) <- pcms.zipWithIndex) {
      val wikitext = serializer.toWikiText(pcm)
      val writer = new FileWriter("output/wikitext/" + title.replaceAll(" ", "_") +  "_" + index + ".txt")
      writer.write(wikitext)
      writer.close()
    }

  }
  
  "The PCM parser" should "parse the example of tables from Wikipedia" in {
    val pcm = parsePCMFromFile("resources/example.pcm")
    pcm.getMatrices.size should be (1)
   }


   it should "parse these PCMs" in {
	   val wikipediaPCMs = Source.fromFile("resources/pcms_to_test.txt").getLines.toList
	   for(article <- wikipediaPCMs) yield {
       println(article)
	     testArticle(article)
     }
   }

}
