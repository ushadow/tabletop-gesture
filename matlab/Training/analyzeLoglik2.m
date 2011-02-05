% analyzeLoglik2(samples, hmm) plots the loglik from each hmm model for each 
% test data
% 
% Args:
%   samples: cell array of input samples whose loglikelihood are to be 
%     evaluated.
%   hmm: cell array of hmms from training
%   indices (optional): an array of 2 elements. If present, plot the 
%     loglikelihood for samples{indices(1)}{indices(2)}.
%   useLegend (optional): boolean to indicate whether to plot legend or not.
%   xscale (optional): scale apply to x-axis to change the unit it
%     represents.
function analyzeLoglik2(samples, hmm, indices, useLegend, xscale)

GESTURES_PER_FIG = 3;
GESTURE_NAMES = {'pan down', 'pan left', 'pan right', 'pan up', ...
  'pitch anticlockwise', 'pitch clockwise', 'roll anticlockwise', ...
  'roll clockwise', 'yaw anticlockwise', 'yaw clockwise', 'zoom in', ...
  'zoom out'};

close all;

if (nargin < 5)
  xscale = 1;
end

numGestures = size(samples, 2);
numModels = size(hmm, 1);

gestureNames = cell(1, numGestures);

for i = 1 : numModels,
  remain = hmm{i}.gesture;
  gestureName = [];
  while true
    [str, remain] = strtok(remain, '_');
    if isempty(str), break; end
    gestureName = [gestureName str];
  end
  gestureNames{i} = gestureName;
end

if (nargin < 3)
  for i = 1 : numGestures,
    numSamples = size(samples{i}, 2);

    for j = 1 : numSamples,

      data = samples{i}{j};
      figureIndex = floor((i - 1) / GESTURES_PER_FIG) + 1;
      index = mod(i - 1, GESTURES_PER_FIG);
      if(index == 0)
         figure(figureIndex);
      end
      subplot(GESTURES_PER_FIG, numSamples, index * numSamples + j);
      plotLoglik(data, hmm, GESTURE_NAMES{i}, xscale);
      if(index == 0 && j==1)
        legend(GESTURE_NAMES, 'Location', 'Best');
      end
    end
  end
else
  data = samples{indices(1)}{indices(2)};
  plotLoglik(data, hmm, GESTURE_NAMES{indices(1)}, xscale);
  if (nargin >=4 && useLegend)
    legend(GESTURE_NAMES, 'Location', 'Best', 'fontsize', 12);
  end
end

end

function plotLoglik(data, hmm, gestureName, xscale)
  STYLES = {'b' 'g' 'r' 'c' 'm' 'k' 'b:' 'g:' 'r:' 'c:' 'm:' 'k:'};
  numModels = length(hmm);
  T = size(data, 2);
  Q = size(hmm{1}.mu1, 2);
  alpha = zeros(Q, 1);
  
  for k = 1 : numModels,

    loglik = zeros(1, T);

    % alpha(:,n) records the last time frame alpha

    % evaluate alpha (forward algo) and loglik one time frame a time
    t = 1;

    obslik = mixgauss_prob(data, hmm{k}.mu1, hmm{k}.Sigma1, hmm{k}.mixmat1);
    obslik(Q, :) = 1;
    alpha(:) = hmm{k}.prior1(:) .* obslik(:, t);

    [alpha(:), scale] = normalise(alpha(:));

    loglik(1) = log(scale);

    for t = 2 : T

      trans = hmm{k}.transmat1;
      % P(O(t)|O(1:t-1)) = P(O(1:t))/P(O(1:t-1))
      m = trans' * alpha(:);

      alpha(:) = m(:) .* obslik(:, t);

      [alpha(:), scale] = normalise(alpha(:));

      loglik(t) = loglik(t - 1) + log(scale);
      %loglik(t) = log(scale);

    end
    title(gestureName);
    
    plot((1 : T) * xscale, loglik, STYLES{k}, 'lineWidth', 1.2);
    set(gca, 'FontSize', 12);
    xlabel('time step t', 'fontsize', 12);
    ylabel('log-likelihood', 'fontsize', 12);
    hold 'on';
  end
  
end

