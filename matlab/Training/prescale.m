% [trainData, testData, mu, sigma] = prescale(data) standardize each training feature data with mean 0 and std dev 1.
%   
% Parameters:
%   data = cell array of cell arrays. Each matrix is a gesture sample and is grouped into cell arrays according to gesture name. Each
%          column is a feature vector
%   numTests = number of test samples for each gesture
%
% Outputs:
%   trainData = standardized trainData with mu and sigma
%   testData = standardaized testData with mu and sigma
%   mu = mean for each feature based on training data
%   sigma = stardard deviation for each feature based on training data

function [trainData, testData, mu, sigma] = prescale(data, numTests)
% prescale standardize the data with 0 mean and 1 standard deviation

numGestures = size(data,2);
[trainData, testData] = getTrainTestData(data, numTests);

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

function [trainData testData] = getTrainTestData(data, numTestSamples)

numGestures = size(data, 2);
trainData = cell(1, numGestures);
testData = cell(1, numGestures);

for i = 1 : numGestures,
  numTests = length(data{i});
  step = floor(numTests / numTestSamples);
  testData{i} = data{i}(step : step : end);
  indices = 1 : length(data{i});
  indices(step : step : end) = [];
  trainData{i} = data{i}(indices);

end

end