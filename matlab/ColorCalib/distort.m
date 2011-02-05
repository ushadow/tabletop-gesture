function I2 = distort(I)

if size(I,3) == 3
    I = rgb2gray(I);
end

[nrows,ncols] = size(I);
[xi,yi] = meshgrid(1:ncols,1:nrows);

X = reshape(xi,1,nrows*ncols);
Y = reshape(yi,1,nrows*ncols);

x_kk = [X;Y];
%-- Focal length:
fc = [ 2272.000248128179464 ; 2225.019907367975520 ];

%-- Principal point:
cc = [ 319.500000000000000 ; 239.500000000000000 ];

%-- Distortion coefficients:
kc = [ -3.357688190862814 ; 0.000000000000000 ; -0.065058503320605 ; 0.013638088172777 ; 0.000000000000000 ];

xn = normalize(x_kk,fc,cc,kc);

resamp = makeresampler('linear','fill');

u = reshape(xn(1,:),size(xi)).* fc(1) + cc(1);
v = reshape(xn(2,:),size(yi)).* fc(2) + cc(2);

tmap_B = cat(3,u,v);
I2 = tformarray(I,[],resamp,[2 1],[1 2],[],tmap_B,.3);

end
