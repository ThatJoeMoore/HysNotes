import java.io.{PrintWriter, FileWriter, File}

for (x <- 1 to 20) {
  val f = new File(s"../annotations/src/main/java/com/thatjoemoore/utils/hystrix/annotations/args/Arguments$x.java")
  f.getParentFile.mkdirs()
  f.createNewFile()

  val fw = new FileWriter(f)
  val writer = new PrintWriter(fw)

  writer.println("package com.thatjoemoore.utils.hystrix.annotations.args;")
  writer.println()
  writer println
    s"""/**
      | * Argument list, containing $x arguments
    """.stripMargin
  for (i <- 0 until x) {
    writer println s" * @param <Type$i> Type of argument $i"
  }
  writer println " */"
  writer print "public interface Arguments"
  writer print x
  writer print (0 until x).mkString("<Type", ", Type", ">")
  writer println " extends Arguments {"

  for (i <- 0 until x) {
    writer println()
    writer println
      s"""    /**
         |     * Get the value of argument $i
         |     * @return value of the argument
         |     */
         |     Type$i arg$i();
       """.stripMargin
  }

  writer println "}"

  writer.flush()
  writer.close()
  fw.close()

}