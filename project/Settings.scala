import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import sbt._

import scala.collection.immutable.Seq
import scalariform.formatter.preferences.FormattingPreferences

object Settings {

  object Formatting {
    lazy val formatSettings: Seq[Setting[_]] = Seq(
      ScalariformKeys.autoformat in Test := true,
      ScalariformKeys.autoformat in Compile := true,
      ScalariformKeys.preferences in Test := formattingPreferences,
      ScalariformKeys.preferences in Compile := formattingPreferences
    )

    lazy val docFormatSettings: Seq[Setting[_]] = Seq(
      ScalariformKeys.autoformat in Test := true,
      ScalariformKeys.autoformat in Compile := true,
      ScalariformKeys.preferences in Test := docFormattingPreferences,
        ScalariformKeys.preferences in Compile := docFormattingPreferences
    )

    def formattingPreferences: FormattingPreferences = {
      import scalariform.formatter.preferences._
      FormattingPreferences()
        .setPreference(AlignParameters, true)
        .setPreference(NewlineAtEndOfFile,true)
        .setPreference(RewriteArrowSymbols, true)
        .setPreference(AlignSingleLineCaseStatements, true)
    }

    def docFormattingPreferences: FormattingPreferences = formattingPreferences
  }

}
