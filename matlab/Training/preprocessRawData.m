%scaledData = preprocessRawData(rawDataMap, featureSelect, mu, sigma)
function scaledData = preprocessRawData(rawDataMap, featureSelect, mu, sigma)

[featureData] = extractFeature2(values(rawDataMap), featureSelect);
scaledData = scale(featureData, mu, sigma);

end
