% total = evalColorTransform(images) evalutes how the color of each pixel in the resulting background
% subtracted image deviates from a neutral (grey) color
% inputs
%		camImgs = cell array of camera images in a column
function total = evalColorTransform2(camImgs, projImgs)

numImages = size(camImgs,1);
total = 0;

for i=1:numImages,
	camImg = im2double(camImgs{i});
	projImg = im2double(projImgs{i});
	error = sum(sum(sum((camImg - projImg).^2)));
  total = total + error;
end

end