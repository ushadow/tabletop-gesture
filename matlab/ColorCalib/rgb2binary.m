function im = rgb2binary(rgb,thresh)

[height, width, depth] = size(rgb);

rgbDouble = im2double(rgb);
im = zeros(height, width);

for i = 1:height,
    for j = 1:width,
        if (rgbDouble(i,j,1)+rgbDouble(i,j,2)+rgbDouble(i,j,3)>thresh)
            im(i,j) = 1;
        end
    end
end

imshow(im);








