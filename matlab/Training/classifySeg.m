%classifySeg(diffNorm, hmmSeg, trueLabelFile)
%
% Parameters:
%   diffNorm = normalized difference between consecutive frames.
%   hmmSeg = hmm for segmentation
function classifySeg(diffNorm, hmmSeg, trueLabelFile, filteredIndices)
close all;
hold on;

[L, T] = size(diffNorm);
state  = ones(1, T);

% evaluate alpha (forward algo) and loglik one time frame a time
obslik = mixgauss_prob(diffNorm, hmmSeg.mu1, hmmSeg.Sigma1, hmmSeg.mixmat1);

% alpha(i,t) = p(Q(t)=i | y(1:t))
alpha = fwdback(hmmSeg.prior1, hmmSeg.transmat1, obslik, 'fwd_only', 1);

start = 0;
for t = 1 : T,
  if(alpha(2, t) > alpha(1, t))
    state(t) = 2;
    if (t==1 || state(t - 1) == 1)
      start = t;
    end
  else
    if ((t > 1 && state(t-1) == 2))
      stop = t - 1;
      plot([start stop], [2 2], 'b');
      plot([start, stop], [2, 2], 'bx');
    end
  end
end

if (state(T) == 2)
  plot([start T], [2 2], 'b');
  plot([start, T], [2, 2], 'bx');
end

%get true labels
[labels, startTimes, endTimes] = readTrueLabels(trueLabelFile, filteredIndices);
for i = 1 : length(labels),
    plot([startTimes(i) endTimes(i)], [hmmSeg.mu1(2) hmmSeg.mu1(2)], 'r');
    plot([startTimes(i), endTimes(i)], [hmmSeg.mu1(2), hmmSeg.mu1(2)], 'rx');
end

plot(1 : T, diffNorm, 'g');

end
