package com.intellij.idea

import sbt.{ModuleReport, ConfigurationReport, UpdateReport, ModuleID, ScalaInstance}

object SbtIdeaModuleMapping {

  def toIdeaLib(instance: ScalaInstance) = {
    IdeaLibrary("scala-" + instance.version, List(instance.libraryJar, instance.compilerJar),
      instance.extraJars.filter(_.getAbsolutePath.endsWith("docs.jar")),
      instance.extraJars.filter(_.getAbsolutePath.endsWith("-sources.jar")))
  }


  private def equivModule(m1: ModuleID, m2: ModuleID) = (m1.organization == m2.organization) && (m1.name == m2.name)

  private def ideaLibFromModule(moduleReport: ModuleReport) = {
    val module = moduleReport.module
    IdeaLibrary(module.organization + "_" + module.name,
      classes = moduleReport.artifacts.collect{ case (artifact, file) if (artifact.classifier == None) => file },
      javaDocs = moduleReport.artifacts.collect{ case (artifact, file) if (artifact.classifier == Some("javadoc")) => file },
      sources = moduleReport.artifacts.collect{ case (artifact, file) if (artifact.classifier == Some("sources")) => file })
  }

  private def toScope(conf: String) = {
    import com.intellij.idea.IdeaLibrary._
    conf match {
      case "compile" => CompileScope
      case "runtime" => RuntimeScope
      case "test" => TestScope
      case "provided" => ProvidedScope
      case _ => CompileScope
    }
  }

  private def convertConfigReport(configReport: ConfigurationReport, deps: Seq[ModuleID]) = {
    val scope = toScope(configReport.configuration)
    val depFilter = libDepFilter(deps, configReport.configuration) _
    configReport.modules.filter(modReport => depFilter(modReport.module)).map( moduleReport => {
      IdeaModuleLibRef(scope, ideaLibFromModule(moduleReport))
    })
  }

  private def libDepFilter(deps: Seq[ModuleID], configuration: String)(module: ModuleID): Boolean = {
    deps.exists(libModule => {
      val libConf = libModule.configurations.getOrElse("compile")
        (libConf == configuration) && equivModule(libModule, module)
    })
  }

  def convertDeps(report: UpdateReport, deps: Seq[ModuleID]): Seq[IdeaModuleLibRef] = {
    report.configurations.flatMap(convertConfigReport(_, deps))
  }
}