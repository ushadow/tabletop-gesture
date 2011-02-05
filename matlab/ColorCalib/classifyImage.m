%classified = classifyImage(I,params): color classification of image I
function classified = classifyImage(I,params)

I = im2uint8(I);

R = bitshift(uint32(I(:,:,1)), 16);
G = bitshift(uint32(I(:,:,2)), 8);
B = uint32(I(:,:,3));

RGB = bitor(bitor(R,G), B) + 1;

classified = params.table(RGB) + 1;

colormap(params.colors./255);

image(classified);