function images = batchImread(filePrefix, startIndex, endIndex)
  images = cell(endIndex - startIndex + 1, 1);
  for i = startIndex : endIndex,
    fileName = sprintf('%s%05d.png', filePrefix, i);
    images{i - startIndex + 1} = imread(fileName);
  end
end
