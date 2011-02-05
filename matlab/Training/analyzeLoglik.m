% analyzeLoglik find average upper bound and average lower bound of loglik
% for isolated gesture samples
%   [averageUpperBound averageLowerBound] = analyzeLoglik(testData, hmm)
function [averageUpperBound averageLowerBound] = analyzeLoglik(testData, hmm)

numGestures = size(testData, 2);
Q = size(hmm{1}.mu1,2);
alpha = zeros(Q, 1);

upperBoundTotal = 0;
lowerBoundTotal = 0;
numTests = 0;

for i = 1 : numGestures,
    
    numSamples = size(testData{i}, 2);
    
    for j = 1 : numSamples,
        
        data = testData{i}{j};
        loglik = 0;
        upperBound = 0;
        lowerBound = 0;
        
        % alpha(:,n) records the last time frame alpha

        % evaluate alpha (forward algo) and loglik one time frame a time
        t = 1;
   
        obslik = mixgauss_prob(data(:,t), hmm{i}.mu1, hmm{i}.Sigma1, hmm{i}.mixmat1);
        obslik(Q, 1) = 1;
        alpha(:) = hmm{i}.prior1(:) .* obslik(:);

        [alpha(:), scale] = normalise(alpha(:));

        [p, state] = max(alpha(:));

        loglik = loglik + log(scale);
        
        if(state < 2)
            upperBound = loglik;
        end
        
        T = size(data, 2);

        for t=2:T
           
           trans = hmm{i}.transmat1;
           obslik = mixgauss_prob(data(:,t), hmm{i}.mu1, hmm{i}.Sigma1, hmm{i}.mixmat1);

           obslik(Q, 1) = 1;
           % P(O(t)|O(1:t-1)) = P(O(1:t))/P(O(1:t-1))
           m = trans' * alpha(:);

          alpha(:) = m(:) .* obslik(:);
      
          [alpha(:), scale] = normalise(alpha(:));
  

          [p, state] = max(alpha(:));
          
          loglik = loglik + log(scale);
          
          if(state < 2)
            upperBound = loglik;
          else if (state == Q)
               lowerBound = loglik;
              end
          end
        end
        
        upperBoundTotal = upperBoundTotal + upperBound;
        lowerBoundTotal = lowerBoundTotal + lowerBound;
        numTests = numTests + 1;
    end
end

averageUpperBound = upperBoundTotal / numTests;
averageLowerBound = lowerBoundTotal / numTests;
