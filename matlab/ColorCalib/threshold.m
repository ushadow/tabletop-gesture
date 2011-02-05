function res = threshold(I)

A = I(:,:,1) + I(:,:,2) + I(:,:,3);
res = ones(size(I));

[X Y] = find(A < 0.4);

for i=1:size(X),
    res(X(i),Y(i),:) = 0;
end

end
