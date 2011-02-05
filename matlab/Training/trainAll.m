% [rawDataMap, featureSelect, featureData, trainData, testData, mu, sigma, hmm, accuracy, classification] = trainAll(path, featureSelect, numTests, Q, M): The full
% process of reading data and traing
% 
% Input:
%   featureSelect = vector of feasture indices to select desired features
%   featureData = unscaled feature data
function [rawDataMap, featureSelect, featureData, trainData, testData, mu, ...
  sigma, hmm, accuracy, classification] = trainAll(path, featureSelect, numTests, Q, M)

SUFFIX_LEN = 8;

rawDataMap = readData(path, SUFFIX_LEN);

if(nargin == 1)
    [featureSelect] = analyzeData(rawDataMap);
end

[featureData] = extractFeature2(values(rawDataMap), featureSelect);

[trainData, testData, mu, sigma] = prescale(featureData, numTests);

[hmm accuracy classification] = trainHmm(trainData, testData, keys(rawDataMap), Q, M);

end