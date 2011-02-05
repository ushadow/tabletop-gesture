% total = evalColorTransform(images) evalutes how the color of each pixel in the resulting background
% subtracted image deviates from a neutral (grey) color
% inputs
%		images = cell array of images after background division in a column
function total = evalColorTransform1(images)

numImages = size(images, 1);
total = 0;

for i=1 : numImages,
	ave = sum(images{i}, 3) ./ 3;
	ave = repmat(ave, [1, 1, 3]);
	diff = (images{i} - ave) .^ 2;
	total = total + sum(diff(:));
end

end