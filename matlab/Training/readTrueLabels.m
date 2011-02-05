% [labels, startTimes, endTimes] = readTrueLabels(path, filteredIndices)
% reads the true labels of the continuous gesture samples.
%
% Args:
%   path: full file name of the true label file.
%   filteredIndices: indices of feature vectors that should be ignored
function [labels, startTimes, endTimes] = readTrueLabels(path, filteredIndices)

[labels, startTimes, endTimes] = textread(path,'%s %d %d');
[startTimes, endTimes] = filter(startTimes, endTimes, filteredIndices);

end

function [startTimes, endTimes] = filter(startTimes, endTimes, filteredIndices)

if (~isempty(filteredIndices))
  n = length(filteredIndices);
  accum1 = ones(size(startTimes));
  accum2 = ones(size(endTimes));
  for i = 1 : n,
    ind = find(startTimes >= filteredIndices(i));
    accum1(ind) = accum1(ind) + 1;
    ind = find(endTimes >= filteredIndices(i));
    accum2(ind) = accum2(ind) + 1;
  end
  startTimes = startTimes - accum1;
  endTimes = endTimes - accum2;
  if (startTimes(1) == 0)
    startTimes(1) = 1;
  end
  if (endTimes(1) == 0)
    endTimes(1) = 1;
  end
end

end
