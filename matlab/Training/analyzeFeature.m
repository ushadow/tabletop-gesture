%analyzeFeature(featureData, trueLabelFile, hmmSeg)
function diffNorm = analyzeFeature(featureData, trueLabelFile, hmmSeg)

close all;
hold on;

diffNorm = getNormDiff2(featureData);

diffLen = length(diffNorm);
plot(1:diffLen, diffNorm);

muOff = hmmSeg.mu1(1);
muOn = hmmSeg.mu1(2);

%get true labels
[labels, startTimes, endTimes] = textread(trueLabelFile, '%s %d %d');
plot([1 startTimes(1)-2], [muOff muOff], 'g');
for i = 1:length(labels),
  plot([startTimes(i)-1 endTimes(i)-2],[muOn muOn], 'r');
  if(i<length(labels))
    plot([endTimes(i)-1 startTimes(i+1)-2], [muOff muOff], 'g');
  else
    plot([endTimes(i)-1 diffLen], [muOff muOff], 'g');
  end
		
end

legend(gestureNames,'Location', 'Best');

end