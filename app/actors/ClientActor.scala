package actors

import akka.actor._
import models._
import play.api.mvc.Action

class ClientActor() extends Actor {

  //var server:ActorSelection = _

  //  override def preStart(): Unit = {
  //    server = context.actorSelection(s"akka.tcp://MasterActor@$MasterHost:$MasterPort/user/Master")
  //    server ! "connect"
  //  }
  override def receive: Receive = {
    //连接测试
    case "connect ok" => {
      println("连接集群成功")
    }
    //测试任务
    case "收到测试任务" => {
      TaskResult.submit = true
      println("测试提交成功，正在运行")
    }
    case TestResult(result) => {
      TaskResult.succeess = true
      TaskResult.result = result
      println("测试任务完成")
    }
    //ALS任务
    case "收到ALS算法任务" => {
      AlsResult.submit = true
      println("ALS任务提交成功")
    }
    case "ALS推荐算法任务成功结束" => {
      AlsResult.success = true
    }
    //决策树任务
    case "收到决策树任务" => {
      DTResult.submit = true
      println("决策树任务提交成功")
    }
    case DTTaskResult(modelResult, precison, predictResultPath) => {
      DTResult.success = true
      DTResult.modelResult = modelResult
      DTResult.precison = precison
      DTResult.predictResultPath = predictResultPath
    }


    //随机森林任务
    case "收到随机森林任务" => {
      RFResult.submit = true
      println("随机森林任务提交成功")
    }
    case RFTaskResult(modelResult, precison, presictResult) => {
      RFResult.success = true
      RFResult.modelResult = modelResult
      RFResult.precison = precison
      RFResult.predictResultPath = presictResult
    }

      //SVM任务
    case "收到SVM任务" =>{
      SvmResult.submit = true
      println("SVM任务发送成功")
    }
    case SvmTaskResult(result,svmModelResultPath,svmPredictResultPath)=>{
      SvmResult.success = true
      SvmResult.modelResult = svmModelResultPath
      SvmResult.predictResultPath = svmPredictResultPath
      SvmResult.auROC = result
    }
    case string: String => {
      println(string)
    }
  }
}

object ClientActor {
  def props = Props[ClientActor]

}

