% [featureSelect, mu, sigma] = analyzeData(dataMap)analyze raw
% data to select features
%
% Parameters:
%   data = map of cell arrays of data for each gesture sample
function [featureSelect, mu, sigma] = analyzeData(dataMap)

NUM_VECS = 18;

%for each vector data sample
%(x, y, z) of forearm
%(roll, yaw, pitch) for 17 joints (forearm, hand, thumb, index, middle,
%ring, pinky). Note that the rotation data for hand is not used, that's why
%its degree of freedom is 0
DOF = [3 3 0 2 1 1 2 1 1 2 1 1 2 1 1 2 1 1];
totalDOF = sum(DOF);
featureSelect = zeros(1, totalDOF);

%concatenate all data for analysis

%cell arrays of data for each gesture
data = values(dataMap);

%number of gestures
count = size(data,2);
concatData = [];
for i = 1 : count,
    c = size(data{i}, 2);

    for j = 1 : c
        concatData = horzcat(concatData, data{i}{j});
    end
end

% mu and sigma2 are column vectors
mu = mean(concatData,2);
sigma = std(concatData,0,2);
    
sigma_reshape = reshape(sigma, 3, NUM_VECS);

[Y, I] = sort(sigma_reshape, 1, 'descend');

currentIndex = 1;

for i = 1 : NUM_VECS,
    d = DOF(i);
    if d > 0
        base = (i-1) * 3;
        featureSelect(currentIndex : currentIndex + d -1) = base + I(1:d,i); 
        currentIndex = currentIndex + d;
    end
end

featureSelect = sort(featureSelect);
          
