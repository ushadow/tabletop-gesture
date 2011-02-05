function isolateSingle(dir, featureSelect)

for i = 3 : 9,
  [rawDataMap, featureSelect, featureData, trainData, testData, mu, sigma, hmm, accuracy] = trainAll(dir, featureSelect, 3, i, 1);
  trainingAccuracy = test(trainData, hmm);
  fprintf('num states = %d, test accuracy = %f, traiining accuracy = %f\n', i, accuracy, trainingAccuracy);
end

end