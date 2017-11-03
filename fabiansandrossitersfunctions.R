kappa <- function(CM) {
  #convert both data frames and vectors to matrices
  cmx<-as.matrix(CM)
  #try to convert a vector to a square matrix
  if (ncol(cmx) == 1)
    cmx<-matrix(cmx, byrow=TRUE, nrow=sqrt(nrow(cmx)))
  nr<-nrow(cmx); nc<-ncol(cmx)
  if (nr != nc)
  { print("Error: matrix is not square"); return(NULL) }
  n<-sum(cmx)
  d<-diag(cmx); dsum<-sum(d); th1<-dsum/n # also die correct rate ist th1, 
  #bei wiki heisst th1 also p0
  th1v<-((th1*(1-th1))/n)
  csum<-apply(cmx,2,sum); rsum<-apply(cmx,1,sum)
  ua<-d/rsum; pa<-d/csum #user accuracy ist als anteil der echt dieser klasse zugehörigen,
  #producers reliability anteil der richtigen an allen die als diese klasse klassifiziert wurden!
  th2 <- sum(rsum*csum) / n^2; kh <- (th1-th2)/(1-th2) #HIER WIRD KAPPA BERECHNET, th2 is pe, also the wahrscheinlichkeit bei unabhängigkeit
  th3 <- sum( (csum + rsum) * d ) / n^2;
  th4 <- 0; for (i in 1:nr) for (j in 1:nc)
    th4 <- th4 + (cmx[i,j] * ((csum[i] + rsum[j])^2));
  th4 <- th4 / n^3;
  th1c <- 1 - th1; th2c <- 1 - th2;
  khv <- 1/n *
    (
      ( ( th1 * th1c ) / th2c^2 )
      + ( ( 2 * th1c * ((2*th1*th2) - th3) ) / th2c^3 )
      + ( ( th1c^2 * ( th4 - (4 * th2^2 ) ) ) / th2c^4 )
    ) # das ist die Varianz von kappa
  #per-class kappa, user’s accuracy...
  p <- cmx/n; uap <- apply(p,1,sum); pap <- apply(p,2,sum); dp<-diag(p);
  kpu <- (dp/uap - pap)/(1 - pap);
  #...and its variance
  t1 <- uap-dp; t2 <- (pap*uap)-dp; t3 <- dp*(1 - uap - pap + dp);
  kpuv <- ( (t1/(uap^3 * (1-pap)^3)) * ((t1*t2) + t3) )/n;
  #per-class kappa, producer’s reliability...
  kpp <- (dp/pap - uap)/(1 - uap);
  #...and its variance
  t1 <- (pap-dp);
  kppv <- ( (t1/(pap^3 * (1-uap)^3)) * ((t1*t2) + t3) )/n;
  #return all statistics as a list
  return(list(sum.n=n, sum.naive=th1, sum.var=th1v, sum.kappa=kh, sum.kvar=khv,
              user.naive=ua, prod.naive=pa,
              user.kappa=kpu, user.kvar=kpuv, prod.kappa=kpp, prod.kvar=kppv))
}

summary.kappa <- function(kappa, alpha=0.05) {
  ciw<-function(var, n) {
    qnorm(1-(alpha/2))*sqrt(var) + (1/(2*n))
  }
  print(paste("Number of observations:", kappa$sum.n), quote=F)
  print("Summary of naive statistics", quote=F)
  print(paste(
    "Overall accuracy, stdev, CV%:",
    round(kappa$sum.naive, 4), ",",
    round(sqrt(kappa$sum.var), 4), ",",
    round((sqrt(kappa$sum.var)/kappa$sum.naive)*1000,0)/10),
    quote=F)
  w<-ciw(kappa$sum.var, kappa$sum.n)
  print(paste(
    round((1-alpha)*100,0),"% confidence limits for accuracy:",
    round((kappa$sum.naive-w),4),"...",
    round((kappa$sum.naive+w),4)), quote=F, sep="")
  print("User’s accuracy", quote=F); print(round(kappa$user.naive,4));
  print("Producer’s reliability:", quote=F); print(round(kappa$prod.naive,4));
  print("Summary of kappa statistics", quote=F)
  print(paste("Overall kappa, stdev, & CV%:",
              round(kappa$sum.kappa,4), ",",
              round(sqrt(kappa$sum.kvar),4), ",",
              round((sqrt(kappa$sum.kvar)/kappa$sum.kappa)*1000,0)/10), quote=F)
  w<-ciw(kappa$sum.kvar, kappa$sum.n)
  print(paste(
    round((1-alpha)*100,0),"% confidence limits for kappa:",
    round((kappa$sum.kappa-w),4),"...",
    round((kappa$sum.kappa+w),4)), quote=F, sep="")
  print("Per-class kappa, stdev, & CV%, for user’s accuracy:", quote=F)
  print(round(kappa$user.kappa,4), quote=F);
  print(round(sqrt(kappa$user.kvar),4), quote=F);
  print(round((sqrt(kappa$user.kvar)/kappa$user.kappa)*1000,0)/10, quote=F);
  print("Per-class kappa, stdev, & CV%, for producer’s reliability:", quote=F)
  print(round(kappa$prod.kappa,4), quote=F);
  print(round(sqrt(kappa$prod.kvar),4), quote=F);
  print(round((sqrt(kappa$prod.kvar)/kappa$prod.kappa)*1000,0)/10, quote=F);
}

tau <- function(CM, P) {
  #convert both data frames and vectors to matrices
  cmx<-as.matrix(CM)
  #try to convert a vector to a square matrix
  if (ncol(cmx) == 1)
    cmx<-matrix(cmx, byrow=TRUE, nrow=sqrt(nrow(cmx)))
  nr<-nrow(cmx); nc<-ncol(cmx)
  if (nr != nc)
  { print("Error: matrix is not square"); return(NULL) }
  #check P and create if necessary
  if (missing(P))
    P<-rep(1/nr, nr)
  if (length(P) != nc)
  { print("Error: prior probabilities vector has wrong length"); return(NULL) }
  if (abs(1-sum(P)) > 0.0001)
  { print("Error: prior probabilities must sum to 1"); return(NULL) }
  n<-sum(cmx)
  d<-diag(cmx); dsum<-sum(d); th1<-dsum/n
  csum<-apply(cmx,2,sum); th2<-(csum%*%P)/n
  tau<-(th1-th2)/(1-th2);
  th3<-sum( (csum + (P*n)) * diag(cmx) ) / n^2;
  rsum<-apply(cmx,1,sum)
  ua<-d/rsum; pa<-d/csum
  th4 <- 0; for (i in 1:nr) for (j in 1:nc)
    th4 <- th4 + (cmx[i,j] * ((csum[i] + P[j]*n)^2));
  th4 <- th4 / n^3;
  th1c <- 1 - th1; th2c <- 1 - th2;
  tv <- 1/n *
    (
      ( ( th1 * th1c ) / th2c^2 )
      + ( ( 2 * th1c * ((2*th1*th2) - th3) ) / th2c^3 )
      + ( ( th1c^2 * ( th4 - (4 * th2^2 ) ) ) / th2c^4 )
    )
  return(list(prior=P, obs=rsum, ref=csum, n=n, tau=tau, tvar=tv,
              coeff=c(th1, th2, th3, th4)))
}

summary.tau <- function(tau, alpha=0.05) {
  ciw<-function(var, n) {
    qnorm(1-(alpha/2))*sqrt(var) + (1/(2*n))
  }
  print(paste("Number of observations:", tau$n), quote=F)
  print("Prior class probabilities:", quote=F)
  print(tau$prior, quote=F)
  print("Observed class proportions:", quote=F)
  print(round(tau$obs/tau$n,4), quote=F)
  print("Reference class proportions:", quote=F)
  print(round(tau$ref/tau$n,4), quote=F)
  print(paste("Tau, stdev, & CV%:",
              round(tau$tau,4), ",",
              round(sqrt(tau$tvar),4), ",",
              round((sqrt(tau$tvar)/tau$tau)*1000,0)/10), quote=F)
  w<-ciw(tau$tvar, tau$n)
  print(paste(round((1-alpha)*100,0),"% confidence limits for tau:",
              round((tau$tau-w),4), "...", round((tau$tau+w),4), sep=""), quote=F)
}

kw <- function(CM, W = diag(sqrt(length(as.matrix(CM)))) ) {
  cmx<-as.matrix(CM); wx<-as.matrix(W)
  #try to convert a vector to a square matrix
  if (ncol(cmx) == 1)
    cmx<-matrix(cmx, byrow=TRUE, nrow=sqrt(nrow(cmx)))
  if (ncol(wx) == 1)
    wx<-matrix(wx, byrow=TRUE, nrow=sqrt(nrow(wx)))
  nr<-nrow(cmx); nc<-ncol(cmx)
  if (nr != nc) { print("Error: confusion matrix is not square"); return(NULL) }
  if (dim(wx) != dim(cmx))
  { print("Weight and Confusion Matrices are not the same size"); return(NULL) }
  #summarize cmx
  n<-sum(cmx); rs<-apply(cmx,1,sum); cs<-apply(cmx,2,sum)
  # confusion matrix and marginals as proportions
  p <- cmx/n; cp <- cs/n; rp <- rs/n;
  if ( round((sum(rp) + sum(cp))/2, 2) != 1)
  { print("Error: Bad checksum in row proportions"); return(NULL) }
  # expected proportions
  pp<- rp %o% cp
  # weighted weights
  wr <- wx%*%cp; wc <- t(t(wx)%*%rp);
  # marginal accuracy
  # rows = user’s
  ua <- apply(wx*p,1,sum)/rp; uasd<-sqrt(ua*(1-ua)/rs);
  # columns = producer’s
  pa <- apply(wx*p,2,sum)/cp; pasd<-sqrt(pa*(1-pa)/cs);
  thw1 <- sum(sum(p * wx)); thw1v<-((thw1*(1-thw1))/n)
  thw2 <- sum(sum(pp * wx));
  khw <- (thw1-thw2)/(1-thw2);
  thw1c <- 1 - thw1; thw2c <- 1 - thw2;
  thw4 <- 0; for (i in 1:nr) for (j in 1:nc)
    thw4 <- thw4 + (p[i,j]*((wx[i,j]*thw2c - (wr[i]+wc[j]) * thw1c)^2 ))
  khwv <- (thw4 - (thw1*thw2 - 2*thw2 + thw1)^2) / (n * thw2c^4)
  return(list(
    sum.n=n,
    sum.kappa=khw, sum.kvar=khwv, theta=c(thw1,thw2,thw4),
    sum.naive=thw1, sum.var=thw1v,
    user.wa=ua, prod.wa=pa,
    user.wsd=uasd, prod.wsd=pasd,
    weights.row=wr, weights.col=wc, expected=pp))
}

summary.kw <- function(kw, alpha=0.05) {
  ciw<-function(var, n) {
    qnorm(1-(alpha/2))*sqrt(var) + (1/(2*n))
  }
  print(paste("Number of observations:", kw$sum.n), quote=F)
  print(paste("Sum of weighted sum of row, column weights:",
              round(sum(kw$weights.row), 2), ",",
              round(sum(kw$weights.col), 2) ), quote=F)
  print("Summary of weighted naive statistics", quote=F)
  print(paste(
    "Overall accuracy, stdev, CV%:",
    round(kw$sum.naive, 4), ",", round(sqrt(kw$sum.var), 4), ",",
    round((sqrt(kw$sum.var)/kw$sum.naive)*1000,0)/10),
    quote=F)
  w<-ciw(kw$sum.var, kw$sum.n)
  print(paste(
    round((1-alpha)*100,0),"% confidence limits for accuracy:",
    round((kw$sum.naive-w),4), "...",
    round((kw$sum.naive+w),4), sep=""), quote=F)
  print("User’s weighted accuracy", quote=F)
  print(round(kw$user.wa,4));
  print("Producer’s weighted reliability:", quote=F)
  print(round(kw$prod.wa,4));
  print("Summary of weighted kappa statistics", quote=F)
  print(paste("Overall weighted kappa, stdev, & CV%:",
              round(kw$sum.kappa,4), ",",
              round(sqrt(kw$sum.kvar),4), ",",
              round((sqrt(kw$sum.kvar)/kw$sum.kappa)*1000,0)/10), quote=F)
  w<-ciw(kw$sum.kvar, kw$sum.n)
  print(paste(
    round((1-alpha)*100,0),"% confidence limits for weighted kappa:",
    round((kw$sum.kappa-w),4), "...",
    round((kw$sum.kappa+w),4), sep=""), quote=F)
}

uci.binomial <- function(x,n,a=0,b=1,alpha=.05){
  # Finds the exact upper confidence limit for binomial count
  # R v0.90.1, Tomas Aragon, created 01/01/2000, edited
  # x = observed count (non-negative integer)
  # n = number of Bernoulli trials
  # a = default lower bound for bisection method
  # b = default upper bound for bisection method
  # alpha = .05 (default), for 95% confidence interval
  # This function is used by ci.binomial()
  #
  if(x==0) answer <- 1-(alpha)^(1/n)
  else{
    answer <- (a+b)/2
    if(abs(a-b) >= 0.00000001){
      lefta <- pbinom(x,n,a)-alpha/2
      centera <- pbinom(x,n,answer)-alpha/2
      righta <- pbinom(x,n,b)-alpha/2
      if(lefta*centera < 0){
        answer <- uci.binomial(x,n,a,answer)
      }
      else{
        answer <- uci.binomial(x,n,answer,b)
      }
    }
  }
  answer
}

lci.binomial <- function(x,n,a=0,b=1,alpha=.05){
  # Finds the exact lower confidence limit for binomial count
  # R v0.90.1, Tomas Aragon, created 01/01/2000, edited
  # x = observed count (non-negative integer)
  # n = number of Bernoulli trials
  # a = default lower bound for bisection method
  # b = default upper bound for bisection method
  # alpha = .05 (default), for 95% confidence interval
  # This function is used by ci.binomial()
  #
  if(x==0) answer <- 0
  else{
    answer <- (a+b)/2
    if(abs(a-b) >= 0.00000001){
      lefta <- 1-pbinom(x,n,a)+dbinom(x,n,a)-alpha/2
      centera <- 1-pbinom(x,n,answer)+dbinom(x,n,answer)-alpha/2
      righta <- 1-pbinom(x,n,b)+dbinom(x,n,b)-alpha/2
      if(lefta*centera < 0){
        answer <- lci.binomial(x,n,a,answer)
      }
      else{
        answer <- lci.binomial(x,n,answer,b)
      }
    }
  }
  answer
}

ci.binomial <- function(x,n,alpha=0.05){
  # Finds the exact 95% confidence interval for binomial count
  # R v0.90.1, Tomas Aragon, created 01/01/2000, edited
  # x = observed count (non-negative integer)
  # number of Bernoulli trials
  # alpha = .05 (default), for 95% confidence interval
  # Output includes x and CIs, and p=x/n and CIs
  # Uses lci.binomial() and uci.binomial() for bisection method
  #
  xn <- cbind(x,n)
  ci.pois <- function(xx,aa=alpha){
    lci <- lci.binomial(xx[1],xx[2],alpha=aa)
    uci <- uci.binomial(xx[1],xx[2],alpha=aa)
    c(lci=lci,uci=uci)
  }
  prob <- t(apply(xn,1,ci.pois))
  ans <- cbind(as.vector(x),n,prob*n,as.vector(x/n),prob)
  dimnames(ans)[[2]] <- c("x","n","x.lci","x.uci","p=x/n","p.lci","p.uci")
  ans
}

#Cramers V
Cramer <- function(CM) {
  chisq <- chisq.test(CM)$statistic
  nr=nrow(CM);  nc=ncol(CM) ;n=sum(CM)
  denom=n*(min(nr,nc)-1)
  V=sqrt(chisq/denom)
  return(unname(V))
}


landformdistribution_newlegend <- function(modeldata,dependent,predictor,legend,predlegend,predlegendtextcol){
  modeldata_new <- merge(modeldata,legend)
  dependent_new <- names(legend)[1]
  modeldata_new[[dependent_new]] <-droplevels(modeldata_new[[dependent_new]]) 
  mymodeldata <- modeldata_new[c(dependent_new,predictor)]
  f <- paste(dependent_new,"~.")
  fit <- do.call("svm",list(as.formula(f),mymodeldata,cross=10,kernel="radial"))
  mymodeldatana <- na.omit(mymodeldata)
  mymodeldatana$preds <- predict(fit,mymodeldata)
  mymodeldatana <- merge(mymodeldatana,predlegend,by.x=as.character(predictor),by.y="code",all.x=T)
  correct <- mean(mymodeldatana[[dependent_new]] == predict(fit,mymodeldatana))
  table_df <- as.data.frame.matrix(table(mymodeldatana[[predlegendtextcol]],mymodeldatana$preds))
  table_df$rowsum <-apply(table_df,1,sum)
  table_df <- rbind(table_df, apply(table_df,2,sum))
  print(table_df)
}

evaluatepredictors_radial_newlegend <- function(modeldata,dependent,predictors,legend=makrolegend_gen1){
  require(e1071)
  modeldata_new <- merge(modeldata,legend,all.x=T)
  dependent_new <- names(legend)[1]
  modeldata_new[[dependent_new]] <-droplevels(modeldata_new[[dependent_new]]) 
  mymodeldata <- modeldata_new[c(dependent_new,predictors)]
  f <- paste(dependent_new,"~.")
  fit <- do.call("svm",list(as.formula(f),mymodeldata,cross=10,kernel="radial"))
  cverror = 1-(fit$tot.accuracy)/100
  print(paste("10fold cv-error: ",cverror," for predictors",paste(predictors,collapse=" AND ")))
  mymodeldatana <- na.omit(mymodeldata)
  mymodeldatana$preds <- predict(fit,mymodeldata)
  contable <- table(mymodeldatana[[dependent_new]],predict(fit,mymodeldatana))
  table_df <- as.data.frame.matrix(contable)
  table_df$rowsum <-apply(table_df,1,sum)
  table_df <- rbind(table_df, apply(table_df,2,sum))
  print(table_df)
  print(paste("difference in points = ",nrow(mymodeldata) - nrow(mymodeldatana)))
}


perclasserror_newlegend <- function(modeldata,dependent,predictorlist,legend){
  require(xtable)
  modeldata_new <- merge(modeldata,legend,all.x=T)
  dependent_new <- names(legend)[1]
  modeldata_new[[dependent_new]] <-droplevels(modeldata_new[[dependent_new]]) 
  percentagetable <- data.frame(c(levels(modeldata_new[[dependent_new]]),"overall classification rate"))
  names(percentagetable) <- "topographic positions"
  for(p in predictorlist){
    predictors <- unlist(p)
    mymodeldata <- modeldata_new[c(dependent_new,predictors)]
    f <- paste(dependent_new,"~.")
    fit <- do.call("svm",list(as.formula(f),mymodeldata,cross=10,kernel="radial"))
    mymodeldatana <- na.omit(mymodeldata)
    mymodeldatana$preds <- predict(fit,mymodeldatana)
    correct <- mean(mymodeldatana[[dependent_new]] == predict(fit,mymodeldatana))
    
    contable <- table(mymodeldatana[[dependent_new]],predict(fit,mymodeldatana))
    colsums <- apply(contable,2,sum)
    rowsums <- apply(contable,1,sum)  
    for (i in 1:nrow(contable)){
      percentagetable[i,paste(predictors,collapse=" & ")] <- contable[i,i]/rowsums[i]
    }
    percentagetable[i+1,paste(predictors,collapse=" & ")] <- correct
  }
  newtable <- as.data.frame(t(percentagetable))
  names(newtable) <- percentagetable[,1]
  newtable <- newtable[-1,]
  for(nn in names(newtable)){
    newtable[[nn]] <- as.numeric(as.character(newtable[[nn]])) 
  }
  #print(newtable)
  print(xtable(newtable,digits = 2,row.names=FALSE))
}

predict_radial_newlegend <- function(modeldata,dependent,predictors,legend,doreturn=TRUE){
  require(e1071)
  modeldata_new <- merge(modeldata,legend,all.x=T)
  dependent_new <- names(legend)[1]
  modeldata_new[[dependent_new]] <-droplevels(modeldata_new[[dependent_new]]) 
  mymodeldata <- modeldata_new[c(dependent_new,predictors)]
  f <- paste(dependent_new,"~.")
  fit <- do.call("svm",list(as.formula(f),mymodeldata,cross=10,kernel="radial"))
  cverror = 1-(fit$tot.accuracy)/100
  print(paste("10fold cv-error: ",cverror," for predictors",paste(predictors,collapse=" AND ")))
  mymodeldatana <- na.omit(mymodeldata)
  preds <- predict(fit,mymodeldata)
  CM <- table(mymodeldata[[dependent_new]],preds)
  print(CM)
  summary.kappa(kappa(CM))
  print(paste("#########  Cramer's V = ",Cramer(CM)))
  if(doreturn) {  return(preds)}
}

predict_radial_newlegend_is.correct <- function(modeldata,dependent,predictors,legend,doreturn=TRUE,shortpred="fz"){
  require(e1071)
  modeldata_new <- merge(modeldata,legend,all.x=T)
  dependent_new <- names(legend)[1]
  modeldata_new[[dependent_new]] <-droplevels(modeldata_new[[dependent_new]]) 
  mymodeldata <- modeldata_new[c(dependent_new,predictors)]
  f <- paste(dependent_new,"~.")
  fit <- do.call("svm",list(as.formula(f),mymodeldata,cross=10,kernel="radial"))
  cverror = 1-(fit$tot.accuracy)/100
  print(paste("10fold cv-error: ",cverror," for predictors",paste(predictors,collapse=" AND ")))
  colname=paste("p",shortpred,dependent_new,collapse="_",sep="_")
  colname2=paste("c",shortpred,dependent_new,collapse="_",sep="_")
  modeldata_new[[colname]] <- predict(fit,mymodeldata)
  modeldata_new[[colname2]] <- ifelse(modeldata_new[[dependent_new]] == modeldata_new[[colname]],1,0)
  CM <- table(modeldata_new[[dependent_new]],modeldata_new[[colname]])
  print(CM)
  summary.kappa(kappa(CM))
  print(paste("#########  Cramer's V = ",Cramer(CM)))
  if(doreturn) {  return(modeldata_new[c("AufID",colname,colname2)])}
}


#modeldata=fuzzy_defredmak;dependent="Def_red_mak";predictors=fuzzy_makro_gl1;legend=makrolegend_gen1;input="/home/fabs/Data/paper1_lenny/GIS/paper_10m_comparisons_aoijan2/Fuzzylandforms_macro_GL1_6t012deg_R9803_R333_from150m.tif";outname="fuzzy"
predict_radial_newlegend_newmap <- function(modeldata,dependent,predictors,legend,doreturn=FALSE,outname,input){
  require(e1071)
  require(rgdal)
  modeldata_new <- merge(modeldata,legend,all.x=T)
  dependent_new <- names(legend)[1]
  modeldata_new[[dependent_new]] <-droplevels(modeldata_new[[dependent_new]]) 
  mymodeldata <- modeldata_new[c(dependent_new,predictors)]
  f <- paste(dependent_new,"~.")
  fit <- do.call("svm",list(as.formula(f),mymodeldata,cross=10,kernel="radial"))
  modellevels <- levels(mymodeldata[[predictors]])
  oldrast <- readGDAL(input)
  newdata <-oldrast@data
  newdata$UID <- 1:nrow(newdata)
  newdata[[predictors]] <-as.factor(newdata$band1) 
 newlevels <- levels(newdata[[predictors]])
 bothlevels<- modellevels[modellevels %in% newlevels]
 predictdata<- newdata[newdata[[predictors]] %in% bothlevels,]
 predictdata<-droplevels(predictdata)
 predictdata$pred <- predict(fit,predictdata)
 predictdata <- merge(predictdata,legend,by.x="pred",by.y=dependent_new,all.x=TRUE)
 newdata <- merge(newdata,predictdata,by="UID",all.x=T)
 newdata <- newdata[order(newdata$UID,decreasing = F),]
 oldrast@data <- newdata
 writeGDAL(oldrast["code"],fname=paste(outname,"_",dependent,".tif",sep=""))
 cverror = 1-(fit$tot.accuracy)/100
  print(paste("10fold cv-error: ",cverror," for predictors",paste(predictors,collapse=" AND ")))
  preds <- predict(fit,mymodeldata)
  CM <- table(mymodeldata[[dependent_new]],preds)
  print(CM)
  summary.kappa(kappa(CM))
  print(paste("#########  Cramer's V = ",Cramer(CM)))
  if(doreturn) {  return(preds)}
}

#modeldata=terrain_defredmak;dependent="Def_red_mak";predictors=terrain_makro_gl1;legend=makrolegend_gen1;input=list("/home/fabs/Data/paper1_lenny/GIS/paper_10m_comparisons_aoijan2/Topographic_Wetness_Index_50m.tif","/home/fabs/Data/paper1_lenny/GIS/paper_10m_comparisons_aoijan2/profc_50m_ws350m_from50m.tif","/home/fabs/Data/paper1_lenny/GIS/paper_10m_comparisons_aoijan2/minic_50m_ws250m_from50m.tif");outname="terrain"
predict_radial_newlegend_newmap_multiprednumeric <- function(modeldata,dependent,predictors,legend,doreturn=FALSE,outname,input){
  require(e1071)
  require(rgdal)
  modeldata_new <- merge(modeldata,legend,all.x=T)
  dependent_new <- names(legend)[1]
  modeldata_new[[dependent_new]] <-droplevels(modeldata_new[[dependent_new]]) 
  mymodeldata <- modeldata_new[c(dependent_new,predictors)]
  f <- paste(dependent_new,"~.")
  fit <- do.call("svm",list(as.formula(f),mymodeldata,cross=10,kernel="radial"))
rastdata <- readGDAL(input[1])
rastdata$UID <- 1:nrow(rastdata)
newdata<-as.data.frame(rastdata$UID)
names(newdata) <- c("UID")
npred=1
  for(i in input){
    tmprst <-readGDAL(i)
  newdata[[predictors[npred]]] <- tmprst@data$band1
  npred=npred+1
  }
  predictdata<-newdata
  predictdata$pred <- predict(fit,predictdata)
  predictdata <- merge(predictdata,legend,by.x="pred",by.y=dependent_new,all.x=TRUE)
  predictdata <- predictdata[order(predictdata$UID,decreasing = F),]
rastdata@data <- predictdata
  writeGDAL(rastdata["code"],fname=paste(outname,"_",dependent,".tif",sep=""))
  cverror = 1-(fit$tot.accuracy)/100
  print(paste("10fold cv-error: ",cverror," for predictors",paste(predictors,collapse=" AND ")))
  preds <- predict(fit,mymodeldata)
  CM <- table(mymodeldata[[dependent_new]],preds)
  print(CM)
  summary.kappa(kappa(CM))
  print(paste("#########  Cramer's V = ",Cramer(CM)))
  if(doreturn) {  return(preds)}
}


###########################################################################################################################################################
evaluateforwardCV <-function(mypath, kk=1:10, endround=5, type="rf",yrange=c(0.35,0.70),...) {
  if(type == "rf"){
    xrange <- c(1,endround)
    plot(xrange,yrange,type="n",xlab="steps",ylab="prediction-error")
    colors=rainbow(max(kk))
    vcpreds <- data.frame(1:endround)
    for (k in kk){
      load(file=paste(mypath,"/k",k,"_round_",endround,".RData",sep=""))
      lines(result_df$tt,result_df$OOB,type="b",col=colors[k])
      #lines(result_df$tt,result_df$geheimerprederror,col="black")
      vcpreds[[k]] <- result_df$geheimerprederror
    }
    vcpreds["meanprederror"] <- apply(vcpreds,1,mean,na.rm=TRUE)
    vcpreds["standard.error"] <- apply(vcpreds,1,function(x) { sd(x,na.rm = TRUE)/sqrt(sum(!is.na(x)))})
    vcpreds["se_lower"] <- vcpreds$meanprederror - 2*vcpreds$standard.error
    vcpreds["se_upper"] <- vcpreds$meanprederror + 2*vcpreds$standard.error
    cn <- ncol(vcpreds)
    lines(1:endround,vcpreds$meanprederror,type="b",col="red",lwd=3)
    matlines(vcpreds[,cn-1:cn],col="black",lty=2)
    one.se.rule <- vcpreds[vcpreds$meanprederror==min(vcpreds$meanprederror),"standard.error"] + min(vcpreds$meanprederror)
    abline(h=one.se.rule,lwd=3)
    predictor_df <- data.frame(row.names=1:endround)
    allchosen <- vector()
    for (k in kk){
      kname=paste("k",k)
      load(file=paste(mypath,"/k",k,"_round_",endround,".RData",sep=""))
      predictor_df[[kname]]<- result_df$metric
      allchosen <-c(allchosen,as.character(result_df$metric))
    }
    print(predictor_df)
    #print(table(allchosen))
    chosen_df <- as.data.frame(table(allchosen))
    chosen_df <- chosen_df[chosen_df$Freq > 1,]
    chosen_df <- chosen_df[order(chosen_df$Freq,decreasing = TRUE),]
    return(chosen_df)
    par(mar=c(10,2,2,2))
    barplot(height=chosen_df$Freq,names.arg = chosen_df$allchosen,las=2,cex.names=0.6)
  }else if(type== "svm"){
    xrange <- c(1,endround)
    yrange=yrange
    plot(xrange,yrange,type="n",xlab="steps",ylab="prediction-error")
    colors=rainbow(max(kk))
    vcpreds <- data.frame(1:endround)
    for (k in kk){
      load(file=paste(mypath,"/k",k,"_round_",endround,".RData",sep=""))
      lines(result_df$tt,result_df$cv,type="b",col=colors[k])
      lines(result_df$tt,result_df$OOB,type="b",col=colors[k])
      #lines(result_df$tt,result_df$geheimerprederror,col="black",lty=2)
      vcpreds[[k]] <- result_df$geheimerprederror
    }
    vcpreds["meanprederror"] <- apply(vcpreds,1,mean,na.rm=TRUE)
    vcpreds["standard.error"] <- apply(vcpreds,1,function(x) { sd(x,na.rm = TRUE)/sqrt(sum(!is.na(x)))})
    vcpreds["se_lower"] <- vcpreds$meanprederror - 2*vcpreds$standard.error
    vcpreds["se_upper"] <- vcpreds$meanprederror + 2*vcpreds$standard.error
    cn <- ncol(vcpreds)
    lines(1:endround,vcpreds$meanprederror,type="b",col="red",lwd=3)
    matlines(vcpreds[,cn-1:cn],col="black",lty=2)
    one.se.rule <- vcpreds[vcpreds$meanprederror==min(vcpreds$meanprederror),"standard.error"] + min(vcpreds$meanprederror)
    abline(h=one.se.rule,lwd=3)
    predictor_df <- data.frame(row.names=1:endround)
    allchosen <- vector()
    for (k in kk){
      kname=paste("k",k)
      load(file=paste(mypath,"/k",k,"_round_",endround,".RData",sep=""))
      predictor_df[[kname]]<- result_df$metric
      allchosen <-c(allchosen,as.character(result_df$metric))
    }
    print(predictor_df)
    #as.data.frame(table(allchosen))[order(as.data.frame(table(allchosen))$Freq,decreasing=TRUE),]
    #print(table(allchosen))
    chosen_df <- as.data.frame(table(allchosen))
    chosen_df <- chosen_df[chosen_df$Freq > 1,]
    chosen_df <- chosen_df[order(chosen_df$Freq,decreasing = TRUE),]
    par(mar=c(10,2,2,2))
    barplot(height=chosen_df$Freq,names.arg = chosen_df$allchosen,las=2,cex.names=0.6)
    return(chosen_df)
  }
}


predict_radial_newlegend_truekappa <- function(modeldata,dependent,predictors,legend){
  require(e1071)
  modeldata_new <- merge(modeldata,legend,all.x=T)
  dependent_new <- names(legend)[1]
  modeldata_new[[dependent_new]] <-droplevels(modeldata_new[[dependent_new]]) 
  mymodeldata <- modeldata_new[c(dependent_new,predictors)]
  f <- paste(dependent_new,"~.")
  fit <- do.call("svm",list(as.formula(f),mymodeldata,cross=10,kernel="radial"))
  cverror = 1-(fit$tot.accuracy)/100
  print(paste("10fold cv-error: ",cverror," for predictors",paste(predictors,collapse=" AND ")))
  mymodeldatana <- na.omit(mymodeldata)
  preds <- predict(fit,mymodeldata)
  CM <- table(preds,mymodeldata[[dependent_new]])
  print(CM)
  summary.kappa(kappa(CM))
  summary.tau(tau(CM))
  print(paste("#########  Cramer's V = ",Cramer(CM)))
  return(preds)
}

quality <- function(CM){
  #convert both data frames and vectors to matrices
  cmx<-as.matrix(CM)
  #try to convert a vector to a square matrix
  if (ncol(cmx) == 1)
    cmx<-matrix(cmx, byrow=TRUE, nrow=sqrt(nrow(cmx)))
  nr<-nrow(cmx); nc<-ncol(cmx)
  if (nr != nc)
  { print("Error: matrix is not square"); return(NULL) }
  n<-sum(cmx)
  d<-diag(cmx)
  dsum<-sum(d)
  th1<-dsum/n # also die correct rate ist th1, 
  csum<-apply(cmx,2,sum)
  rsum<-apply(cmx,1,sum)
  ua<-d/rsum 
  pa<-d/csum
  ua_adj <- 
    quality <- (ua*pa)/(ua+pa-pa*ua)
  mean_quality <- sum(quality,na.rm = T)/length(quality)
  print(paste("mean quality = ",mean_quality))
  return(mean_quality)
}

predict_ranfor_full <- function(modeldata,dependent,predictors,doreturn=FALSE, kappasum=FALSE,tausum=FALSE,pset,altdata){
  require(randomForest)
  fullmodel <- randomForest(as.formula(paste(dependent,"~.")),na.omit(modeldata[c(dependent,paramsets[[pset]])]))
print(paste("OBB error with all predictors of ",paramsetnames[pset], "is ",fullmodel$err.rate[nrow(fullmodel$err.rate),1]))
  mymodeldata <- modeldata[c(dependent,predictors)]
  f <- paste(dependent,"~.")
  fit <- do.call("randomForest",list(as.formula(f),mymodeldata))
  cverror =  fit$err.rate[nrow(fit$err.rate),1]
  print(paste("OOB-error: ",cverror," for predictors",paste(predictors,collapse=" AND ")))
  preddata <- mymodeldata[,!names(mymodeldata)%in% c(dependent)]
  preds <- predict(fit,preddata)
  CM <- table(preds,mymodeldata[[dependent]])
  print(CM)
  print(paste("Kappa overall = ",kappa(CM)$sum.kappa))
  if(kappasum==T) print(summary.kappa(kappa(CM)))
  print(paste("Tau overall = ",tau(CM)$tau))
  if(tausum == T) print(summary.tau(tau(CM)))
  print(paste("The quality is ",quality(CM)))
  print(paste("#########  Cramer's V = ",Cramer(CM)))
  if(doreturn==TRUE) return(preds)
   altmodeldata <- na.omit(altdata[c(dependent,predictors)])
  altpreddata<-altmodeldata[predictors]
  altpreds <- predict(fit,altpreddata)
  ACM <- table(altpreds, altmodeldata[[dependent]])
  print(paste("classification error rate with altdata: ",mean(altpreds != altmodeldata[[dependent]])))
}

predict_ranfor_newlegend_full <- function(modeldata,dependent,predictors,doreturn=FALSE, kappasum=FALSE,tausum=FALSE,altdata,legend){
  require(randomForest)
 modeldata_new <- merge(modeldata,legend,all.x=T)
  dependent_new <- names(legend)[1]
  modeldata_new[[dependent_new]] <-droplevels(modeldata_new[[dependent_new]]) 
  mymodeldata <- modeldata_new[c(dependent_new,predictors)]
  f <- paste(dependent_new,"~.")
  fit <- do.call("randomForest",list(as.formula(f),mymodeldata))
  cverror =  fit$err.rate[nrow(fit$err.rate),1]
  print(paste("OOB-error: ",cverror," for predictors",paste(predictors,collapse=" AND ")))
  preddata <- mymodeldata[,!names(mymodeldata)%in% c(dependent_new,dependent)]
  preds <- predict(fit,preddata)
  CM <- table(preds,mymodeldata[[dependent_new]])
  #print(CM)
  print(paste("Kappa overall = ",kappa(CM)$sum.kappa))
  if(kappasum==T) print(summary.kappa(kappa(CM)))
  print(paste("Tau overall = ",tau(CM)$tau))
  if(tausum == T) print(summary.tau(tau(CM)))
  print(paste("The quality is ",quality(CM)))
  print(paste("#########  Cramer's V = ",Cramer(CM)))
  if(doreturn==TRUE) return(preds)
  altdataleg <- merge(altdata,legend,all.x=T)
   altmodeldata <- na.omit(altdataleg[c(dependent_new,predictors)])
  altpreddata<-altmodeldata[predictors]
  altpreds <- predict(fit,altpreddata)
  ACM <- table(altpreds, altmodeldata[[dependent_new]])
  print(ACM)
  print(paste("classification error rate with altdata: ",mean(altpreds != altmodeldata[[dependent_new]])))
  print(paste("Kappa overall = ",kappa(ACM)$sum.kappa))
  if(kappasum==T) print(summary.kappa(kappa(ACM)))
  print(paste("Tau overall = ",tau(ACM)$tau))
  if(tausum == T) print(summary.tau(tau(ACM)))
  print(paste("The quality is ",quality(ACM)))
  print(paste("#########  Cramer's V = ",Cramer(ACM)))
}

predict_ranfor_newlegend_full_naproblem <- function(modeldata,dependent,predictors,doreturn=FALSE, kappasum=FALSE,tausum=FALSE,altdata,legend){
  require(randomForest)
 modeldata_new <- merge(modeldata,legend,all.x=T)
  dependent_new <- names(legend)[1]
  modeldata_new[[dependent_new]] <-droplevels(modeldata_new[[dependent_new]]) 
  mymodeldata <- modeldata_new[c(dependent_new,predictors)]
  mymodeldata <- na.omit(mymodeldata)
  f <- paste(dependent_new,"~.")
  fit <- do.call("randomForest",list(as.formula(f),mymodeldata))
  cverror =  fit$err.rate[nrow(fit$err.rate),1]
  print(paste("OOB-error: ",cverror," for predictors",paste(predictors,collapse=" AND ")))
  preddata <- mymodeldata[,!names(mymodeldata)%in% c(dependent_new,dependent)]
  preds <- predict(fit,preddata)
  CM <- table(preds,mymodeldata[[dependent_new]])
  #print(CM)
  print(paste("Kappa overall = ",kappa(CM)$sum.kappa))
  if(kappasum==T) print(summary.kappa(kappa(CM)))
  print(paste("Tau overall = ",tau(CM)$tau))
  if(tausum == T) print(summary.tau(tau(CM)))
  print(paste("The quality is ",quality(CM)))
  print(paste("#########  Cramer's V = ",Cramer(CM)))
  if(doreturn==TRUE) return(preds)
  altdataleg <- merge(altdata,legend,all.x=T)
   altmodeldata <- na.omit(altdataleg[c(dependent_new,predictors)])
  altpreddata<-altmodeldata[predictors]
  altpreds <- predict(fit,altpreddata)
  ACM <- table(altpreds, altmodeldata[[dependent_new]])
  print(ACM)
  print(paste("classification error rate with altdata: ",mean(altpreds != altmodeldata[[dependent_new]])))
  print(paste("Kappa overall = ",kappa(ACM)$sum.kappa))
  if(kappasum==T) print(summary.kappa(kappa(ACM)))
  print(paste("Tau overall = ",tau(ACM)$tau))
  if(tausum == T) print(summary.tau(tau(ACM)))
  print(paste("The quality is ",quality(ACM)))
  print(paste("#########  Cramer's V = ",Cramer(ACM)))
}

predict_radial_newlegend_fullparamset <- function(modeldata,dependent,pset,altdata,legend){
  require(e1071)
  modeldata_new <- merge(modeldata,legend,all.x=T)
  dependent_new <- names(legend)[1]
  modeldata_new[[dependent_new]] <-droplevels(modeldata_new[[dependent_new]]) 
  modeldata <- modeldata_new
  fullmodel <- svm(as.formula(paste(dependent_new,"~.")),na.omit(modeldata[c(dependent_new,paramsets[[pset]])]),cross=10)
  print(paste("10fold cv-error with all predictors of ",paramsetnames[pset], "is ",1-(fullmodel$tot.accuracy)/100))
  altdata <- merge(altdata,legend,all.x=T)
  altmodeldata <- na.omit(altdata[c(dependent_new,paramsets[[pset]])])
  altpreddata<-altmodeldata[paramsets[[pset]]]
  altpreds <- predict(fullmodel,altpreddata)
  ACM <- table(altpreds, altmodeldata[[dependent_new]])
  print(ACM)
  print(paste("classification error rate with altdata: ",mean(altpreds != altmodeldata[[dependent_new]])))
}

importance_ranfor_pset <- function(modeldata,dependent,pset,altdata){
  require(randomForest)
  fullmodel <- randomForest(as.formula(paste(dependent,"~.")),na.omit(modeldata[c(dependent,paramsets[[pset]])]))
print(paste("OBB error with all predictors of ",paramsetnames[pset], "is ",fullmodel$err.rate[nrow(fullmodel$err.rate),1]))
 importance <- as.data.frame(fullmodel$importance)
importance$parameters <- row.names(importance)
importance <- importance[order(importance$MeanDecreaseGini,decreasing = T),]
print(importance[1:10,])
  altmodeldata <- na.omit(altdata[c(dependent,paramsets[[pset]])])
  altpreddata<-altmodeldata[paramsets[[pset]]]
  altpreds <- predict(fullmodel,altpreddata)
  ACM <- table(altpreds, altmodeldata[[dependent]])
  print(ACM)
  print(paste("classification error rate with altdata: ",mean(altpreds != altmodeldata[[dependent]])))
}

importance_ranfor_pset_newlegend <- function(modeldata,dependent,pset,altdata,legend){
  require(randomForest)
  modeldata_new <- merge(modeldata,legend,all.x=T)
  dependent_new <- names(legend)[1]
  modeldata_new[[dependent_new]] <-droplevels(modeldata_new[[dependent_new]]) 
  modeldata <- modeldata_new
  fullmodel <- randomForest(as.formula(paste(dependent_new,"~.")),na.omit(modeldata[c(dependent_new,paramsets[[pset]])]))
print(paste("OBB error with all predictors of ",paramsetnames[pset], "is ",fullmodel$err.rate[nrow(fullmodel$err.rate),1]))
 importance <- as.data.frame(fullmodel$importance)
importance$parameters <- row.names(importance)
importance <- importance[order(importance$MeanDecreaseGini,decreasing = T),]
print(importance[1:10,])
altdata <- merge(altdata,legend,all.x=T)
  altmodeldata <- na.omit(altdata[c(dependent_new,paramsets[[pset]])])
  altpreddata<-altmodeldata[paramsets[[pset]]]
  altpreds <- predict(fullmodel,altpreddata)
  ACM <- table(altpreds, altmodeldata[[dependent_new]])
  print(ACM)
  print(paste("classification error rate with altdata: ",mean(altpreds != altmodeldata[[dependent_new]])))
}

increaseacc <- function(modeldata,dependent,pset){
  require(randomForest)
  require(knitr)
  fullmodel <- randomForest(as.formula(paste(dependent,"~.")),na.omit(modeldata[c(dependent,paramsets[[pset]])]),importance=TRUE)
  print(paste("OBB error with all predictors of ",paramsetnames[pset], "is ",fullmodel$err.rate[nrow(fullmodel$err.rate),1]))
  importance <- as.data.frame(fullmodel$importance)
  importance <- importance[order(importance$MeanDecreaseAccuracy,decreasing = T),]
  print(kable(as.data.frame(importance[1:10,])))
  return(importance[1:10,])
   cat('\n')
}


predict_radial_full <- function(modeldata,dependent,predictors,doreturn=FALSE,kappasum=FALSE,tausum=FALSE){
  require(e1071)
  mymodeldata <- modeldata[c(dependent,predictors)]
  f <- paste(dependent,"~.")
  fit <- do.call("svm",list(as.formula(f),mymodeldata,cross=10,kernel="radial"))
  cverror = 1-(fit$tot.accuracy)/100
  print(paste("10fold cv-error: ",cverror," for predictors",paste(predictors,collapse=" AND ")))
  preds <- predict(fit,mymodeldata)
  CM <- table(preds,mymodeldata[[dependent]])
  print(CM)
  print(paste("Kappa overall = ",kappa(CM)$sum.kappa))
  if(kappasum==T) print(summary.kappa(kappa(CM)))
  print(paste("Tau overall = ",tau(CM)$tau))
  if(tausum == T) print(summary.tau(tau(CM)))
  print(paste("The quality is ",quality(CM)))
  print(paste("#########  Cramer's V = ",Cramer(CM)))
  if(doreturn==TRUE) return(preds)
}

predict_radial_newlegend_full <- function(modeldata,dependent,predictors,legend,doreturn=FALSE,kappasum=FALSE,tausum=FALSE,alttest=TRUE,altdata){
  require(e1071)
  modeldata_new <- merge(modeldata,legend,all.x=T)
  dependent_new <- names(legend)[1]
  modeldata_new[[dependent_new]] <-droplevels(modeldata_new[[dependent_new]]) 
  mymodeldata <- modeldata_new[c(dependent_new,predictors)]
  f <- paste(dependent_new,"~.")
  fit <- do.call("svm",list(as.formula(f),mymodeldata,cross=10,kernel="radial"))
  cverror = 1-(fit$tot.accuracy)/100
  print(paste("10fold cv-error: ",cverror," for predictors",paste(predictors,collapse=" AND ")))
  mymodeldatana <- na.omit(mymodeldata)
  preds <- predict(fit,mymodeldata)
  CM <- table(preds,mymodeldata[[dependent_new]])
  print(CM)
  print(paste("Kappa overall = ",kappa(CM)$sum.kappa))
  if(kappasum==T) print(summary.kappa(kappa(CM)))
  print(paste("Tau overall = ",tau(CM)$tau))
  if(tausum == T) print(summary.tau(tau(CM)))
  print(paste("The quality is ",quality(CM)))
  print(paste("#########  Cramer's V = ",Cramer(CM)))
  if(doreturn==TRUE) return(preds)
    if(alttest==TRUE){
    altdata <- merge(altdata,legend,all.x=T)
    altmodeldata <- na.omit(altdata[c(dependent_new,predictors)])
    altpreddata<-altmodeldata[predictors]
    altpreds <- predict(fit,altpreddata)
    ACM <- table(altpreds, altmodeldata[[dependent_new]])
    print(ACM)
    print(paste("classification error rate with altdata: ",mean(altpreds != altmodeldata[[dependent_new]])))
    print(paste("Kappa overall = ",kappa(ACM)$sum.kappa))
    if(kappasum==T) print(summary.kappa(kappa(ACM)))
    print(paste("Tau overall = ",tau(ACM)$tau))
    if(tausum == T) print(summary.tau(tau(ACM)))
    print(paste("The quality is ",quality(ACM)))
    print(paste("#########  Cramer's V = ",Cramer(ACM)))
  }
}

predict_radial_newlegend_full_naproblem <- function(modeldata,dependent,predictors,legend,doreturn=FALSE,kappasum=FALSE,tausum=FALSE,alttest=TRUE,altdata){
  require(e1071)
  modeldata_new <- merge(modeldata,legend,all.x=T)
  dependent_new <- names(legend)[1]
  modeldata_new[[dependent_new]] <-droplevels(modeldata_new[[dependent_new]]) 
  mymodeldata <- modeldata_new[c(dependent_new,predictors)]
  f <- paste(dependent_new,"~.")
  fit <- do.call("svm",list(as.formula(f),mymodeldata,cross=10,kernel="radial"))
  cverror = 1-(fit$tot.accuracy)/100
  print(paste("10fold cv-error: ",cverror," for predictors",paste(predictors,collapse=" AND ")))
  mymodeldatana <- na.omit(mymodeldata)
  preds <- predict(fit,mymodeldatana)
  CM <- table(preds,mymodeldatana[[dependent_new]])
  print(CM)
  print(paste("Kappa overall = ",kappa(CM)$sum.kappa))
  if(kappasum==T) print(summary.kappa(kappa(CM)))
  print(paste("Tau overall = ",tau(CM)$tau))
  if(tausum == T) print(summary.tau(tau(CM)))
  print(paste("The quality is ",quality(CM)))
  print(paste("#########  Cramer's V = ",Cramer(CM)))
  if(doreturn==TRUE) return(preds)
    if(alttest==TRUE){
    altdata <- merge(altdata,legend,all.x=T)
    altmodeldata <- na.omit(altdata[c(dependent_new,predictors)])
    altpreddata<-altmodeldata[predictors]
    altpreds <- predict(fit,altpreddata)
    ACM <- table(altpreds, altmodeldata[[dependent_new]])
    print(ACM)
    print(paste("classification error rate with altdata: ",mean(altpreds != altmodeldata[[dependent_new]])))
    print(paste("Kappa overall = ",kappa(ACM)$sum.kappa))
    if(kappasum==T) print(summary.kappa(kappa(ACM)))
    print(paste("Tau overall = ",tau(ACM)$tau))
    if(tausum == T) print(summary.tau(tau(ACM)))
    print(paste("The quality is ",quality(ACM)))
    print(paste("#########  Cramer's V = ",Cramer(ACM)))
  }
}

Modus <- function(x) {
  ux <- unique(x)
  ux[which.max(tabulate(match(x, ux)))]
}

  evaluateforwardCV_anyerror <-function(mypath, kk=1:10, endround=5,yrange=c(0.35,0.70),error,geheim) {
    xrange <- c(1,endround)
    yrange=yrange
    plot(xrange,yrange,type="n",xlab="steps",ylab="prediction-error")
    colors=rainbow(max(kk))
    vcpreds <- data.frame(1:endround)
    for (k in kk){
      load(file=paste(mypath,"/k",k,"_round_",endround,".RData",sep=""))
      lines(result_df$tt,result_df[[error]],type="b",col=colors[k])
      vcpreds[[k]] <- result_df[[geheim]]
    }
    vcpreds["meanprederror"] <- apply(vcpreds,1,mean,na.rm=TRUE)
    vcpreds["standard.error"] <- apply(vcpreds,1,function(x) { sd(x,na.rm = TRUE)/sqrt(sum(!is.na(x)))})
    vcpreds["se_lower"] <- vcpreds$meanprederror - 2*vcpreds$standard.error
    vcpreds["se_upper"] <- vcpreds$meanprederror + 2*vcpreds$standard.error
    cn <- ncol(vcpreds)
    lines(1:endround,vcpreds$meanprederror,type="b",col="red",lwd=3)
    matlines(vcpreds[,cn-1:cn],col="black",lty=2)
    one.se.rule <- vcpreds[vcpreds$meanprederror==min(vcpreds$meanprederror),"standard.error"] + min(vcpreds$meanprederror)
    abline(h=one.se.rule,lwd=3)
    predictor_df <- data.frame(row.names=1:endround)
    allchosen <- vector()
    for (k in kk){
      kname=paste("k",k)
      load(file=paste(mypath,"/k",k,"_round_",endround,".RData",sep=""))
      predictor_df[[kname]]<- result_df$metric
      allchosen <-c(allchosen,as.character(result_df$metric))
    }
    print(paste("Prediction error at end is: ",vcpreds[,"meanprederror"]))
    print(predictor_df)
    #as.data.frame(table(allchosen))[order(as.data.frame(table(allchosen))$Freq,decreasing=TRUE),]
    #print(table(allchosen))
    chosen_df <- as.data.frame(table(allchosen))
    chosen_df <- chosen_df[chosen_df$Freq > 1,]
    chosen_df <- chosen_df[order(chosen_df$Freq,decreasing = TRUE),]
    if (nrow(chosen_df)>0){
    par(mar=c(10,2,2,2))
    barplot(height=chosen_df$Freq,names.arg = chosen_df$allchosen,las=2,cex.names=0.6)
      }
    return(chosen_df)
  }
predict_radial_newlegend_full_naomit <- function(modeldata,dependent,predictors,legend,doreturn=TRUE){
  require(e1071)
  modeldata_new <- merge(modeldata,legend,all.x=T)
  dependent_new <- names(legend)[1]
  modeldata_new[[dependent_new]] <-droplevels(modeldata_new[[dependent_new]]) 
  mymodeldata <- modeldata_new[c(dependent_new,predictors)]
  mymodeldata <- na.omit(mymodeldata)
  f <- paste(dependent_new,"~.")
  fit <- do.call("svm",list(as.formula(f),mymodeldata,cross=10,kernel="radial"))
  cverror = 1-(fit$tot.accuracy)/100
  print(paste("10fold cv-error: ",cverror," for predictors",paste(predictors,collapse=" AND ")))
  mymodeldatana <- na.omit(mymodeldata)
  preds <- predict(fit,mymodeldatana)
  CM <- table(preds,mymodeldatana[[dependent_new]])
  print(CM)
  summary.kappa(kappa(CM))
  summary.tau(tau(CM))
  print(paste("The quality of the modeled TP is ",quality(CM)))
  print(paste("#########  Cramer's V = ",Cramer(CM)))
  if(doreturn==TRUE) return(preds)
}

 sqliteGRASS_lenny <- function(location,mapset,vector){
 require(RSQLite)
 drv <- dbDriver("SQLite")
 grass <-"/home/fabs/Data/GRASSDATA/"
 con <- dbConnect(drv, dbname = paste(grass,location,"/",mapset,"/sqlite/sqlite.db",sep=""))
 statement= paste("SELECT * FROM '",as.character(vector),"'",sep="")
 df<- dbGetQuery(con,statement)
 return(df)
}

 sqliteGRASS_delilah <- function(location,mapset,vector){
 require(RSQLite)
 drv <- dbDriver("SQLite")
 grass <-"/media/fabs/Volume/Data/GRASSDATA/"
 con <- dbConnect(drv, dbname = paste(grass,location,"/",mapset,"/sqlite/sqlite.db",sep=""))
 statement= paste("SELECT * FROM '",as.character(vector),"'",sep="")
 df<- dbGetQuery(con,statement)
 return(df)
}
sqlite_df <- function(dbpath,vector){
  require(RSQLite)
  drv <- dbDriver("SQLite")
  con <- dbConnect(drv, dbname = dbpath)
  statement= paste("SELECT * FROM '",as.character(vector),"'",sep="")
  df<- dbGetQuery(con,statement)
  return(df)
}
