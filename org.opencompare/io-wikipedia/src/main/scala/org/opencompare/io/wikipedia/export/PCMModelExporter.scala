package org.opencompare.io.wikipedia.export

import java.util
import org.opencompare.api.java.impl.PCMFactoryImpl
import org.opencompare.api.java._
import org.opencompare.api.java.io.{PCMDirection, IOMatrixLoader, IOCell, IOMatrix}
import org.opencompare.io.wikipedia.io.WikiTextLoader
import org.opencompare.io.wikipedia.pcm.{Cell, Matrix, Page}
import scala.collection.JavaConversions._

/**
 * Created by gbecan on 19/11/14.
 */
class PCMModelExporter {

  private val factory = new PCMFactoryImpl
  private val ioLoader = new IOMatrixLoader(factory, PCMDirection.UNKNOWN)

  def export(page : Page) : util.List[PCMContainer] = {
    val pcmContainers = for (matrix <- page.getMatrices) yield {
      val normalizedMatrix = normalize(matrix)
      val ioMatrix = new IOMatrix()
      ioMatrix.setName(normalizedMatrix.name)

      for (r <- 0 until normalizedMatrix.getNumberOfRows(); c <- 0 until normalizedMatrix.getNumberOfColumns()) {
        val cellOpt = normalizedMatrix.getCell(r, c)
        if (cellOpt.isDefined) {
          val cell = cellOpt.get
          val ioCell = new IOCell(cell.content, cell.rawContent)
          ioMatrix.setCell(ioCell, r, c, cell.rowspan, cell.colspan)
        }
      }

      ioLoader.load(ioMatrix)
    }

    seqAsJavaList(pcmContainers)
  }

  /**
   * Normalize a matrix
   * @param matrix
   * @return
   */
  def normalize(matrix : Matrix) : Matrix = {
    // Duplicate cells with rowspan or colspan
    val normalizedMatrix = new Matrix

    for (cell <- matrix.cells.map(_._2)) {
      for (
        rowShift <- 0 until cell.rowspan;
        columnShift <- 0 until cell.colspan
      ) {

        val row = cell.row + rowShift
        val column = cell.column + columnShift

        val duplicate = new Cell(cell.content, cell.rawContent, cell.isHeader, row, 1, column, 1)
        normalizedMatrix.setCell(duplicate, row, column)
      }
    }

    fillMissingCells(normalizedMatrix)

    normalizedMatrix
  }

  /**
   * Detect holes in the matrix and add a cell if necessary
   * @param matrix
   */
  def fillMissingCells(matrix : Matrix) {

    for (row <- 0 until matrix.getNumberOfRows(); column <- 0 until matrix.getNumberOfColumns()) {
      if (!matrix.getCell(row, column).isDefined) {
        val emptyCell = new Cell("", "", false, row, 1, column, 1)
        matrix.setCell(emptyCell, row, column)
      }
    }
  }

}
