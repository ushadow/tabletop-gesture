%hmm = trainGestureSegmentaionHMM(diffNorm, trueLabelFile) training HMM
%for fully labeled data
%
% inputs:
%		diffNorms = cell array of normalized difference between consecutive feature vector,
%   a row vector. If the observed data has length length m, then difference has
%   length m-1. so x_i = f_i - f_{i-1} 
%   trueLabelFiles = cell array of true label file names corresponding to
%   the data in diffNorms 
function hmm = trainSegHmm2(diffNorms, trueLabelFiles, filteredIndices)

Q = 2; %number of states, state 1 = gesture off, state 2 = gesture on
M = 1; %number of mixtures
FEATURE_LEN = 1;

%initialize
hmm.transmat1 = zeros(Q, Q);
hmm.mu1 = zeros(FEATURE_LEN, Q);

%Sigma1 is the covariance matrix
hmm.Sigma1 = zeros(FEATURE_LEN, FEATURE_LEN, Q);
hmm.mixmat1 = ones(Q,M);
hmm.prior1 = zeros(Q,1);

countInit = zeros(Q,1);
onTotal = 0;
offTotal = 0;
onCountTotal = 0;
offCountTotal = 0;
offOnCountTotal = 0;
onOffCountTotal = 0;
onData = [];
offData = [];

n = length(diffNorms);

for j = 1 : n,
  
  diffNorm = diffNorms{j};
  m = length(diffNorm);

  %get true labels
  [labels, startTimes, endTimes] = readTrueLabels(trueLabelFiles{j}, filteredIndices{j});
  
  numSegments = length(startTimes);

  if (startTimes(1) == 1)
    countInit(2) = countInit(2) + 1;
  else countInit(1) = countInit(1) + 1;
  end
  
  mask = zeros(1, m);
  for i = 1 : numSegments,
    %mask is 1 when gesture is on (startTime ~ endTime - 1)
    %because the diffNorm vector has the first frame removed fromt the
    %original data, so there is a -1 term here. 
    if (startTimes(i) == 1)
      mask((startTimes(i)) : (endTimes(i) - 1)) = 1;
    else
      mask((startTimes(i) - 1) : (endTimes(i) - 2)) = 1;
    end
  end

  onData = [onData diffNorm(mask == 1)];
  offData = [offData diffNorm(mask == 0)];
  onTotal = onTotal + sum(diffNorm(mask == 1));
  offTotal = offTotal + sum(diffNorm(mask == 0));

  onCount = sum(mask);
  onCountTotal = onCountTotal + sum(mask);
  offCountTotal = offCountTotal + m - onCount; 

  offOnCountTotal = offOnCountTotal + length(startTimes);
  onOffCountTotal = onOffCountTotal + length(endTimes);

  if(startTimes(1)==1)
    offOnCount = offOnCount - 1;
  end
end

hmm.prior1 = countInit / n;
%state1 = gesture off, state2 = gesture on
hmm.transmat1(1, 2) = offOnCountTotal / offCountTotal;
hmm.transmat1(1, 1) = 1 - hmm.transmat1(1, 2);
hmm.transmat1(2, 1) = onOffCountTotal / onCountTotal;
hmm.transmat1(2, 2) = 1 - hmm.transmat1(2, 1);

hmm.mu1(1) = offTotal / offCountTotal;
hmm.mu1(2) = onTotal / onCountTotal;
hmm.Sigma1(1) = (offData - hmm.mu1(1)) * (offData - hmm.mu1(1))' / offCountTotal;
hmm.Sigma1(2) = (onData - hmm.mu1(2)) * (onData - hmm.mu1(2))' / onCountTotal;

end

