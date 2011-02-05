% viewHmm(hmms) plot mu1 against each state
% inputs:
%   hmms = cell array of hmms
function viewHmm(hmms)
	NUM_SUBPLOTS = [2 3];
	STYLES = {'b' 'g' 'r' 'c' 'm' 'k' 'b:' 'g:' 'r:' 'c:' 'm:'};
	close all;
	
	numHmms = size(hmms,1);
	[numFeatures numStates] = size(hmms{1}.mu1);
	
	for i = 1:numFeatures,
		numSubplots = prod(NUM_SUBPLOTS);
		
		index = mod(i-1,numSubplots)+1;
		if(index==1)
			figure(floor((i-1)/numSubplots)+1);
			
		end
		subplot(NUM_SUBPLOTS(1), NUM_SUBPLOTS(2), index);
		hold on;
		for j = 1:numHmms,
			
			plot(1:numStates,hmms{j}.mu1(i,:), STYLES{j});
		end
	end
end