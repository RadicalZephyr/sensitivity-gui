#!/usr/bin/Rscript

library(ggplot2)
library(reshape)

subset.by.level <- function(data, var, lvl) {
  vec <- getElement(data, var)
  cols <- names(data) %in% var
  return (data[which(vec==lvl),!cols]);
}

printgraphs <- function (acceptance, name) {
  for (device in levels(acceptance$device.axis)) {
    devsubset <- subset.by.level(acceptance, "device.axis", device)

    pdf(paste(name, "-ByVersion-", device, ".pdf", sep=""))
    for (portion in levels(acceptance$portion)) {
      pdevsubs <- subset.by.level(devsubset, "portion", portion)
      castpdata <<- cast(pdevsubs, ... ~ metric)
      print(ggplot(castpdata, aes(x=version, y=RMS, fill=test)) +
            geom_bar(aes(colour=test), stat="identity",position="dodge") +
            ggtitle(paste("Portion of test:", portion)))
    }
    dev.off()

    pdf(paste(name, "-ByTest-", device, ".pdf", sep=""))
    for (portion in levels(acceptance$portion)) {
      pdevsubs <- subset.by.level(devsubset, "portion", portion)
      castpdata <<- cast(pdevsubs, ... ~ metric)
      print(ggplot(castpdata, aes(x=test, y=RMS, fill=version)) +
            geom_bar(aes(colour=version), stat="identity",position="dodge") +
            ggtitle(paste("Portion of test:", portion)))
    }
    dev.off()

  }
}
args <- commandArgs(TRUE)

fname <- args[1]
targetname <- unlist(strsplit(fname, "\\."))

acceptance <- read.csv(fname)

# Strip versions 17 and 18 from the dataset
acceptance <- acceptance[which(!(acceptance$version %in% c("v0.17.0", "v0.18.0"))),]
printgraphs(acceptance, targetname[1])
