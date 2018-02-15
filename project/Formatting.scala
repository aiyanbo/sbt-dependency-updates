import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import sbt.{AutoPlugin, Def, PluginTrigger, Setting, _}

import scalariform.formatter.preferences.FormattingPreferences

object Formatting extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Def.Setting[_]] = formatSettings

  lazy val formatSettings: Seq[Setting[_]] = Seq(
    ScalariformKeys.autoformat in Test := true,
    ScalariformKeys.autoformat in Compile := true,
    ScalariformKeys.preferences in Test := formattingPreferences,
    ScalariformKeys.preferences in Compile := formattingPreferences
  )

  lazy val docFormatSettings: Seq[Setting[_]] = Seq(
    ScalariformKeys.preferences in Test := docFormattingPreferences,
    ScalariformKeys.preferences in Compile := docFormattingPreferences
  )

  def formattingPreferences: FormattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences()
      .setPreference(AlignParameters, true)
      .setPreference(NewlineAtEndOfFile, true)
      .setPreference(RewriteArrowSymbols, true)
      .setPreference(AllowParamGroupsOnNewlines, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentConstructorArguments, true)
  }

  def docFormattingPreferences: FormattingPreferences = formattingPreferences

}
