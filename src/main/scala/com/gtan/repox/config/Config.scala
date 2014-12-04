package com.gtan.repox.config

import java.nio.file.{Path, Paths}

import akka.agent.Agent
import com.gtan.repox.{Immediate404Rule, Repo, Repox}
import com.ning.http.client.{AsyncHttpClient, ProxyServer => JProxyServer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Created by IntelliJ IDEA.
 * User: xf
 * Date: 14/11/30
 * Time: 下午3:10
 */


case class Config(proxies: Map[String, JProxyServer],
                  repos: Seq[Repo],
                  proxyUsage: Map[Repo, JProxyServer],
                  immediate404Rules: Seq[Immediate404Rule],
                  storage: Path,
                  connectionTimeout: Duration,
                  connectionIdleTimeout: Duration,
                  mainClientMaxConnectionsPerHost: Int,
                  mainClientMaxConnections: Int,
                  proxyClientMaxConnectionsPerHost: Int,
                  proxyClientMaxConnections: Int)

object Config {
  val defaultProxies = Map(
    "lantern" -> new JProxyServer("localhost", 8787)
  )
  val defaultRepos: Seq[Repo] = Seq(
    Repo(1, "koala", "http://nexus.openkoala.org/nexus/content/groups/Koala-release",
      priority = 1, getOnly = true, maven = true),
    Repo(2, "sonatype", "http://oss.sonatype.org/content/repositories/releases", priority = 2),
    Repo(3, "typesafe", "http://repo.typesafe.com/typesafe/releases", priority = 2),
    Repo(4, "oschina", "http://maven.oschina.net/content/groups/public",
      priority = 2, getOnly = true, maven = true),
    Repo(5, "sbt-plugin", "http://dl.bintray.com/sbt/sbt-plugin-releases", priority = 4),
    Repo(6, "scalaz", "http://dl.bintray.com/scalaz/releases", priority = 4),
    Repo(7, "central", "http://repo1.maven.org/maven2", priority = 4, maven = true),
    Repo(8, "ibiblio", "http://mirrors.ibiblio.org/maven2", priority = 5, maven = true)
  )

  val defaultImmediate404Rules: Seq[Immediate404Rule] = Vector(
    Immediate404Rule(1, """.+-javadoc\.jar"""), // we don't want javadoc
    Immediate404Rule(2, """.+-parent.*\.jar"""), // parent have no jar
    Immediate404Rule(3, """/org/scala-sbt/.*""", exclude = Some( """/org/scala-sbt/test-interface/.*""")), // ivy only artifact have no maven uri
    //    Immediat404Rule( """/org/scala-tools/.*"""), // ivy only artifact have no maven uri
    Immediate404Rule(4, """/com/eed3si9n/.*"""), // ivy only artifact have no maven uri
    Immediate404Rule(5, """/io\.spray/.*""", exclude = Some( """/io\.spray/sbt-revolver.*""")), // maven only artifact have no ivy uri
    Immediate404Rule(6, """/org/jboss/xnio/xnio-all/.+\.jar"""),
    Immediate404Rule(7, """/org\.jboss\.xnio/xnio-all/.+\.jar"""),
    Immediate404Rule(8, """/org/apache/apache/(\d+)/.+\.jar"""),
    Immediate404Rule(9, """/org\.apache/apache/(\d+)/.+\.jar"""),
    Immediate404Rule(10, """/com/google/google/(\d+)/.+\.jar"""),
    Immediate404Rule(11, """/com\.google/google/(\d+)/.+\.jar"""),
    Immediate404Rule(12, """/org/ow2/ow2/.+\.jar"""),
    Immediate404Rule(13, """/org\.ow2/ow2/.+\.jar"""),
    Immediate404Rule(14, """/com/github/mpeltonen/sbt-idea/.*\.jar"""),
    Immediate404Rule(15, """/com\.github\.mpeltonen/sbt-idea/.*\.jar"""),
    Immediate404Rule(16, """/org/fusesource/leveldbjni/.+-sources\.jar"""),
    Immediate404Rule(17, """/org\.fusesource\.leveldbjni/.+-sources\.jar"""),
    Immediate404Rule(18, """.*/jsr305.*\-sources\.jar""")
  )

//  def seq2map[T](s: Seq[T]): Map[Long, T] = s.groupBy(_.id).map({ case (k, v) => k -> v.head})

  val default = Config(
    proxies = defaultProxies,
    repos = defaultRepos,
    proxyUsage = Map(),
    immediate404Rules = defaultImmediate404Rules,
    storage = Paths.get(System.getProperty("user.home"), ".repox", "storage"),
    connectionTimeout = 6 seconds,
    connectionIdleTimeout = 10 seconds,
    mainClientMaxConnections = 200,
    mainClientMaxConnectionsPerHost = 10,
    proxyClientMaxConnections = 10,
    proxyClientMaxConnectionsPerHost = 20
  )

  val instance: Agent[Config] = Agent[Config](null)

  def set(data: Config): Future[Config] = instance.alter(data)

  def get = instance.get()

  def storage: Path = instance.get().storage

  def repos: Seq[Repo] = instance.get().repos

  def proxies: Map[String, JProxyServer] = instance.get().proxies

  def proxyUsage: Map[Repo, JProxyServer] = instance.get().proxyUsage

  def immediate404Rules: Seq[Immediate404Rule] = instance.get().immediate404Rules

  def clientOf(repo: Repo): AsyncHttpClient = instance.get().proxyUsage.get(repo) match {
    case None => Repox.mainClient
    case Some(proxy) => Repox.proxyClients(proxy)
  }

  def connectionTimeout: Duration = instance.get().connectionTimeout

  def connectionIdleTimeout: Duration = instance.get().connectionIdleTimeout

  def mainClientMaxConnections: Int = instance.get().mainClientMaxConnections

  def mainClientMaxConnectionsPerHost: Int = instance.get().mainClientMaxConnectionsPerHost

  def proxyClientMaxConnections: Int = instance.get().proxyClientMaxConnections

  def proxyClientMaxConnectionsPerHost: Int = instance.get().proxyClientMaxConnectionsPerHost
}