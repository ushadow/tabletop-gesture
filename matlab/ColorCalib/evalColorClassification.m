%res = evalColorClassification(trueLabel, testImages)
%inputs:
% testImages = cell array of images
function res = evalColorClassification(trueLabel, testImages)
  white = [255 255 255];
  black = [0 0 0];
  numImages = size(testImages,1);
  [H, W, channels] = size(trueLabel);
  trueLabel = reshape(trueLabel, H * W, channels);
  
  total = 0;
  correct = 0; 
  
  for i = 1 : numImages,
    testImage = reshape(testImages{i}, H * W, channels);
    for j = 1 : H * W,
      if (all(trueLabel(j, :) == white))
        total = total + 1;
        if (all(testImage(j, :) == black))
          correct = correct + 1;
        end
      elseif (any(trueLabel(j, :) ~= black))
          total = total + 1;
          if(all(trueLabel(j, :) == testImage(j, :)))
            correct = correct + 1;
          end       
      end
    end
  end
 
  res = correct / total;
end