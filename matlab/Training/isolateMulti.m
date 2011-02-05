function isolateMulti(dir, featureSelect, numTests, Q)
for i = 1 : 4
  [rawDataMap, featureSelect, featureData, trainData, testData, mu, sigma, hmm, accuracy] = trainAll(dir, featureSelect, numTests, Q, i);
  trainingAccuracy = test(trainData, hmm);
  fprintf('num mixutres = %d, test accuracy = %f, training accuracy = %f\n', i, accuracy, trainingAccuracy);
end

end