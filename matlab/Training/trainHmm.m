% [hmm error classification]= trainHmm(trainDataAll, testDataAll, gestures,Q) train hmm model for each gesture   
%
% Parameters:
%   trainDataAll = scaled data for training, data is grouped into cell
%       arrays according to gesture names
%   testDataAll = scaled data for testing
%   gestures = a cell array of gesture names
%   Q = number of states
%   M = number of mixtures
% Outputs:
%   hmm = cell array of hmm models
%   error = test error rate
%   classification = classfication error matrix

function [hmm accuracy classification]= trainHmm(trainDataAll, testDataAll, gestures, Q, M)

numGestures = size(trainDataAll, 2);

hmm = cell(numGestures,1);

for i = 1 : numGestures,
    
    trainData = trainDataAll{i};

    % Initialize parameters

    [prior0, transmat0, mu0, Sigma0, mixmat0, transEnd0] = initialize(trainData, Q, M);

    [hmm{i}.LL, hmm{i}.prior1, hmm{i}.transmat1, hmm{i}.mu1, hmm{i}.Sigma1, hmm{i}.mixmat1, hmm{i}.transEnd1] = ...
       mhmm_em(trainData, prior0, transmat0, mu0, Sigma0, mixmat0, 'max_iter', 10, 'hasEndState', 0, 'verbose', 0, 'transEnd', transEnd0);
   
    hmm{i}.gesture = gestures{i};
end

[accuracy classification] = test(testDataAll,hmm);

end


