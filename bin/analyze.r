#!/usr/bin/Rscript

library(reshape)

evaluate <- function (ds) {
  ds2 <- ds[which(ds$metric %in% c("ABS", "RMS")),]
  dscast <- cast(ds2, version ~ device.axis+metric, fun.aggregate=mean)
  dscast[,2:13] <- scale(dscast[,2:13])
  return(dscast)
}

args <- commandArgs(TRUE)

ds <- read.csv(args[1])
print(evaluate(ds))
