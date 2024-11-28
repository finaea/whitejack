package whitejack

import com.typesafe.config.{Config, ConfigFactory}

import java.net.InetAddress
import scala.io.StdIn

object Configuration {
  var localAddress: Option[InetAddress] = None
  var runLocalOnly: Option[Boolean] = None

  def askServerConfig(port: Int): Config = {
    val defaultIP = "127.0.0.1"
    println(s"Enter the server IP address (if left empty: $defaultIP):")
    val userInput = StdIn.readLine().trim
    val ipString = if (userInput.isEmpty) defaultIP else userInput
    val ipRaw = ipString.split('.').map(_.toInt)

    val inetAddress = InetAddress.getByAddress(ipRaw.map(x => x.toByte))
    localAddress = Some(inetAddress)
    runLocalOnly = Some(true)
    Configuration(localAddress.get.getHostAddress, "", "2222")
  }

  def askClientConfig(): Config = {
    val defaultIP = "127.0.0.1"
    println(s"Enter the client IP address (if left empty: $defaultIP):")
    val userInput = StdIn.readLine().trim
    val ipString = if (userInput.isEmpty) defaultIP else userInput
    val ipRaw = ipString.split('.').map(_.toInt)

    val inetAddress = InetAddress.getByAddress(ipRaw.map(x => x.toByte))
    localAddress = Some(inetAddress)
    runLocalOnly = Some(true)
    Configuration(localAddress.get.getHostAddress, "", "0")
  }

  def apply(extHostName: String, intHostName: String, port: String): Config = {
    ConfigFactory.parseString(
      s"""
         |akka {
         |  loglevel = "INFO" #INFO, DEBUG
         |  actor {
         |    # provider=remote is possible, but prefer cluster
         |    provider =  cluster
         |    allow-java-serialization=on
         |    serializers {
         |      jackson-json = "akka.serialization.jackson.JacksonJsonSerializer"
         |    }
         |    serialization-bindings {
         |      "whitejack.Protocol.JsonSerializable" = jackson-json
         |    }
         |  }
         |  remote {
         |    artery {
         |      transport = tcp # See Selecting a transport below
         |      canonical.hostname = "$extHostName"
         |      canonical.port = $port
         |      bind.hostname = "$intHostName" # internal (bind) hostname
         |      bind.port = $port              # internal (bind) port
         |
         |      #log-sent-messages = on
         |      #log-received-messages = on
         |    }
         |  }
         |  cluster {
         |    downing-provider-class = "akka.cluster.sbr.SplitBrainResolverProvider"
         |  }
         |
         |  discovery {
         |    loglevel = "OFF"
         |    method = akka-dns
         |  }
         |
         |  management {
         |    loglevel = "OFF"
         |    http {
         |      hostname = "$extHostName"
         |      port = 8558
         |      bind-hostname = "$intHostName"
         |      bind-port = 8558
         |    }
         |  }
         |}
         """.stripMargin)
  }
}