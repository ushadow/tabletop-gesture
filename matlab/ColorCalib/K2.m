% inputs:
%		u = row vectors
%		v = row vectors
function kval = K2(u, v)

kval = (1 + u * v') .^ 2;

end