% CALIBRATE computes the transformation matrix that maps one set of points 
%   to other. Homogeneous coordinates are used for projective
%   transformation
%
% Usage: [M, Tr] = calibrate(T, C)
%
% Inputs:
%
%	- T is a (d1 x n1) matrix containing n1 points in
%         the target image
%
%	- C is a (d2 x n2) matrix containing n2 points in
%	  the current camera image or world coordinate
%   
%   Both T and C are matrices of column vectors, n1 = n2. 
%
% Outputs:
%
%	- M where T = MC
%
%   - Tr = MC

function [M, Tr] = calibrate(T, C)

%acquire dimensions of input
[d1 n1] = size(T); % d1 is also number of rows per point for matrix P
[d2 n2] = size(C);

%convert C to homogeneous coordinates
C = [C; ones(1, n2)];

d2 = d2 + 1;

%data entry check...
%===========================================================
stderr	= 2;

%not the same number points in f2d and f3D
if(n1 ~= n2)
	fprintf(stderr, 'error: T and C must contain the same number of data points.');
	return;
end

%no input
if(n1 == 0)
	fprintf(stderr, 'error: T and C are empty.');
	return;
end

%===========================================================
%construction of P matrix in Pm = 0
%===========================================================

%number of correspondances
n = n1;

%transpose T and C
T = T';
C = C';

numColums = d2 * (d1 + 1);

%multiplication of each element of vector in T with current points
products = cell(d1, 1);
for i = 1 : d1
    A = repmat(T(:, i), 1, d2);
    products{i} = -A .* C;
end

%initialize and populate matrix P
P = zeros([d1 * n numColums]);
for i=1 : n
    for j=1 : d1
        P(d1 * i - (d1 - j), 1 + (j - 1) * d2 : j * d2 )  = C(i, :);
        P(d1 * i - (d1 - j), 1 + d2 * d1 : d2 * (d1 + 1))  = products{j}(i, :);
    end
end

%===========================================================
%compute M = (A b) using SVD
%===========================================================

[U, S, V] = svd(P);
m	= V(:, numColums);
M = reshape(m, d1 + 1, d2)';

%The transformed points are colum vectors
Tr = M * C'; 

for i=1 : n1,
    Tr(:, i) = Tr(:, i) ./ Tr(d1 + 1, i);
end

Tr(d1 + 1, :) = [];

%===========================================================
%done
%===========================================================
return;