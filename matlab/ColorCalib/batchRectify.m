function batchRectify(imageDir, startIndex, endIndex, paramDir)

	for i = startIndex:endIndex,
    
	  path = [imageDir 'image' int2str(i) '.png'];
    image = imread(path);
		imageRect = distortionCorrection(image, paramDir);
		imwrite(imageRect,[imageDir 'imageRect' int2str(i) '.png']);
    
	end;
	
end
    