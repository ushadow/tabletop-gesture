% Batch process images with name prefix + index + '.png'
% imagePrefix prefix of the image to process
function [bgTransformed, div] = batchColorTransform(dir, startIndex, endIndex, method, params)

	bgTransformed = cell(endIndex-startIndex+1, 1);
	div = cell(endIndex-startIndex+1, 1);

	for i = startIndex:endIndex,
    
    path = [dir 'bgScaled' int2str(i) '_blur.png'];
    image = imread(path);
		%res = imageTransform(image,mapX,mapY);
    bgTransformed{i} = colorTransform(image, params, method);
		imwrite(bgTransformed{i},[dir 'bg' method int2str(i) '.png']);
    %res = res./bg;
		figure(i);
		subplot(1,2,1);
		imshow(bgTransformed{i});
		cameraImage = imread([dir 'bgRect' int2str(i) '_blur.png']);
		cameraImage = im2double(cameraImage);
		R = cameraImage./bgTransformed{i};
		R(R<0)=0;
		R(R>1)=1;
		div{i}=R;
		subplot(1,2,2);
		imshow(div{i});
    res_path = [dir 'div' method int2str(i) '.png'];
    imwrite(div{i},res_path,'PNG');

	end;
	
end
    