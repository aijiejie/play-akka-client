package controllers

import javax.inject.{Inject, Singleton}

import actors._
import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.config.{Config, ConfigFactory}
import models._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

@Singleton
class TaskController @Inject()(cc: ControllerComponents, system: ActorSystem) extends AbstractController(cc) {

  /*表单结构*/

  //测试表单结构
  case class UserInputData(masterHost: String, masterPort: String, dataPath: String, name: String) extends Serializable

  val taskForm = Form(
    mapping(
      "masterHost" -> text,
      "masterPort" -> text,
      "dataPath" -> text,
      "name" -> text
    )(UserInputData.apply)(UserInputData.unapply)
  )

  //命令表单结构
  case class UserInputCmd(masterHost: String, masterPort: String, cmd: String) extends Serializable

  val cmdForm = Form(
    mapping(
      "masterHost" -> text,
      "masterPort" -> text,
      "cmd" -> text
    )(UserInputCmd.apply)(UserInputCmd.unapply)
  )

  //Als表单结构
  case class AlsForm(alsMasterHost: String, alsMasterPort: String, alsDataPath: String,
                     alsResultPath: String, alsResultNumber: Int, alsName: String,
                     alsRank: Int, numIterations: Int, Delimiter: String) extends Serializable

  val alsFrom = Form(
    mapping(
      "alsMasterHost" -> nonEmptyText,
      "alsMasterPort" -> nonEmptyText,
      "alsDataPath" -> nonEmptyText,
      "alsResultPath" -> nonEmptyText,
      "alsResultNumber" -> number,
      "alsName" -> nonEmptyText,
      "alsRank" -> number,
      "numIterations" -> number,
      "Delimiter" -> text
    )(AlsForm.apply)(AlsForm.unapply)
  )

  //决策树表单
  case class DTForm(DTMasterHost: String, DTMasterPort: String, dtTrainDataPath: String, dataPath: String,
                    modelResultPath: String, resultPath: String, numClasses: Int, DTName: String,
                    impurity: String, maxDepth: Int, maxBins: Int, Delimiter: String) extends Serializable

  val dtFrom = Form(
    mapping(
      "DTMasterHost" -> nonEmptyText,
      "DTMasterPort" -> nonEmptyText,
      "dtTrainDataPath" -> nonEmptyText,
      "dataPath" -> text,
      "modelResultPath" -> text,
      "resultPath" -> text,
      "numClasses" -> number,
      "DTName" -> nonEmptyText,
      "impurity" -> text,
      "maxDepth" -> number,
      "maxBins" -> number,
      "Delimiter" -> text
    )(DTForm.apply)(DTForm.unapply)
  )

  //随机森林表单
  case class RFForm(rfMasterHost: String, rfMasterPort: String, rfTrainDataPath: String, dataPath: String, modelResultPath: String, resultPath: String,
                    numClasses: Int, numTrees: Int, rfName: String, featureSubsetStrategy: String, impurity: String, maxDepth: Int, maxBins: Int, Delimiter: String) extends Serializable

  val rfFrom = Form(
    mapping(
      "rfMasterHost" -> nonEmptyText,
      "rfMasterPort" -> nonEmptyText,
      "rfTrainDataPath" -> nonEmptyText,
      "dataPath" -> text,
      "modelResultPath" -> text,
      "resultPath" -> text,
      "numClasses" -> number,
      "numTrees" -> number,
      "rfName" -> nonEmptyText,
      "featureSubsetStrategy" -> text,
      "impurity" -> text,
      "maxDepth" -> number,
      "maxBins" -> number,
      "Delimiter" -> text
    )(RFForm.apply)(RFForm.unapply)
  )


  /*新建客户端actor*/
  val configStr: String =
    s"""
       |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
       |akka.remote.netty.tcp.hostname = "192.168.135.1"
       |akka.remote.netty.tcp.port = "6667"
       """.stripMargin
  val config: Config = ConfigFactory.parseString(configStr)
  val actorSystem = ActorSystem("ClientActor", config)
  val clientActor: ActorRef = actorSystem.actorOf(ClientActor.props, "client")

  //开始页面
  def task = Action {
    implicit request =>
      Ok(views.html.task.render())
  }

  //提交测试处理
  def postForm = Action {
    implicit request =>
      val userInputData = taskForm.bindFromRequest.get
      val masterHost = userInputData.masterHost
      val masterPort = userInputData.masterPort
      val datapath = userInputData.dataPath
      val name = userInputData.name
      var server = actorSystem.actorSelection(s"akka.tcp://MasterActor@$masterHost:$masterPort/user/Server")
      server.tell("connect", clientActor)
      TaskResult.succeess = false
      server.tell(ClientSubmitTask(datapath, name), clientActor)
      Ok(views.html.submit(s"已提交$name 算法"))
  }

  //查看算法结果
  def seeResult = Action {
    implicit request =>
      Ok(views.html.seeResult())
  }

  //提交命令处理
  def postCmd = Action {
    implicit request =>
      val userInputCmd = cmdForm.bindFromRequest.get
      val masterHost = userInputCmd.masterHost
      val masterPort = userInputCmd.masterPort
      val cmd = userInputCmd.cmd
      //var server = actorSystem.actorSelection(s"akka.tcp://MasterActor@$masterHost:$masterPort/user/Supervisor")//监控模式
      var server = actorSystem.actorSelection(s"akka.tcp://MasterActor@$masterHost:$masterPort/user/Server")
      server.tell("connect", clientActor)
      server.tell(cmd, clientActor)
      Ok(s"执行$cmd 命令")
  }

  /**
    * ALS部分
    */


  //ALS初始页面
  def ALS = Action {
    implicit request =>
      Ok(views.html.Als.render())
  }

  //ALS任务提交
  def submitAlsTask = Action {
    implicit request =>
      val alsData = alsFrom.bindFromRequest.get
      val masterHost = alsData.alsMasterHost
      val masterPort = alsData.alsMasterPort
      val datapath = alsData.alsDataPath
      val dataResultPath = alsData.alsResultPath
      val alsRseultNumber = alsData.alsResultNumber
      val name = alsData.alsName
      val rank = alsData.alsRank
      val iter = alsData.numIterations
      val delimiter = alsData.Delimiter
      var server = actorSystem.actorSelection(s"akka.tcp://MasterActor@$masterHost:$masterPort/user/Server")
      server.tell("connect", clientActor)
      AlsResult.success = false
      AlsResult.result = dataResultPath
      server.tell(AlsTask(masterHost, masterPort, datapath, dataResultPath, alsRseultNumber, name, rank, iter, delimiter), clientActor)
      Ok(views.html.submit(s"已提交ALS算法任务,任务名$name"))
  }

  //决策树初始页面
  def DT = Action {
    implicit request =>
      Ok(views.html.DecisionTree.render())
  }

  //决策树任务提交
  def submitDTTask = Action {
    implicit request =>
      val dtData = dtFrom.bindFromRequest.get
      val masterHost = dtData.DTMasterHost
      val masterPort = dtData.DTMasterPort
      val dtTrainData = dtData.dtTrainDataPath
      val predictData = dtData.dataPath
      val modelResult = dtData.modelResultPath
      val result = dtData.resultPath
      val name = dtData.DTName
      val delimiter = dtData.Delimiter
      val impurity = dtData.impurity
      val maxBins = dtData.maxBins
      val maxDepth = dtData.maxDepth
      val numClasses = dtData.numClasses
      val server = actorSystem.actorSelection(s"akka.tcp://MasterActor@$masterHost:$masterPort/user/Server") //无监督创建actor
      //var server = actorSystem.actorSelection(s"akka.tcp://MasterActor@$masterHost:$masterPort/user/Supervisor")
      server.tell("connect", clientActor)
      DTResult.success = false
      server.tell(DTTask(masterHost, masterPort, dtTrainData, predictData, modelResult, result,
        numClasses, name, impurity, maxDepth, maxBins, delimiter), clientActor)
      Ok(views.html.submit(s"已提交决策树算法任务,任务名$name"))
  }


  //随机森林初始页面
  def RF = Action {
    implicit request =>
      Ok(views.html.RandomForest.render())
  }

  //随机森林任务提交
  def submitRfTask = Action {
    implicit request =>
      val rfData = rfFrom.bindFromRequest.get
      val masterHost = rfData.rfMasterHost
      val masterPort = rfData.rfMasterPort
      val rfTrainData = rfData.rfTrainDataPath
      val predictData = rfData.dataPath
      val modelResult = rfData.modelResultPath
      val result = rfData.resultPath
      val name = rfData.rfName
      val delimiter = rfData.Delimiter
      val impurity = rfData.impurity
      val maxBins = rfData.maxBins
      val maxDepth = rfData.maxDepth
      val numClasses = rfData.numClasses
      val numTrees = rfData.numTrees
      val featureSubsetStrategy = rfData.featureSubsetStrategy
      val server = actorSystem.actorSelection(s"akka.tcp://MasterActor@$masterHost:$masterPort/user/Server") //无监督创建actor
      //var server = actorSystem.actorSelection(s"akka.tcp://MasterActor@$masterHost:$masterPort/user/Supervisor")
      server.tell("connect", clientActor)
      RFResult.success = false
      server.tell(RFTask(masterHost, masterPort, rfTrainData, predictData, modelResult, result,
        numClasses, numTrees, name, featureSubsetStrategy, impurity, maxDepth, maxBins, delimiter), clientActor)
      Ok(views.html.submit(s"已提交随机森林算法任务,任务名$name"))
  }


  //SVM表单
  case class SvmForm(svmMasterHost: String, svmMasterPort: String, svmTrainDataPath: String,
                     svmPredictDataPath: String, svmModelResultPath:String,svmPredictResultPath: String, svmName: String,
                     numIterations: Int) extends Serializable

  val svmFrom = Form(
    mapping(
      "svmMasterHost" -> nonEmptyText,
      "svmMasterPort" -> nonEmptyText,
      "svmTrainDataPath" -> nonEmptyText,
      "svmPredictDataPath" -> text,
      "svmModelResultPath" -> text,
      "svmPredictResultPath" -> text,
      "svmName" -> nonEmptyText,
      "numIterations" -> number
    )(SvmForm.apply)(SvmForm.unapply)
  )

  def svm = Action {
    implicit request =>
      Ok(views.html.svm.render())
  }


  def submitSvmTask = Action {
    implicit request =>
      val svmData = svmFrom.bindFromRequest.get
      val svmMasterHost = svmData.svmMasterHost
      val svmMasterPort = svmData.svmMasterPort
      val svmTrainDataPath = svmData.svmTrainDataPath
      val svmPredictDataPath = svmData.svmPredictDataPath
      val svmModelResultPath = svmData.svmModelResultPath
      val svmPredictResultPath = svmData.svmPredictResultPath
      val name = svmData.svmName
      val iter = svmData.numIterations
      var server = actorSystem.actorSelection(s"akka.tcp://MasterActor@$svmMasterHost:$svmMasterPort/user/Server")
      server.tell("connect", clientActor)
      SvmResult.success = false
      SvmResult.modelResult = svmModelResultPath
      server.tell(SvmTask(svmMasterHost,svmMasterPort,svmTrainDataPath,
        svmPredictDataPath,svmModelResultPath,svmPredictResultPath,
        name,iter), clientActor)
      Ok(views.html.submit(s"已提交SVM算法任务,任务名$name"))
  }
}
