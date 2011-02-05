
% initialize initialize the parameters for training
%   [prior0, transmat0,mu,sigma, mixmat0] = initialize(data, Q, M)
%
% Parameters:
%   data = cell array of training data for a particular gesture, each column of data is an 
%          observation (feature vector)
%   Q = num of states
%   M = num of mixtures
%
% Outputs:
%   mu = matrix(featureLen, Q, M)
function [prior0, transmat0, mu, sigma, mixmat0, transEnd0] = initialize(data, Q, M)

% total number of examples
numex = length(data);
 
% initial state parameters
prior0 = zeros(Q, 1);
prior0(1) = 0.5; 
prior0(2) = 0.5;

transEnd0 = zeros(Q, 1);
transEnd0(Q - 1) = 0.5;
transEnd0(Q) = 0.5;

transmat0 = zeros(Q, Q); % transmission parameters
for i = 1 : Q - 2,
    transmat0(i, i : i + 2) = 0.3333;
end
transmat0(Q - 1, Q - 1 : Q) = 0.5;

%The last state is the end state which does not transit to other states
transmat0(Q, Q) = 1;

mixmat0 = ones(Q, M);

feature_len = size(data{1}, 1);

% each column of new_matrix{j} is a feature vector
new_matrix = cell(Q);

mu = zeros(feature_len, Q, M);
sigma = zeros(feature_len, feature_len, Q, M);

for i = 1 : numex,
  O = data{i};

  %length of the sequence
  m = size(O, 2);

  % divide the sequence to Q-1 divisions, each division has div
  % observations
  div = floor(m / Q);

  % group observations of the same state together
  for j = 1 : Q,
    start_index = (j-1) * div + 1;
    sub_matrix = O(:, start_index : start_index + div - 1);
    new_matrix{j} = horzcat(new_matrix{j}, sub_matrix);
  end
end

%initialize mu and sigma
for j = 1 : Q, 
  %mu(:, j) = mean(new_matrix{j}, 2);

  % Find var of each row
  % All variance is set to be 1, to avoid log-likelihood to be greater
  % than 1.
  
  [mu(:, j, :), sigma(:, :, j, :), mixmat0(j, :)] = mixgauss_init(M, new_matrix{j}, 'full');
  %sigma(:, :, j, :) = repmat(eye(feature_len), [1, 1, 1, M]);
  
end

end