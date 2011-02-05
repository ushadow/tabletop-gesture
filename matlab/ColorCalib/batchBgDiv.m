% res = bgDiv(bg,image)
% bg: backgrou
% image_prefix: prefix of the path to the image, i.e. '../image'
% start: start index of the image
% The full path
function batchBgDiv(bg,dir,startIndex, endIndex, method)

	for i=startIndex:endIndex,
		image = imread([dir 'imageRect' int2str(i) '_blur.png']);
		image = im2double(image);
		res = image./bg;
		imwrite(res,[dir 'imageDiv' method int2str(i) '.png']);
	end	

end
    

