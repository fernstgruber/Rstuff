require(e1071)
source("/media/fabs/Volume/Data/temp_delilah/DISS_new/neueszupaper1/fabians_and_rossiters_functions.R")
#source("/home/fabs/Data/ST_landformclasses/ST_landforms_funcsandlegends/relief_legends.R")
setwd("/media/fabs/Volume/Data/temp_delilah/DISS_new/neueszupaper1/FWCV/")
load("makroreddata_andpredlists.RData")

dependent="Def_red_mak"
data=makroreddata
preds=predsetlist
##################
load(file="fuzzydata.RData")
allcols <- names(fuzzy10m)[-1]
data <- merge(data,fuzzy10m,by="AufID")
goodnewfuzzycols <- character()
for(i in allcols){
  if(length(unique(data[[i]])) > 1) {
    goodnewfuzzycols <- c(goodnewfuzzycols,i)
  }
}
paramsets[[5]] <- goodnewfuzzycols
preds <- append(preds,goodnewfuzzycols[!(goodnewfuzzycols %in% preds)])
###
newinfo <- read.table("/media/fabs/Volume/Data/temp_delilah/DISS_new/neueszupaper1/Forstpuntkte_curvsandfuzzy_lowres.csv",sep=",",header=T)
evennewerinfo <- read.table("/media/fabs/Volume/Data/temp_delilah/DISS_new/neueszupaper1/Forstpunkte_newfuzzy_1050.csv",sep=",",header=T)
curvtest <- read.table("/media/fabs/Volume/Data/temp_delilah/DISS_new/neueszupaper1/Forstpunkte_newcurvs_1050.csv",sep=",",header=T)
curvcols_plus <- names(newinfo)[c(297:504,739:946,1178:1385)]
curvcols_plusplus <- names(curvtest)[55:470]
allcurvcols_plusplus <- c(curvcols_plusplus,curvcols_plus)
newcurvcols <- allcurvcols_plusplus[!(allcurvcols_plusplus %in% names(data))]
basedata <- data[c("AufID",dependent)]
newdata <- merge(basedata,newinfo[c("AufID",curvcols_plus)],all.x=T)
newdata <- merge(newdata,curvtest[c("AufID",curvcols_plusplus)],all.x=T)
data <- merge(data,newdata[c("AufID",newcurvcols)],by="AufID", all.x=T)
#####
preds <- append(preds,allcurvcols_plusplus[!(allcurvcols_plusplus %in% preds)])
fullmodelcols <- c(dependent,preds)
mymodeldata <-data[fullmodelcols]
#########################################################################################
paramsetnames = paramsetnames
paramsets = paramsets
n=1
paramsets[[1]] <- allcurvcols_plusplus
tpiterraintophat <- c(unlist(paramsets[2]),unlist(paramsets[7]),unlist(paramsets[9]))
paramsets[[10]] <- tpiterraintophat
paramsetnames <- append(paramsetnames,"tpiterraintophat")
folds = sample(rep(1:10, length = nrow(mymodeldata)))
p=paramsets[10]
newparamsets <- list(paramsets[[1]],paramsets[[3]],paramsets[[4]],paramsets[[10]],paramsets[[8]],paramsets[[5]])
  newparamsetnames <-paramsetnames[c(1,3,4,10,8,5)]
for (p in newparamsets){
predset_name <- paramsetnames[n]
preds <- unlist(p)
withna <- vector()
for (i in preds){
  if(sum(is.na(mymodeldata[[i]])) != 0) {
    withna <- c(withna,as.character(i))
  }
}
preds = preds[!(preds %in% withna)]
predset=preds
tt=1:5 #number of best parameters in combination
mydir=paste("kappa_svm_fw_10fold_5p_",dependent,"_",predset_name,sep="")
dir.create(mydir)
#############################################################################################################################
#############################################################################################################################
k=1
for(k in 1:10){
  kmodeldata=mymodeldata[folds != k,]
  ktestdata =  mymodeldata[folds == k,]
  keepers <- vector()
  pred_df_orig <- data.frame(preds = as.character(predset))
  pred_df_orig$index <- 1:nrow(pred_df_orig)
  pred_df <- pred_df_orig
  result_df <- data.frame(tt)
  predictions_metrics <- data.frame(index=as.character(unique(pred_df$preds)))
  predset_new <- predset
  t=1
  for(t in tt){
    predictions_metrics <- predictions_metrics
    predset_new <-predset_new[!(predset_new %in% keepers)]
    seed=sample(1:1000,1)
    g=predset_new[1]
    for(g in predset_new){
      starttime <- proc.time()
      set.seed(seed)
      modelcols <- c(dependent,g,keepers)
      modeldata <- kmodeldata[names(kmodeldata) %in% modelcols]
      f <- paste(dependent,"~.")
      fit <- do.call("svm",list(as.formula(f),modeldata,cross=10))
      preds <- predict(fit,modeldata)
      CM <- table(modeldata[[dependent]],preds)
      sumkappa = kappa(CM)
      predictions_metrics[predictions_metrics$index== eval(g),"kappa"] <- 1-sumkappa$sum.kappa
      endtime <- proc.time()
      time <- endtime - starttime
      print(paste(g, " with kappa error.rate of ",1-sumkappa$sum.kappa, "and time =",time[3]))
      #######################################################################
    }
    predictions_metrics <- predictions_metrics[order(predictions_metrics$kappa),]
    
    minindex <- predictions_metrics[predictions_metrics$kappa == min(predictions_metrics$kappa),"index"][1]
    print(paste("###########################################################################################################################################################remove the metric ",minindex,"##############################################################################################################################################################",sep=""))
    result_df[t,"metric"] <- minindex
    result_df[t,"kappa"] <- predictions_metrics[predictions_metrics$index == minindex,"kappa"]
    keepers[t] <-as.character(minindex)
    modelcols <- c(dependent,keepers)
    modeldata <- modeldata <- kmodeldata[names(kmodeldata) %in% modelcols]
    modeldata <- na.omit(modeldata)
    set.seed(seed)
    fit <- do.call("svm",list(as.formula(f),modeldata,cross=10))
    preds=predict(fit,newdata=ktestdata[modelcols])
    prederror=mean(ktestdata[[dependent]] != preds)
    result_df[t,"geheimerprederror"] <- prederror
    CM <- table(ktestdata[[dependent]],preds)
    sumkappa = kappa(CM)
    result_df[t,"geheimerkappaerror"] <-  1-sumkappa$sum.kappa
    print(paste("geheimerprederror = ",prederror,sep=""))
    testdatatable <- table(ktestdata[[dependent]])
    traindatatable<- table(kmodeldata[[dependent]])
    save(predictions_metrics,result_df,fit,keepers,traindatatable,testdatatable,file=paste(mydir,"/k",k,"_round_",t,".RData",sep=""))
    predictions_metrics <- predictions_metrics[predictions_metrics$index != as.character(minindex),]
    
  }
}
n=n+1
}

  
  