% diffNorm = getNormDiff2(scaledFeatureData) generate festure data uses
% velocity for segmetation hmm training
% 
% inputs:
%   featureData= cell array of scaled feature data, includes velocity, scaled
% output:
%   diffNorms = if the input is a cell array, the output is a cell array
%   too, else the output is one data sample
function diffNorm = getNormDiff2(scaledFeatureData)

  if (iscell(scaledFeatureData))
    n = length(scaledFeatureData);
    diffNorm = cell(n,1);

    for j = 1 : n,
      diffNorm{j} = getNormDiffOneData(scaledFeatureData{j});
    end
  else
    diffNorm = getNormDiffOneData(scaledFeatureData);
  end

end

function res = getNormDiffOneData(featureData)

  [featureSz, sampleSz] = size(featureData);
  temp = zeros(featureSz, sampleSz);

  %compute difference between consecutive frames, ignore the first 2 rows
  %because they are velocity which is already the difference between the
  %consecutive frames
  temp(3 : end, 2 : end) = featureData(3 : end, 1 : end - 1);
  diff = zeros(featureSz, sampleSz -1);
  diff(3 : end, :) = featureData(3 : end, 2 : end) - temp(3 : end, 2 : end);

  diff(1 : 2, :) = featureData(1 : 2, 2 : end);
  res = zeros(1, sampleSz - 1);
  for i = 1 : sampleSz - 1,
    res(i) = norm(diff(:, i));
  end
end