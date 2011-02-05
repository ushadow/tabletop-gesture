%evalConClassification(result, trueLabels)
%
%inputs:
% result = recognition result is a vector of gesture index
% trueLabels = cell arrays: {1} gesture labels in index; {2} start index {3} end index
function [totalGestures, correct] = evalConClassification(result, trueLabels, tolerance)

totalGestures = size(trueLabels.labels, 1);
T = length(result);
labels = trueLabels.labels;
startIndices = trueLabels.startTimes;
endIndices = trueLabels.endTimes;

correct = 0;

for i = 1 : totalGestures,
  leftBound = startIndices(i) - tolerance;
  if (leftBound < 1)
    leftBound = 1;
  end
  rightBound = endIndices(i) + tolerance;
  if (rightBound > T)
    rightBound = T;
  end
  
  if (find(result(leftBound : rightBound) == labels{i}))
    correct = correct + 1;
  end
end