% Batch process images with name prefix + index + '.png'
% imagePrefix prefix of the image to process
function [bgTransformed, div] = batchColorTransformLUT(dir, startIndex, endIndex, method, N)

	bgTransformed = cell(endIndex-startIndex+1, 1);
	div = cell(endIndex-startIndex+1, 1);

	for i = startIndex : endIndex,

		path = [dir 'bgScaled' int2str(i) '_blur.png'];
		image = imread(path);

		bgTransformed{i} = colorTransformLUT(image, dir, method, N);
		imwrite(bgTransformed{i}, [dir 'bgLUT' int2str(N) method int2str(i) '.png']);
			
		figure(i);
		subplot(1, 2, 1);
		imshow(bgTransformed{i});
		cameraImage = imread([dir 'bgRect' int2str(i) '_blur.png']);
		cameraImage = im2double(cameraImage);
		R = cameraImage ./ bgTransformed{i};
		R(R < 0) = 0;
		R(R > 1) = 1;
		div{i} = R;
		subplot(1, 2, 2);
		imshow(div{i});
		res_path = [dir 'divLUT' int2str(N) method int2str(i) '.png'];
		imwrite(div{i}, res_path, 'PNG');

	end;
	
end
    