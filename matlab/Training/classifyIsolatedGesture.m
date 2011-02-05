function gesture = classifyIsolatedGesture(data, hmm)

	numGestures = length(hmm);

	ll = zeros(numGestures,1);
	
	for i = 1 : numGestures,    
       
		ll(i) = mhmm_logprob(data,hmm{i}.prior1, hmm{i}.transmat1,...
				hmm{i}.mu1, hmm{i}.Sigma1, hmm{i}.mixmat1, 'hasEndState', 1);
	end
     
	[Y, gesture ] = max(ll);
	
end