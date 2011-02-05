function hmm = trainSegHmmEm(diffNorms, iniHmm)

[hmm.LL, hmm.prior1, hmm.transmat1, hmm.mu1, hmm.Sigma1, hmm.mixmat1] = ...
       mhmm_em(diffNorms, iniHmm.prior1, iniHmm.transmat1, iniHmm.mu1, iniHmm.Sigma1, iniHmm.mixmat1, 'max_iter', 10, 'hasEndState', 0);
     
end