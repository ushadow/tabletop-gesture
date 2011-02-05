% res = toBinary(I): convert rgb image to binary image, colored pixels are
% converted to white
function res = toBinary(I)

A = I(:,:,1) + I(:,:,2) + I(:,:,3);
res = zeros(size(A));

[X Y] = find(A > 0);

for i=1:size(X),
    res(X(i),Y(i)) = 1;
end

end
