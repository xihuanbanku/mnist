package com.isinonet

import com.huaban.analysis.jieba.{JiebaSegmenter, SegToken}
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode
import org.apache.spark.ml.feature.{HashingTF, IDF}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

/**
  * Created by Administrator on 2018-07-19.
  * 2018-07-19
  */
object Sentence {

  def jiebaSplit(sentence: String):Array[String] = {

    val segment = new JiebaSegmenter()
    segment.process(sentence, SegMode.SEARCH).toArray
      .filter(token =>{
         val word = token.asInstanceOf[SegToken].word
        !word.equals(",")&& !word.equals(" ")
      })
      .map(x => {
        x.asInstanceOf[SegToken].word
      })

  }

  def main(a: Array[String]) :Unit = {

    val sparkSession = SparkSession.builder()
      .master("local")
      .appName("Similar analysis")
      .config("spark.testing.memory", 2147480000)
      .getOrCreate()
    val wordsData = sparkSession.createDataFrame(Seq(
      (1, jiebaSplit("我很喜欢看电影, 也喜欢看书")),
      (2, jiebaSplit("我很喜欢看电影, 也喜欢看书")),
      (3, jiebaSplit("我喜欢旅游, 想去泰国"))
    )).toDF("id", "words")

//    sparkSession.sqlContext.udf.register("jieba", (sentence:String) => {
//      segment.process(sentence, SegMode.SEARCH).toArray.map(x => {
//        x.asInstanceOf[SegToken].word
//      })
//    })
//    val wordsData = sentences.select("id", "words")//.toDF("id", "words")
    wordsData.show(false)
    val hashingTF = new HashingTF()
      .setInputCol("words").setOutputCol("rawFeatures").setNumFeatures(100)

    val featurizedData = hashingTF.transform(wordsData)

    val idf = new IDF().setInputCol("rawFeatures")
      .setOutputCol("features")
    val iDFModel = idf.fit(featurizedData)

    val finalResult = iDFModel.transform(featurizedData)

    finalResult.select("id", "features").show(false)

    sparkSession.stop()

  }
}
