import sbt.*

object AppDependencies {

  private val bootstrapVersion = "8.6.0"
  private val mongoVersion = "1.9.0"

  private val compile = Seq(
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"             % mongoVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"     % "9.11.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30" % "2.0.0",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"     % bootstrapVersion
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                 %% "bootstrap-test-play-30"     % bootstrapVersion,
    "uk.gov.hmrc.mongo"           %% "hmrc-mongo-test-play-30"    % mongoVersion,
    "org.scalatestplus"           %% "scalacheck-1-17"            % "3.2.18.0",
    "org.jsoup"                   %  "jsoup"                      % "1.18.1",
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
