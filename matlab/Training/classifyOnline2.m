% classifyOnline online classification of gestures
%   classifyOline(data, hmm)
% Parameters:
%   data = a matrix, each column represents a streaming scaled feature vector
%   hmm = cell array of hmm models for different gestures
function [totalTrueGestures, recRate] = classifyOnline2(data, hmm, hmmSeg, trueLabelFiles, filteredIndices, xscale)
close all;

if (nargin < 6)
  xscale = 1;
end

totalTrueGestures = 0;
totalCorrect = 0;

n = length(data);
for i = 1 : n,
  figure(i);
  if (nargin >= 4)
    [numTrueGestures, numCorrect] = classifyOnlineSingle(data{i}, hmm, hmmSeg, trueLabelFiles{i}, filteredIndices{i}, xscale);
    totalTrueGestures = totalTrueGestures + numTrueGestures;
    totalCorrect = totalCorrect + numCorrect;
  else 
    classifyOnlineSingle(data{i}, hmm, hmmSeg);
  end
  
end

if (totalTrueGestures ~= 0)
  recRate = totalCorrect / totalTrueGestures;
end

end

function [numTrueGestures, numCorrect] = classifyOnlineSingle(data, hmm, hmmSeg, trueLabelFile, filteredIndices, xscale)

TOLERANCE = 5;
DIFF_THRESH = 12;
STYLES = {'b' 'g' 'r' 'c' 'm' 'k' 'b:' 'g:' 'r:' 'c:' 'm:' 'k:'};
GESTURE_NAMES = {'pan down', 'pan left', 'pan right', 'pan up', 'pitch anticlockwise', 'pitch clockwise', ...
  'roll anticlockwise', 'roll clockwise', 'yaw anticlockwise', 'yaw clockwise', 'zoom in', 'zoom out'};

[L, T] = size(data);
numGestures = length(hmm);
%number of states, the last state is the END state
Q = size(hmm{1}.mu1, 2);

alpha = zeros(Q, numGestures);
scale = zeros(1, numGestures);
state = zeros(1, numGestures);
prevState = zeros(1, numGestures);
pi = zeros(Q, numGestures);
loglik = zeros(T, numGestures);

gestureOn = 0;
currentGesture = zeros(T, 1);
obslik = cell(numGestures, 1);

for n = 1 : numGestures,
	%obslik{n}(i,t) = Pr(y(t) | Q(t)=i)
	obslik{n} = mixgauss_prob(data, hmm{n}.mu1, hmm{n}.Sigma1, hmm{n}.mixmat1);
	%obslik{n}(Q, :) = 1;
end

diffNorm = getNormDiff2(data);
obslikSeg = mixgauss_prob(diffNorm, hmmSeg.mu1, hmmSeg.Sigma1, hmmSeg.mixmat1);
alphaSeg = fwdback(hmmSeg.prior1, hmmSeg.transmat1, obslikSeg, 'fwd_only', 1);

% evaluate alpha (forward algo) and loglik one time frame a time
for t = 2 : T
  %diffNorm(t-1) corresponds to data(t)
	if (alphaSeg(2, t - 1) > alphaSeg(1, t - 1))
    %gesture on
    if(gestureOn == 0)
      %gesture just starts
      gestureOn = 1;

      for n = 1 : numGestures,
        alpha(:, n) = hmm{n}.prior1(:) .* obslik{n}(:, t);
        pi(:, n) = hmm{n}.prior1(:) .* obslik{n}(:, t);

        %[alpha(:,t), scale(t)] = normaliseC(alpha(:,t));
        [alpha(:,n), scale(n)] = normalise(alpha(:, n));
        pi(:, n) = normalise(pi(:, n));

        prevState(n) = 0;
        [p, state(n)] = max(alpha(:, n));
      end
      loglik(t, :) = log(scale);
    else
      %gesture continues
      for n = 1 : numGestures,
        trans = hmm{n}.transmat1;

        % P(O(t)|O(1:t-1)) = P(O(1:t))/P(O(1:t-1))
        m = trans' * alpha(:, n);

        alpha(:, n) = m(:) .* obslik{n}(:, t);

        for j = 1 : Q,
          [pi(j, n)] = max(pi(:, n) .* trans(:, j));
          pi(j, n) = pi(j, n) * obslik{n}(j, t);
        end

        %[alpha(:, t), scale(t)] = normaliseC(alpha(:, t));
        [alpha(:, n), scale(n)] = normalise(alpha(:, n));
        pi(:, n) =  normalise(pi(:, n));

        prevState(n) = state(n);
        [p, state(n)] = max(alpha(:, n));
      end
      loglik(t, :) = loglik(t - 1, :) + log(scale);
    end
    [Y, I] = sort(loglik(t, :));
    diff  = Y(end) - Y(end - 1);
    %fprintf('time = %d, diff = %f\n', t, diff);
    if (diff > DIFF_THRESH)
      currentGesture(t) = I(end);
    end
	else
    %gesture ends
    gestureOn = 0;
    currentGesture(t) = 0;
	end
end
                      
for n = 1 : numGestures,
  plot((1 : T) * xscale, loglik(:, n), STYLES{n}, 'lineWidth', 1.2);
  hold 'on';
end

start = 0;
stop = 0;
currentGesture
for t = 1 : T - 1,
  if (currentGesture(t) > 0)
    if (start == 0)
      start = t;
    end
    if (currentGesture(t) ~= currentGesture(t + 1))
      stop = t;     
    end
  elseif (start ~= 0)
    stop = t - 1;
  end
  
  if (start ~= 0 && stop ~= 0)
    style = STYLES{currentGesture(start)};
    style(2) = 'o';
    plot([start stop] * xscale, [-2300 -2300], STYLES{currentGesture(start)}, 'lineWidth', 2);
    plot([start, stop] * xscale, [-2300, -2300], style, 'lineWidth', 2);
    start = 0;
    stop = 0;
  end
  
end

if (start ~= 0)
  style = STYLES{currentGesture(start)};
  style(2) = 'o';
  plot([start T] * xscale, [-2300 -2300], STYLES{currentGesture(start)}, 'lineWidth', 2);
  plot([start, T] * xscale, [-2300, -2300], style, 'lineWidth', 2);
end
 
[gesture_names] = parseGestureNames(hmm);
legend(GESTURE_NAMES, 'Location', 'EastOutside', 'fontsize', 14);
set(gca, 'fontsize', 12);
xlabel('time step t', 'fontsize', 12);
ylabel('log-likelihood', 'fontsize', 12);

%get true labels
if (nargin >= 4)
  [trueLabels.labels, trueLabels.startTimes, trueLabels.endTimes] = readTrueLabels(trueLabelFile, filteredIndices);
  for i = 1 : length(trueLabels.labels),
    index = find(ismember(gesture_names, trueLabels.labels{i}));
    trueLabels.labels{i} = index;
    plot([trueLabels.startTimes(i) trueLabels.endTimes(i)] * xscale, [-2000 -2000], STYLES{index}, 'lineWidth', 2);
    style = STYLES{index};
    style(2) = 'x';
    plot([trueLabels.startTimes(i), trueLabels.endTimes(i)] * xscale, [-2000, -2000], style, 'lineWidth', 2);
  end
  [numTrueGestures, numCorrect] = evalConClassification(currentGesture, trueLabels, TOLERANCE);
end

set(gca, 'XMinorTick', 'on');
set(gca, 'xtick', 0 : 10 : T);
% labels = cell(T+1, 1);
% for i = 1 : T,
%   if (mod(i-1, 10) == 0)
%     labels{i} = int2str(i-1);
%   else labels{i} = ' ';
%   end
% end
% set(gca, 'XTickLabel', labels);
end