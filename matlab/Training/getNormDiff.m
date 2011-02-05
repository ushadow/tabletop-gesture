% diffNorm = getNormDiff(featureData) featureData uses speed
% 
% inputs:
%   featureData = scaled feature data with speed
function diffNorm = getNormDiff(featureData)

[featureSz, sampleSz] = size(featureData);
temp = zeros(featureSz, sampleSz);
temp(:,2:end) = featureData(:,1:end-1);

%ignore the first row speed first
%ignore the first time frame
diff = featureData(2:end, 2:end) - temp(2:end,2:end);
diffNorm = zeros(1, sampleSz-1);
for i = 1:sampleSz-1,
	diffNorm(i) = norm(diff(:,i)) + featureData(1,i+1);
end

end