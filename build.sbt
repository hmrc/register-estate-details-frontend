import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.DefaultBuildSettings.itSettings
import uk.gov.hmrc.DefaultBuildSettings.targetJvm

lazy val appName: String = "register-estate-details-frontend"

ThisBuild / scalaVersion := "3.9.0"
ThisBuild / targetJvm := "jvm-21"
ThisBuild / majorVersion := 1

lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(CodeCoverageSettings())
  .settings(
    name := appName,
    RoutesKeys.routesImport += "models.*",
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat.*",
      "uk.gov.hmrc.govukfrontend.views.html.components.*",
      "uk.gov.hmrc.hmrcfrontend.views.html.components.*",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers.*",
      "views.ViewUtils.*",
      "models.Mode",
      "controllers.routes.*"
    ),
    PlayKeys.playDefaultPort := 8823,
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:src=routes/.*:s",
      "-Wconf:msg=unused import&src=html/.*:s"
    ),
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true
  )

lazy val it = project
  .in(file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(itSettings())

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt it/Test/scalafmt")
