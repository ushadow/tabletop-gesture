% rgb = rgbhist(I, filePrefix)
%
%	Inputs:
%		I = images whose colors are to be analyzed
%		filePrefix (optional) = prefix of the files to be saved, just without the extension

function rgb = rgbhist(images, filePrefix)
 
nBins = 8; %number of divisions for each color
width = 256/nBins;

%generated color palette
nr = 8;
nc = 10;
s = 100; % size of each square

rgb = zeros(1, nBins^3);

for n = 1:size(images,1),
	I = images{n};
	I = im2double(I);
	nRows = size(I,1);
	nCols = size(I,2);

	for i = 1:nRows,
			for  j = 1:nCols,
					color = floor(I(i,j,:).*(nBins-1));
					index = color(1) * nBins * nBins + color(2) * nBins + color(3) + 1;
					rgb(index) = rgb(index) + 1;
			end
	end
end

[B, IX] = sort(rgb,'descend');

C = zeros(nr * s, nc * s, 3); %image

%each row is a color
colors = zeros(nr * nc, 3);

% colors are recorded in a row-major order
for i = 1:nr,
    for j = 1:nc,
        index = (i-1) * nc + j;
        color = IX(index) - 1;
        b = mod(color, 8);
        color = floor(color/8);
        g = mod(color, 8);
        r = floor(color/8);
        colors(index,:) = [r g b] .* width + width/2;
        for x = (i-1) * s + 1 : i * s,
            for y = (j-1) * s + 1 : j * s,
                C(x,y,:) = colors(index,:);
            end
        end
    end
end

C = uint8(C);
imshow(C);

if(nargin >= 2 && ischar(filePrefix))
    imwrite(C, [filePrefix '.png'], 'PNG');
    fout = fopen([filePrefix '.txt'], 'w');
    fprintf(fout, '%d %d %d\n', colors');
    fclose(fout);
end

end
    