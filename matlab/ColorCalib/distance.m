% find the average distance between two sets of corresponding poitns in I
% and O. I and O are column vectors
function res = distance(I,O)

D = I-O;

[r,c] = size(D);

D = D.^2;

S = zeros(1,c);

for i=1:r,
    S = S+D(i,:);
end

S = S.^0.5;

res = sum(S)/c;
