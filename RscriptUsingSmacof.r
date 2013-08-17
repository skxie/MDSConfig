args <- commandArgs(trailingOnly = TRUE)
input <- args[1]
files <- args[2]
library(foreign)
library(smacof)
library(plotrix)
simdata <- read.table(input, header=T, sep="^")
dissimdata <- 8 - simdata
names(dissimdata) <- sub("\\.", " ", names(dissimdata))
l <- cbind(attributes(dissimdata)$names)

mdsConf <- smacofSym(dissimdata,ndim=2,eps=10e-12,metric=TRUE,itmax=100000)
title <- paste("smacof Stress:", mdsConf$stress.m, sep = "")

x <- mdsConf$conf[,1]
y <- mdsConf$conf[,2]

xmin <- 1.3*min(x)
xmax <- 1.3*max(x)
ymin <- 1.3*min(y)
ymax <- 1.3*max(y)

jpeg(files, width = 900, height = 900, units = "px")
par(omi=c(1,1,1,1))
plot(x, y, main = title, xlab = "", ylab = "", xlim = c(xmin, xmax), ylim = c(ymin, ymax), type="p")
thigmophobe.labels(x,y,l)
dev.off()

write(mdsConf$stress.m, stdout())
