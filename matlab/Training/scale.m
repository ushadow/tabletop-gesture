%[data] = scale(data, mu, sigma)
%Inputs:
%   data = cell array of cell arrays of data with dimesion 1xN
%Outputs:
%   data = scaled data
function [data] = scale(data, mu, sigma)

	numCells = size(data,2);

	for i = 1 : numCells,
			numSamples = size(data{i},2);
			for j = 1 : numSamples,
					data{i}{j} = standardize(data{i}{j},mu,sigma);
			end
	end

end