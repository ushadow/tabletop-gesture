%hmm = trainGestureSegmentaionHMM(diffNorm, trueLabelFile) training HMM
%for fully labeled data, for a single data sample
%
% inputs:
%		diffNorm = normalized difference between consecutive feature vector,
%		a row vector. If the observed data has length length m, then difference has
%		length m-1. so x_i = f_i - f_{i-1} 
function hmm = trainSegHmm(diffNorm, trueLabelFile)

Q = 2; %number of states
M = 1; %number of mixtures
FEATURE_LEN = 1;

m = length(diffNorm);

%get true labels
[labels, startTimes, endTimes] = textread(trueLabelFile, '%s %d %d');
numSegments = length(startTimes);

hmm.transmat1 = zeros(Q, Q);
hmm.mu1 = zeros(FEATURE_LEN, Q);

%Sigma1 is the covariance matrix
hmm.Sigma1 = zeros(FEATURE_LEN, FEATURE_LEN, Q);
hmm.mixmat1 = ones(Q,M);
hmm.prior1 = zeros(Q,1);

if(startTimes(1)==1)
	hmm.prior1(2) = 1;
end

hmm.prior1(1) = 1 - hmm.prior1(2);

mask = zeros(1, m);
for i = 1:numSegments,
	%mask is 1 when gesture is on (startTime ~ endTime - 1)
	%because the diffNorm vector has the first frame removed fromt the
	%original data, so there is a -1 term here. 
	mask((startTimes(i)-1) : (endTimes(i)-2)) = 1;
end

onTotal = sum(diffNorm(mask==1));
offTotal = sum(diffNorm(mask==0));

onCount = sum(mask);
offCount = m - onCount;

offOnCount = length(startTimes);
onOffCount = length(endTimes);

if(startTimes(1)==1)
	offOnCount = offOnCount - 1;
end

%state1 = gesture off, state2 = gesture on
hmm.transmat1(1,2) = offOnCount/offCount;
hmm.transmat1(1,1) = 1 - hmm.transmat1(1,2);
hmm.transmat1(2,1) = onOffCount/onCount;
hmm.transmat1(2,2) = 1 - hmm.transmat1(2,1);

hmm.mu1(1) = offTotal/offCount;
hmm.mu1(2) = onTotal/onCount;

hmm.Sigma1(1) = ((diffNorm(mask==0)-hmm.mu1(1))*(diffNorm(mask==0)-hmm.mu1(1))')/offCount;
hmm.Sigma1(2) = ((diffNorm(mask==1)-hmm.mu1(2))*(diffNorm(mask==1)-hmm.mu1(2))')/onCount;


