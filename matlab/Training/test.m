%[errorRate, classification] = test(data,hmm) test isolated gesture
%classification
%
% Parameters:
%   data = cell arrays, data{i} is a 
%   hmm = cell array of hmm models for each gesture 
%
% Outputs:
%   errorRate = # errors / total test samples
%   classification = value at (i,j) is the number of test samples that is
%       actual gesture i classified as gesture j
function [accuracy, classification] = test(data, hmm)

% number of signs to test
numGestures = length(hmm);

testGestures = size(data, 2);

classification = zeros(numGestures);

error = 0;
totalTests = 0;
    
for k = 1 : testGestures,
    actualGesture = k;
    numSamples = size(data{k}, 2);
    for i = 1 : numSamples,    
        max_ll = -inf;
        gesture = 0; % classification
        totalTests = totalTests + 1;
        for j = 1 : numGestures,
            ll = mhmm_logprob(data{k}{i}, hmm{j}.prior1, hmm{j}.transmat1,...
                hmm{j}.mu1, hmm{j}.Sigma1, hmm{j}.mixmat1, 'hasEndState', 0, 'transEnd', hmm{j}.transEnd1);
            if (ll > max_ll)
                max_ll = ll;
                gesture = j;
            end
        end
   
        if (gesture ~= 0)
          classification(actualGesture, gesture) = classification(actualGesture, gesture) + 1;
        end
        if( actualGesture ~= gesture || gesture == 0 )
            error = error + 1;
        end
    end
end


accuracy = 1 - error / totalTests;

end
        
    