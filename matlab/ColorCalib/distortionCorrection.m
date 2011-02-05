% R = distortion_correction(I, filePrefix, color) The transformation map for distortion correction is saved as a binary
% file named 'distmap'.
%
% Inputs:
%   I = image read using imread with integer values
%   dir = directory to get the distortion parameters and to save the distortion map
%		save = (optional) if save = 1, to save the distortion map.
%		color  = (optional)  
% Ouputs:
%   R = result image in double
% 

function R = distortionCorrection(I, dir, save, color)

inputFileName = 'Calib_Results.mat';
outputFileName = 'distmap';
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% read data generated from matlab camera calibration toolbox
% fc = focal length
% cc = principal point
% kc = distortion coefficients
load([dir inputFileName], 'fc', 'cc', 'kc');

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

I = im2double(I);

[nrows, ncols, d3] = size(I);

%[X,Y] = MESHGRID(x,y) transforms the domain specified by vectors
%    x and y into arrays X and Y that can be used for the evaluation
%    of functions of two variables.
%The rows of the output array X are copies of the vector x and
%the columns of the output array Y are copies of the vector y.
[xi, yi] = meshgrid(1 : ncols, 1 : nrows);

X = reshape(xi, 1, nrows * ncols);
Y = reshape(yi, 1, nrows * ncols);

%Feature locations on the image
x_kk = [X; Y];

%Convert feature locations to normalized coordinates with origin at the
%priciple point and z = f (focal length)
x = [(x_kk(1, :) - cc(1)) / fc(1); (x_kk(2, :) - cc(2)) / fc(2)];
xn = apply_distortion2(x, kc);

%convert back to image coordinates
u = reshape(xn(1, :), size(xi)) .* fc(1) + cc(1);
v = reshape(xn(2, :), size(yi)) .* fc(2) + cc(2);

%if filePrefix argument is not null, save the transform map
if (nargin >= 3 && save == 1)
    fout = fopen([dir outputFileName], 'w');
    %Change to zero-base coordinates
    fwrite(fout, (round(u - 1))', 'short');
    fwrite(fout, (round(v - 1))', 'short');

    fclose(fout);
end

R = ones(nrows, ncols, d3);

% TDIMS_A   TDIMS_A and TDIMS_B indicate which dimensions of the input
% TDIMS_B   and output arrays are involved in the geometric
%               transformation. The entries need not be listed in
%               increasing order, but the order matters.  It specifies the
%               precise correspondence between dimensions of arrays A and B
%               and the input and output spaces of the transformer, T.
%               LENGTH(TDIMS_A) must equal T.ndims_in, and LENGTH(TDIMS_B)
%               must equal T.ndims_out.
%  
%               Suppose, for example, that T is a 2-D transformation,
%               TDIMS_A = [2 1], and TDIMS_B = [1 2].  Then the column
%               dimension and row dimension of A correspond to the first
%               and second transformation input-space dimensions,
%               respectively.  The row and column dimensions of B
%               correspond to the first and second output-space
%               dimensions, respectively.
%  
% TMAP_B is an array that provides an alternative
%              way of specifying the correspondence between the position
%              of elements of B and the location in output transform
%              space.  TMAP_B can be used, for example, to compute the
%              result of an image warp at a set of arbitrary locations in
%              output space.  If TMAP_B is not empty, then the size of
%              TMAP_B takes the form: 
% 
%                  [D1 D2 D3 ... DN L]
% 
%              where N equals length(TDIMS_B).  The vector [D1 D2 ... DN]
%              is used in place of TSIZE_B.  If TMAP_B is not empty, then
%              TSIZE_B should be [].
%  
%              The value of L depends on whether or not T is empty.  If T
%              is not empty, then L is T.ndims_out, and each L-dimension
%              point in TMAP_B is transformed to an input-space location
%              using T.  If T is empty, then L is LENGTH(TDIMS_A), and
%              each L-dimensional point in TMAP_B is used directly as a
%              location in input space.

%resamp = makeresampler('linear','fill');
resamp = makeresampler('nearest', 'replicate');
TMAP_B = cat(3, u, v);
R(:, :, 1) = tformarray(I(:, :, 1), [], resamp, [2 1], [1 2], [], TMAP_B, .3);
R(:, :, 2) = tformarray(I(:, :, 2), [], resamp, [2 1], [1 2], [], TMAP_B, .3);
R(:, :, 3) = tformarray(I(:, :, 3), [], resamp, [2 1], [1 2], [], TMAP_B, .3);

if(nargin >= 3)
    checkColor(color);
end

end


