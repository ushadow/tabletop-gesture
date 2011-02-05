% [newData] = extractFeature(rawData, featureSelect) extract features from
% raw data, using velocity in x-y plane
% Parameters:
%   data = cell array of raw data grouped in cell arrays according to
%       gestures
%   featureSelect = a vector to select the features from each column of the
%       raw data.
% Outputs:
%   data = cell array of new data after feature selection and transformation (i.e., calculating speed), data for each
%       gesture is grouped in a cell array
function [data] = extractFeature2(data, featureSelect)

count = size(data, 2);

for n = 1 : count,
    %select features and calculate speed
    c = size(data{n}, 2);
    for i = 1 : c,
        tempData = data{n}{i}(featureSelect, :);
        %translation in x-y plane
        trans1 = tempData(1:2,:);
        trans2 = zeros(size(trans1));
        trans2(:,2:end) = trans1(:,1:end-1);
        diff = trans1-trans2;

        tempData(1:2,:) = diff;
        
        %ignore the first column, i.e. data sample at t = 1
        tempData(:,1) = [];
        data{n}{i} = tempData;
    end
end
