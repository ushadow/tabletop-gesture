function displayPalette(nrows, ncols, colors)

numColors = size(colors,1);

if(numColors ~= nrows * ncols)
	error('Wrong nubmer of rows or columns!');
end;

s = 100;
P = zeros(nrows*s, ncols*s, 3);

for i = 1:nrows,
	for j = 1:ncols,
		for x = (i-1) * s + 1 : i * s,
      for y = (j-1) * s + 1 : j * s,
				P(x, y,:) = colors((i-1)*ncols+j,:);
			end
		end
	end
end
	 
imshow(P);