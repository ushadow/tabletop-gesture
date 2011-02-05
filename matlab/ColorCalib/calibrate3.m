% T = MC, finding M using pseudo-inverse
% T and C are column vectors
function [M, Transformed] = calibrate3(T, C)

M = T*pinv(C);

Transformed = M*C;