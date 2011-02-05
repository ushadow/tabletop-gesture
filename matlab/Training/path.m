% path find the state sequence with maximum probablity with give data
% sequence
%   P = path(data,hmm)
function P = path(data, hmm)
Q = length(hmm.prior1);
T = size(data, 2);
B = mixgauss_prob(data, hmm.mu1, hmm.Sigma1, hmm.mixmat1);
B(Q, :) = ones(1, T);

[p] = viterbi_path(hmm.prior1, hmm.transmat1, B, 'hasEndState', 1);

P = int32(p);
