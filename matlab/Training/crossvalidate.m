% crossvalidate(data, n, names): n-fold cross-validation
function crossvalidate(data, n, names)

  numSamples = length(data{1});
  numTests = floor(numSamples / n);
  
  for q = 3 : 9,
    errorRateSum = 0;
    for i = 1 : numTests : numSamples,
      [trainData, testData] = prescale(data, i : (i + numTests - 1));
      [hmm errorRate] = trainHmm(trainData, testData, names, q, 1);
      errorRateSum = errorRateSum + errorRate; 
    end
    fprintf('q = %d, errorRate = %f\n', q, errorRateSum / n);
  end
end

function [trainData, testData, mu, sigma] = prescale(data, testIndices)
% prescale standardize the data with 0 mean and 1 standard deviation

numGestures = size(data,2);
[trainData, testData] = getTrainTestData(data, testIndices)

%Concatenate all training data
all = [];
for i = 1 : numGestures,
  numSamples = size(trainData{i}, 2);
  for j = 1 : numSamples,
    all = horzcat(all, trainData{i}{j});
  end
end

% mu and sigma are column vectors
mu = mean(all, 2);

% calculate standard deviation
sigma = std(all, 0, 2);
sigma = sigma + eps * (sigma == 0);

trainData = scale(trainData, mu, sigma);
testData = scale(testData, mu, sigma);

end

% getTrainTestData Get training and test data as cell arrays   
%   [trainData testData] = getTrainTestData(data, numTestSamples)trainData = getTrainingData(data, ind)
%
% Parameters:
%   data = cell array of sample matrices
%   numTestSamples = number of test samples for each gesture
%
% Ouputs:
%   trainData = cell arrays of training data, trainData{i} is a cell array
%   of matrices for gesture i.
%   testData = cell arrays of test data, testData{i} is a cell array
%   of matrices for gesture i.

function [trainData testData] = getTrainTestData(data, testIndices)

numGestures = size(data, 2);
trainData = cell(1, numGestures);
testData = cell(1, numGestures);

for i = 1 : numGestures,
  testData{i} = data{i}(testIndices);
  indices = 1 : length(data{i});
  indices(testIndices) = [];
  trainData{i} = data{i}(indices);

end

end