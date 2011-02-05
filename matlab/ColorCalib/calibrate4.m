% Use pseudo-inverse to calculate M in T = MC with homogeneous coordinates
%
%	Inputs:
%		T = target, dependent variables as column vectors for each data point
%		C = independent variables as column vectors for each data point
function [M, Transformed] = calibrate4(T,C)

[dt,nt] = size(T);
[dc,nc] = size(C);

T = [T;ones(1,nt)];
C = [C;ones(1,nc)];

M = T*pinv(C);

Transformed = M*C;

for i=1:nt,
    Transformed(:,i) = Transformed(:,i)./Transformed(4,i);
end

Transformed(4,:) = [];