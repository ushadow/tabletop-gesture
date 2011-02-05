% inputs:
%		I = image in uint8 format
% outputs:
%		res = image in double format [0,1]
function res = colorTransformLUT(I, dir, method, N)

	scale = 256/N;
	res = zeros(size(I));
	LUT = readBinaryFile([dir 'colormap' method int2str(N)],'uint8');

	I = double(I);

	R = I(:,:,1);
	G = I(:,:,2);
	B = I(:,:,3);

	index = floor(R./scale).*N.*N + floor(G./scale).*N + floor(B./scale) + 1;

	res(:,:,1) = LUT(index);
	res(:,:,2) = LUT(index + N*N*N);
	res(:,:,3) = LUT(index + N*N*N*2);

	res = res./255;

end