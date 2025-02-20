package org.floxx

import org.floxx.env.configuration.config
import org.floxx.env.configuration.config.Configuration
import org.floxx.env.repository._
import org.floxx.env.service._
import org.floxx.env.service.trackService.TrackService
import zio.blocking.Blocking
import zio.clock.Clock
import zio._
import zio.logging.{LogFormat, LogLevel, Logging}

object environment {

  val dbTransactor = {
    (config.layer ++ Blocking.live ++ Clock.live) >>> QuillContext.dataSourceLayer
  }

  val loggingLayer = Logging.console(
    logLevel = LogLevel.Info,
    format = LogFormat.ColoredLogFormat()
  ) >>> Logging.withRootLoggerName("Floxx")

  //repository
  val hitRepo   = dbTransactor >>> hitRepository.layer
  val cfpRepo   = dbTransactor >>> cfpRepository.layer
  val statsRepo = dbTransactor >>> statsRepository.layer
  val userRepo  = dbTransactor >>> userRepository.layer

  //services
  val adminSer    = userRepo ++ cfpRepo >>> adminService.layer
  val trackSer    = (cfpRepo ++ config.layer) >>> trackService.layer
  val hitSer      = (trackSer ++ hitRepo ++ config.layer) >>> hitService.layer
  val securitySer = (loggingLayer ++ userRepo ++ config.layer) >>> securityService.layer
  val statsSer    = statsRepo >>> statService.layer

  type AppEnvironment =
    Clock with Blocking with Has[Configuration] with Has[TrackService] with Has[hitService.HitService] with Has[
      securityService.SecurityService
    ] with Has[statService.StatsService] with Has[adminService.AdminService]

  val appEnvironnement = Clock.live ++
  Blocking.live ++
    loggingLayer ++
  config.layer ++
  hitSer ++
  securitySer ++
  statsSer ++
  trackSer ++
  adminSer

}
