% averageMaxDiff = analyzeLoglik3(testData, hmm) plots loglik differences
%   red line: difference between highest loglik and 2nd highest
%   gree line: max diff between highest and 2nd highest when i is not
%           highest
%   blue line: if loglik(i) is the highest at t, diff(t) = loglik(i)-2nd highest
%           else diff(t) = highest - loglik(i) at t
%   blue +: difference between loglik(i) and the second highest loglik
%           if i is not the highest at t, diff(t)=0
%
% inputs:
%   testData = cell array of cell arrays of data
function averageMaxDiff = analyzeLoglik3(testData, hmm)

GESTURES_PER_FIG = 3;

close all;

numGestures = size(testData, 2);
Q = size(hmm{1}.mu1, 2);
alpha = zeros(Q, 1);
numModels = size(hmm, 1);
totalMaxDiff = 0;
totalSamples = 0;

%parse gesture names
gestureNames = cell(1, numGestures);
for i = 1 : numGestures,
  remain = hmm{i}.gesture;
  gestureName = [];
  while true
      [str,remain] = strtok(remain, '_');
      if isempty(str), break; end
      gestureName = [gestureName str];
  end
  gestureNames{i} = gestureName;
end

for i = 1 : numGestures,
    
  numSamples = size(testData{i}, 2);

  for j = 1 : numSamples,

    data = testData{i}{j};
    T = size(data, 2);

    figureIndex = floor((i-1)/GESTURES_PER_FIG)+1;
    index = mod(i-1,GESTURES_PER_FIG);
    if(index == 0)
        figure(figureIndex);
    end
    subplot(GESTURES_PER_FIG, numSamples, index * numSamples + j);

    loglik = zeros(numModels,T);

    for k=1 : numModels,

      % alpha(:,n) records the last time frame alpha

      % evaluate alpha (forward algo) and loglik one time frame a time
      t = 1;

      obslik = mixgauss_prob(data, hmm{k}.mu1, hmm{k}.Sigma1, hmm{k}.mixmat1);
      obslik(Q, :) = 1;
      alpha(:) = hmm{k}.prior1(:) .* obslik(:,t);

      [alpha(:), scale] = normalise(alpha(:));

      loglik(k, 1) = log(scale);

      for t=2:T

        trans = hmm{k}.transmat1;
        % P(O(t)|O(1:t-1)) = P(O(1:t))/P(O(1:t-1))
        m = trans' * alpha(:);

        alpha(:) = m(:) .* obslik(:, t);

        [alpha(:), scale] = normalise(alpha(:));

        loglik(k, t) = loglik(k, t - 1) + log(scale);

      end

      title(gestureNames{i});

      hold 'on';
    end

    [Y, I] = sort(loglik);

    diff = zeros(1, T);

    %time step that gesture i has the max probability
    I1 = find(I(end, :) == i);
    diff(I1) = Y(end, I1) - Y(end - 1, I1);

    %plot difference between loglik(i) and the second highest loglik
    %if i is not the highest at t, diff(t)=0
    plot(1 : T, diff, 'b+');

    %time steps that gesture i doesn't have the max probability
    I2 = find(I(end, :) ~= i);
    diff(I2) = Y(end, I2) - loglik(i, I2);

    %blue line: if loglik(i) is the highest at t, diff(t) = loglik(i)-2nd highest
    %           else diff(t) = highest - loglik(i) at t
    plot(1 : T, diff);

    diff2 = Y(end, :) - Y(end - 1, :);
    maxDiff = max(diff2(I2));
    if(isempty(maxDiff))
        maxDiff = 0;
    end

    totalMaxDiff = totalMaxDiff + maxDiff;
    totalSamples = totalSamples + 1;
    %red line: difference between highest loglik and 2nd highest
    %gree line: max diff between highest and 2nd highest when i is not
    %           highest
    plot(1 : T, diff2, 'r', 1 : T, repmat(maxDiff, 1, T), 'g');
  end
end

averageMaxDiff = totalMaxDiff / totalSamples;

end
